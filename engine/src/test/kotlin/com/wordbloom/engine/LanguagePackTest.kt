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
  ((DraftLanguagePacks.locales-ApprovedLanguages.locales.toSet())+listOf("ar","hi","zz")).forEach { locale ->
   try { Library.forLanguage(locale);fail("Unapproved locale activated: $locale") } catch(expected: IllegalArgumentException) { }
  }
 }
 @Test fun localizedSpokenWordsAndCommands() {
  assertEquals(Command.SHUFFLE,Commands.parse("Mélanger les lettres.","fr").command)
  assertEquals(Command.SHUFFLE,Commands.parse("Wymieszaj litery","pl").command)
  assertEquals("ÉTOILE",Commands.parse("e\u0301toile","fr").word)
  assertEquals("SŁOWO",Commands.parse("słowo","pl").word)
 }
 @Test fun everyDisplayedCommandIsAcceptedInItsLanguage() {
  val expected=listOf(Command.START,Command.NEXT,Command.SHUFFLE,Command.HINT,Command.REPEAT,Command.PROGRESS,Command.DAILY,Command.PAUSE,Command.HOME)
  (DraftLanguagePacks.locales+"en-GB").forEach { locale ->
   val phrases=Commands.examples(locale).split(" · ")
   assertEquals(9,phrases.size)
   phrases.zip(expected).forEach { (phrase,command) -> assertEquals("$locale: $phrase",command,Commands.parse(phrase,locale).command) }
  }
  assertEquals(Command.SHUFFLE,Commands.parse("embaralhar letras","pt-BR").command)
  assertEquals(Command.SHUFFLE,Commands.parse("baralhar letras","pt-PT").command)
 }
 @Test fun draftCollectionsContainNoDuplicateTargetSets() {
  DraftLanguagePacks.locales.forEach { locale ->
   val sets=DraftLanguagePacks.puzzles(locale).map { it.answers.toSet() }
   assertEquals(locale,sets.size,sets.distinct().size)
  }
 }
}
