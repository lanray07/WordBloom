package com.wordbloom.garden
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import android.graphics.Bitmap

@RunWith(AndroidJUnit4::class)
class GameUiTest {
 @get:Rule val rule=createAndroidComposeRule<MainActivity>()
 private fun begin() {
  rule.waitUntil(15000) { rule.onAllNodesWithText("Begin your journey  â†’").fetchSemanticsNodes().isNotEmpty() || rule.onAllNodesWithText("Continue your journey  â†’").fetchSemanticsNodes().isNotEmpty() }
  val begin=rule.onAllNodesWithText("Begin your journey  â†’")
  if(begin.fetchSemanticsNodes().isNotEmpty())begin[0].performClick() else rule.onNodeWithText("Continue your journey  â†’").performClick()
  rule.waitForIdle()
 }
 private fun enter(word: String) { rule.onNodeWithText("Type a word").performScrollTo().performTextInput(word);rule.onNodeWithText("Check",useUnmergedTree=true).performClick();rule.waitForIdle() }
 private fun capture(name: String) {
  rule.waitForIdle()
  val activity=rule.activity;val folder=File(activity.getExternalFilesDir(null),"screenshots").apply { mkdirs() }
  val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()?:error("Screenshot failed")
  File(folder,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
 }
 @Test fun accessibleTapAndWordValidation() {
  begin()
  // This test expects a fresh installation / cleared application data.
  rule.onNodeWithContentDescription("Letter C, tile 1").performClick()
  rule.onNodeWithContentDescription("Letter A, tile 2").performClick()
  rule.onNodeWithContentDescription("Letter T, tile 3").performClick()
  rule.onNodeWithText("Submit word").performClick()
  rule.waitUntil(5000) { rule.onAllNodesWithText("1 of 3 words found  Â·  Beginner").fetchSemanticsNodes().isNotEmpty() }
  enter("ACT");enter("SAT")
  rule.onNodeWithText("You grew something lovely.").assertExists()
 }
 @Test fun screenshotTour() {
  Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("capture")=="true")
  begin();capture("01-gameplay")
  enter("CAT");capture("02-crossword")
  rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
  rule.onNodeWithText("Play today  â†’").performScrollTo().performClick();capture("03-daily")
  rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
  rule.onNodeWithText("Visit my garden  â†’").performScrollTo().performClick();capture("04-garden")
  rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
  rule.onNodeWithText("Explore the journey  â†’").performScrollTo().performClick();capture("05-difficulty")
  rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
  rule.onNodeWithText("Settings",useUnmergedTree=true).performClick();rule.onNodeWithText("Voice, when you want it").performScrollTo();capture("06-voice")
  rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() };capture("07-offline")
  rule.onNodeWithText("Settings",useUnmergedTree=true).performClick();rule.onNodeWithText("Puzzle language").performScrollTo();capture("08-english")
 }
 @Test fun roomPersistsAtomicWalletAndProgress() = runBlocking {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val name="wordbloom-test-${System.nanoTime()}.db"
  var db=Room.databaseBuilder(context,GameDatabase::class.java,name).build()
  db.game().save(Progress("test",found="CAT|ACT",revealed="1,2"),Wallet(hints=7));db.close()
  db=Room.databaseBuilder(context,GameDatabase::class.java,name).build()
  assertEquals("CAT|ACT",db.game().get("test")?.found);assertEquals("1,2",db.game().get("test")?.revealed);assertEquals(7,db.game().wallet()?.hints)
  db.close();context.deleteDatabase(name)
 }
}
