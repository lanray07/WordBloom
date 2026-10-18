package com.wordbloom.garden

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.semantics.SemanticsActions
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wordbloom.engine.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import android.graphics.Bitmap
import java.io.File

/** Opt-in instrumentation for isolated .qa installation; no purchase is made. */
@RunWith(AndroidJUnit4::class)
class LocalizationDeviceTest {
 @get:Rule val rule=createEmptyComposeRule()
 @Test fun languageSelectionGameplayAndPersistence() {
  val args=InstrumentationRegistry.getArguments()
  Assume.assumeTrue(args.getString("localizationQA")=="true")
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val production=args.getString("reviewedProduction")=="true"
  assertEquals(if(production)"com.wordbloom.garden" else "com.wordbloom.garden.qa",context.packageName)
  val locale=args.getString("locale")!!
  val catalogue=UiCatalog(context)
  fun t(text: String)=catalogue.text(text,locale)
  fun wait(text: String) { rule.waitUntil(15000) { rule.onAllNodesWithText(text,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty() } }
  fun click(text: String) {
   val node=rule.onNodeWithText(text,useUnmergedTree=true)
   if(!node.isDisplayed())node.performScrollTo()
   node.performClick();rule.waitForIdle()
  }
  runBlocking {
   SettingsRepository(context).language(if(locale=="en-GB")"en-US" else "en-GB")
   if(production && args.getString("orientation")=="portrait")SettingsRepository(context).set("large",true)
  }
  val scenario=ActivityScenario.launch(MainActivity::class.java)
  try {
   wait("Begin your journey  →")
   click("Settings")
   click(ApprovedLanguages.names.getValue(locale))
   // Changing the collection intentionally returns to its fresh Home screen.
   wait(t("Begin your journey  →"))
   click(t("Settings"))
   wait(t("Puzzle language"))
   val expected=if(args.getString("orientation")=="landscape")2 else 1
   scenario.onActivity { assertEquals(expected,it.resources.configuration.orientation) }
   fun capture(name: String) {
    rule.waitForIdle();InstrumentationRegistry.getInstrumentation().uiAutomation.waitForIdle(200,5000)
    val folder=File(context.getExternalFilesDir(null),"language-qa/$locale").apply { mkdirs() }
    val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()!!
    File(folder,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
   }
   capture("settings")
   scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
   wait(t("Begin your journey  →"));capture("home")
   click(t("Begin your journey  →"))
   val first=Library.forLanguage(locale).first()
   wait(t("Find 3 words. Swipe or tap the letters."))
   fun enter(word: String) {
    rule.onNodeWithText(t("Type a word")).performScrollTo().performTextInput(word)
    click(t("Check"))
    scenario.onActivity { WindowInsetsControllerCompat(it.window,it.window.decorView).hide(WindowInsetsCompat.Type.ime()) }
   }
   enter("ZZZ");wait(t("That word is not in this puzzle’s curated word list"))
   enter(first.answers.first());wait(t("Beautiful! ${first.answers.first()}"));capture("gameplay")
   scenario.recreate();wait(t("Beautiful! ${first.answers.first()}"))
   first.answers.drop(1).forEach { word -> enter(word);wait(t(if(word==first.answers.last())"Puzzle complete. Your garden is growing! Hints earned: +3" else "Beautiful! $word")) }
   wait(t("You grew something lovely."));capture("complete")
   scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
   click(t("Visit my garden  →"));wait(t("Flowers planted: 1"));capture("garden")
   scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
   // The compact header omits the premium shortcut; use the real locked-level route.
   click(t("Explore the journey  →"));click("◇  5")
   wait(t("One-time purchase · No ads · No subscription"));capture("premium")
   // Exercise a curated accented/long target without altering any entitlement cache.
   // Seed prerequisite progress in this QA-only database; bypass the paywall only
   // through the existing VM test call below. This is input/layout QA, not billing QA.
   val library=Library.forLanguage(locale)
   val index=if(production)library.indices.maxBy { i -> library[i].letters.length+if(library[i].answers.any { w -> w.any { it.code>127 } })100 else 0 }
    else library.indexOfFirst { p -> p.answers.any { word -> word.any { it.code>127 } } }.let { if(it<0)library.lastIndex else it }
   val db=androidx.room.Room.databaseBuilder(context,GameDatabase::class.java,"wordbloom.db").build()
   runBlocking { library.take(index).forEach { db.game().put(Progress(it.id,complete=true)) } };db.close()
   scenario.close()
   val second=ActivityScenario.launch(MainActivity::class.java)
   try {
    rule.waitUntil(15000) { var ready=false;second.onActivity { ready=ViewModelProvider(it)[GameViewModel::class.java].state.value.ready };ready }
    second.onActivity { ViewModelProvider(it)[GameViewModel::class.java].start(index,premium=true) }
    val puzzle=library[index];val word=puzzle.answers.firstOrNull { w -> w.any { it.code>127 } }?:puzzle.answers.maxBy { it.length }
    wait(t("Find 3 words. Swipe or tap the letters."))
    rule.onNodeWithText(t("Type a word")).performScrollTo().performTextInput(word.lowercase(java.util.Locale.forLanguageTag(locale)))
    click(t("Check"));wait(t("Beautiful! $word"))
    val longest=puzzle.answers.maxBy { it.length }
    if(production && longest!=word) {
     rule.onNodeWithText(t("Type a word")).performScrollTo().performTextInput(longest.lowercase(java.util.Locale.forLanguageTag(locale)))
     click(t("Check"));wait(t("Beautiful! $longest"))
    }
    second.onActivity { WindowInsetsControllerCompat(it.window,it.window.decorView).hide(WindowInsetsCompat.Type.ime()) }
    capture("accent-or-long-word")
    if(production) {
     rule.onAllNodes(hasScrollAction()).onFirst().performSemanticsAction(SemanticsActions.ScrollBy) { it(0f,-10000f) }
     capture("accent-or-long-word-board")
    }
    println("LOCALE=$locale ORIENTATION=${args.getString("orientation")} FIRST=${first.id} INPUT=$word LONGEST=$longest BAG_SIZE=${puzzle.letters.length} LANGUAGE_PERSISTED=true NO_PURCHASE=true")
   } finally { second.close() }
  } finally { try { scenario.close() } catch(_: Exception){} }
 }
}
