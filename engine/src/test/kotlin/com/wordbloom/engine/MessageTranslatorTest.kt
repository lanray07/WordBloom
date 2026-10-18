package com.wordbloom.engine
import org.junit.Assert.*
import org.junit.Test

class MessageTranslatorTest {
 @Test fun reorderedParametersAndNestedLabels() {
  val t=MessageTranslator(listOf("Beginner" to "Anfänger","{0} of {1} words · {2}" to "{2}: {0} von {1} Wörtern"))
  assertEquals("Anfänger: 1 von 3 Wörtern",t.text("1 of 3 words · Beginner"))
 }
 @Test fun accentsPricesAndUnknownAnswersArePreserved() {
  val t=MessageTranslator(listOf("Unlock · {0}" to "Débloquer · {0}","Heard: {0}" to "Entendu : {0}"))
  assertEquals("Débloquer · 3,99 €",t.text("Unlock · 3,99 €"))
  assertEquals("Entendu : ÉTOILE",t.text("Heard: ÉTOILE"))
  assertEquals("SŁOWO",t.text("SŁOWO"))
 }
 @Test fun emptyCaptureAndLiteralSpecialCharacters() {
  val t=MessageTranslator(listOf("A ({0}) + {1}" to "{1} / {0}"))
  assertEquals("£3.99 / ",t.text("A () + £3.99"))
  assertEquals("AX / B",t.text("AX / B"))
 }
}
