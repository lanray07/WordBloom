package com.wordbloom.engine

import org.junit.Assert.*
import org.junit.Test
import java.text.Normalizer

class LanguagePackTest {
 @Test fun allEightDraftEditionsHaveDistinctSolvableProgressIds() {
  val ids = mutableSetOf<String>()
  DraftLanguagePacks.locales.forEach { locale ->
   val puzzles = DraftLanguagePacks.puzzles(locale)
   assertEquals(locale,24,puzzles.size)
   puzzles.forEach { p ->
    assertTrue(ids.add(p.id))
    assertEquals(3,p.answers.size)
    assertTrue(p.letters.length <= 12)
    p.entries.forEach { e ->
     assertEquals(e.word,Normalizer.normalize(e.word,Normalizer.Form.NFC))
     assertEquals(Verdict.TARGET,judge(p,e.word,emptySet(),emptySet()))
     e.cells().forEach { (cell,c) -> assertEquals(c,p.cells[cell]) }
    }
    val reached=mutableSetOf(p.entries.first())
    repeat(3) { p.entries.filter { e -> reached.any { it.cells().keys.intersect(e.cells().keys).isNotEmpty() } }.forEach { reached+=it } }
    assertEquals(p.entries.size,reached.size)
   }
  }
  assertEquals(192,ids.size)
 }
 @Test fun accentsAreNotSilentlyRemovedOrSubstituted() {
  assertFalse(Words.canForm("OGRÓD","OGROD"))
  assertFalse(Words.canForm("ÉTOILE","ETOILE"))
  assertTrue(Words.canForm("SŁOWO","SŁOWO"))
 }
 @Test fun draftCannotSilentlyBecomeAnApprovedLanguage() {
  (DraftLanguagePacks.locales-ApprovedLanguages.locales.toSet()).forEach { locale ->
   try { Library.forLanguage(locale);fail("Unapproved locale activated: $locale") } catch(expected: IllegalArgumentException) { }
  }
 }
 @Test fun localizedSpokenWordsAndCommands() {
  assertEquals(Command.SHUFFLE,Commands.parse("Mélanger les lettres.","fr").command)
  assertEquals(Command.SHUFFLE,Commands.parse("Wymieszaj litery","pl").command)
  assertEquals("ÉTOILE",Commands.parse("e\u0301toile","fr").word)
  assertEquals("SŁOWO",Commands.parse("słowo","pl").word)
 }
}
