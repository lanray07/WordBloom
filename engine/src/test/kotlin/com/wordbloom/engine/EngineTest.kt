package com.wordbloom.engine
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.util.Locale
class EngineTest {
 @Test fun allPuzzlesSolvableAndConnected() {
  assertEquals(24,Library.puzzles.size)
  Library.puzzles.forEach { p ->
   assertEquals(3,p.answers.size)
   p.answers.forEach { assertTrue("${p.id}: $it",Words.canForm(it,p.letters)); assertEquals(Verdict.TARGET,judge(p,it,emptySet(),emptySet())) }
   p.entries.forEach { e -> e.cells().forEach { (pos,c) -> assertEquals(c,p.cells[pos]) } }
   val reached=mutableSetOf(p.entries.first()); var before=0
   while(before!=reached.size) { before=reached.size; p.entries.filter { e -> reached.any { it.cells().keys.intersect(e.cells().keys).isNotEmpty() } }.forEach { reached+=it } }
   assertEquals(p.entries.size,reached.size)
  }
 }
 @Test fun reproducibleLayoutsAcrossSeeds() {
  Library.seeds.forEachIndexed { i,s ->
   for(seed in 0..15) {
    fun gen()=Crossword.generate("t",s.letters,s.targets.split(' '),emptySet(),s.difficulty,0,seed)
    assertEquals(gen(),gen())
   }
  }
 }
 @Test fun repeatedLettersAndTileUndo() {
  assertFalse(Words.canForm("LETTER","LETER")); assertTrue(Words.canForm("LETTER","LETTER"))
  val t=TileTrace();t.add(0);t.add(1);t.add(1);assertEquals(listOf(0,1),t.indices);t.add(0);assertEquals(listOf(0),t.indices)
 }
 @Test fun fastPointerSamplesPreserveCrossedTilesAndUndo() {
  val points=listOf(TracePoint(0f,0f),TracePoint(20f,0f),TracePoint(40f,0f))
  val trace=TileTrace()
  trace.trace(points[0],points[2],points,6f)
  assertEquals(listOf(0,1,2),trace.indices)
  trace.trace(points[2],points[0],points,6f)
  assertEquals(listOf(0),trace.indices)
 }
 @Test fun pointerNearMissDoesNotSelectTile() {
  val trace=TileTrace()
  trace.trace(TracePoint(0f,11f),TracePoint(40f,11f),listOf(TracePoint(20f,0f)),5f)
  assertTrue(trace.indices.isEmpty())
 }
 @Test fun invalidBonusAndDuplicate() {
  val p=Library.puzzles.first();assertEquals(Verdict.BONUS,judge(p,"cats",emptySet(),emptySet()));assertEquals(Verdict.REPEATED,judge(p,"CAT",setOf("CAT"),emptySet()));assertEquals(Verdict.INVALID,judge(p,"XXXXX",emptySet(),emptySet()))
 }
 @Test fun dailyIdentityUsesDateAndLocale() {
  val d=LocalDate.of(2026,10,16);assertEquals(Library.daily(d,"en-GB"),Library.daily(d,"en-GB"));assertNotEquals(Library.daily(d,"en-GB").id,Library.daily(d.plusDays(1),"en-GB").id)
 }
 @Test fun unicodeAndSafety() {
  assertEquals("ÉTÉ",Words.normalise("e\u0301te\u0301",Locale.FRANCE));assertFalse(Words.suitable("HATE"));assertTrue(Words.canForm("ÉTÉ","ÉTÉ"))
 }
 @Test fun commandsAndAmbiguity() {
  assertEquals(Command.SHUFFLE,Commands.parse("Shuffle letters.","en-GB").command);assertEquals(Command.AMBIGUOUS,Commands.parse("do something please","en-GB").command);assertEquals(Command.WORD,Commands.parse("cat","en-US").command)
 }
 @Test fun entitlementCancellationAndWrongSku() {
  assertTrue(Entitlements.apply(false,EntitlementReceipt(Entitlements.SKU,false,true)));assertFalse(Entitlements.apply(true,EntitlementReceipt(Entitlements.SKU,true,true)));assertFalse(Entitlements.apply(false,EntitlementReceipt("unknown",false,true)))
 }
 @Test(expected=IllegalArgumentException::class) fun impossibleRejected() { Crossword.generate("bad","CAT",listOf("DOG"),emptySet(),Difficulty.BEGINNER,0,1) }
}
