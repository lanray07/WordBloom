package com.wordbloom.garden

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wordbloom.engine.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Fresh normal-debug installation only. Checks real persisted collection boundaries. */
@RunWith(AndroidJUnit4::class)
class LocaleProgressDeviceTest {
 @get:Rule val rule=createEmptyComposeRule()
 @Test fun collectionBoundariesAndSharedEnglishProgress() {
  val args=InstrumentationRegistry.getArguments()
  Assume.assumeTrue(args.getString("localeProgress")=="true")
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  assertEquals("com.wordbloom.garden",context.packageName)
  val source=args.getString("sourceLocale")!!;val target=args.getString("targetLocale")!!
  val shared=args.getString("shared")=="true"
  val catalog=UiCatalog(context)
  fun t(text: String,locale: String)=catalog.text(text,locale)
  fun wait(text: String) { rule.waitUntil(15000) { rule.onAllNodesWithText(text,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty() } }
  fun click(text: String) { val node=rule.onNodeWithText(text,useUnmergedTree=true);if(!node.isDisplayed())node.performScrollTo();node.performClick();rule.waitForIdle() }
  runBlocking { SettingsRepository(context).language(source) }
  val scenario=ActivityScenario.launch(MainActivity::class.java)
  fun enter(word: String,locale: String) {
   rule.onNodeWithText(t("Type a word",locale)).performScrollTo().performTextInput(word)
   click(t("Check",locale))
   scenario.onActivity { WindowInsetsControllerCompat(it.window,it.window.decorView).hide(WindowInsetsCompat.Type.ime()) }
   rule.waitForIdle()
  }
  try {
   wait(t("Begin your journey  →",source));click(t("Begin your journey  →",source))
   val word=Library.forLanguage(source).first().answers.first()
   enter(word,source);wait(t("Beautiful! $word",source))
   fun switch(from: String,to: String) {
    click(t("Settings",from));click(ApprovedLanguages.names.getValue(to))
    wait(t("Begin your journey  →",to));click(t("Begin your journey  →",to))
   }
   switch(source,target)
   wait(t("Words found: ${if(shared)1 else 0} / 3 · ${Library.forLanguage(target).first().difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}",target))
   if(!shared) {
    val other=Library.forLanguage(target).first().answers.first()
    enter(other,target);wait(t("Beautiful! $other",target))
   }
   switch(target,source)
   wait(t("Words found: 1 / 3 · ${Library.forLanguage(source).first().difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}",source))
   enter(word,source);wait(t("You already found $word",source))
   assertEquals(source,runBlocking { SettingsRepository(context).flow.first().language })
   println("SOURCE=$source TARGET=$target SHARED=$shared PROGRESS_PRESERVED=true")
  } finally { scenario.close() }
 }
}
