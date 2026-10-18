package com.wordbloom.garden

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Run separately on a fresh WordBloom test installation. No simulated billing. */
@RunWith(AndroidJUnit4::class)
class FireRuntimeTest {
 @get:Rule val rule = createAndroidComposeRule<MainActivity>()
 private fun click(text: String) {
  rule.onNodeWithText(text, useUnmergedTree = true).performScrollTo().performClick()
  rule.waitForIdle()
 }
 private fun home() {
  rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
  rule.waitForIdle()
 }
 private fun begin() {
  rule.waitUntil(15000) { rule.onAllNodesWithText("Begin your journey  →").fetchSemanticsNodes().isNotEmpty() }
  orientation()
  click("Begin your journey  →")
 }
 private fun orientation() {
  InstrumentationRegistry.getArguments().getString("orientation")?.let { expected ->
   val target=if(expected=="landscape")2 else 1
   rule.waitUntil(5000) { rule.activity.resources.configuration.orientation==target }
   println("DEVICE_ORIENTATION=$expected")
  }
 }
 @Test fun invalidBonusHintsRecreationAndVoiceFallback() {
  Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("runtime") == "true")
  begin()
  click("Type a word")
  rule.onNodeWithText("Type a word").performTextInput("ZZZ")
  click("Check")
  rule.onNodeWithText("That word is not in this puzzle’s curated word list").assertExists()
  rule.onNodeWithText("Type a word").performTextInput("CAST")
  click("Check")
  rule.waitUntil(5000) { rule.onAllNodesWithText("Bonus words: CAST").fetchSemanticsNodes().isNotEmpty() }
  rule.onNodeWithText("✦ Hints: 9").assertExists()
  click("✦ Letter · 1")
  rule.waitUntil(5000) { rule.onAllNodesWithText("✦ Hints: 8").fetchSemanticsNodes().isNotEmpty() }
  rule.onNodeWithText("Type a word").performScrollTo().performTextInput("CAT")
  click("Check")
  rule.waitUntil(5000) { rule.onAllNodesWithText("Words found: 1 / 3 · Beginner").fetchSemanticsNodes().isNotEmpty() }
  rule.activityRule.scenario.recreate()
  rule.waitForIdle()
  rule.onNodeWithText("Words found: 1 / 3 · Beginner").assertExists()
  rule.onNodeWithText("Bonus words: CAST").assertExists()
  rule.onNodeWithText("✦ Hints: 8").assertExists()
  // This Fire device has no recognition service. Touch play must remain usable.
  if(rule.onAllNodesWithText("Voice availability").fetchSemanticsNodes().isNotEmpty()) {
   click("Voice availability")
   rule.onNodeWithText("Speech recognition unavailable. You can swipe, tap, or type words.").assertExists()
  }
  home()
  click("Play today  →")
  rule.onNodeWithText("DAILY WORD ESCAPE").assertExists()
  home()
  click("Visit my garden  →")
  rule.onNodeWithText("Flowers planted: 0").assertExists()
 }
 @Test fun accessibilitySettingsPersistAfterRecreation() {
  Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("settings") == "true")
  rule.waitUntil(15000) { rule.onAllNodesWithText("Begin your journey  →").fetchSemanticsNodes().isNotEmpty() }
  orientation()
  rule.onNodeWithText("Settings",useUnmergedTree=true).performClick()
  listOf("High-contrast letter tiles","Reduce motion","Larger text","Evening theme").forEach { name ->
   rule.onNodeWithContentDescription(name).performScrollTo().performClick()
   rule.waitUntil(5000) { rule.onAllNodesWithContentDescription(name).filter(isOn()).fetchSemanticsNodes().isNotEmpty() }
  }
  rule.activityRule.scenario.recreate();rule.waitForIdle()
  listOf("High-contrast letter tiles","Reduce motion","Larger text","Evening theme").forEach { name ->
   rule.onNodeWithContentDescription(name).performScrollTo()
   rule.waitUntil(5000) { rule.onAllNodesWithContentDescription(name).filter(isOn()).fetchSemanticsNodes().isNotEmpty() }
  }
 }
 @Test fun continuousTouchSwipeAndBacktracking() {
  Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("gesture") == "true")
  begin()
  rule.onNodeWithContentDescription("Letter C, tile 1").performScrollTo()
  fun centre(description: String)=rule.onNodeWithContentDescription(description).fetchSemanticsNode().boundsInRoot.center
  val c=centre("Letter C, tile 1");val a=centre("Letter A, tile 2");val t=centre("Letter T, tile 3")
  rule.onRoot().performTouchInput { down(c);moveTo(a);moveTo(t);up() }
  rule.waitUntil(5000) { rule.onAllNodesWithText("Words found: 1 / 3 · Beginner").fetchSemanticsNodes().isNotEmpty() }
  // C -> A -> C removes A, leaving CT, which must not complete another word.
  rule.onRoot().performTouchInput { down(c);moveTo(a);moveTo(c);moveTo(t);up() }
  rule.waitForIdle()
  rule.onNodeWithText("That word is not in this puzzle’s curated word list").assertExists()
  rule.onNodeWithText("Words found: 1 / 3 · Beginner").assertExists()
 }
}
