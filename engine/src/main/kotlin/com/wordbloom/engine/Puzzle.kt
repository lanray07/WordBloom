package com.wordbloom.engine

import java.text.Normalizer
import java.time.LocalDate
import java.util.Locale
import kotlin.random.Random

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED, EXPERT }
data class Entry(val word: String, val x: Int, val y: Int, val down: Boolean) {
 fun cells() = word.mapIndexed { i,c -> Pair(x + if(down) 0 else i, y + if(down) i else 0) to c }.toMap()
}
data class Puzzle(val id: String, val letters: String, val entries: List<Entry>, val bonus: Set<String>, val difficulty: Difficulty, val world: Int) {
 val answers get() = entries.map { it.word }.toSet()
 val cells get() = entries.flatMap { it.cells().entries }.associate { it.key to it.value }
 val width get() = cells.keys.maxOf { it.first } + 1
 val height get() = cells.keys.maxOf { it.second } + 1
}
object Words {
 fun normalise(s: String, locale: Locale = Locale.UK) = Normalizer.normalize(s.trim(), Normalizer.Form.NFC).uppercase(locale)
 fun canForm(word: String, letters: String): Boolean {
  val bag = letters.toMutableList()
  return word.all { bag.remove(it) }
 }
 // Only allow reviewed vocabulary; never infer suitability from spelling alone.
 val blocked = setOf("HATE", "KILL", "DEAD", "GUN", "WAR")
 fun suitable(word: String) = word.length >= 3 && word.all { it.isLetter() } && word !in blocked
}
object Crossword {
 fun generate(id: String, letters: String, words: List<String>, bonus: Set<String>, difficulty: Difficulty, world: Int, seed: Int): Puzzle {
  require(words.distinct().size == words.size && words.isNotEmpty())
  require((words + bonus).all { Words.suitable(it) && Words.canForm(it,letters) })
  val random = Random(seed)
  repeat(100) {
   val order = words.shuffled(random).sortedByDescending { it.length }
   val placed = mutableListOf(Entry(order.first(),0,0,false))
   for(word in order.drop(1)) {
    val candidates = mutableListOf<Entry>()
    for(e in placed) for((a,c) in e.word.withIndex()) for((b,d) in word.withIndex()) if(c==d) {
     val x=e.x + if(e.down) 0 else a
     val y=e.y + if(e.down) a else 0
     val next=Entry(word, x-if(e.down)b else 0, y-if(e.down)0 else b, !e.down)
     if(fits(next,placed)) candidates += next
    }
    if(candidates.isNotEmpty()) placed += candidates.shuffled(random).minBy { candidate ->
     val cells=(placed+candidate).flatMap { it.cells().keys }
     (cells.maxOf { it.first }-cells.minOf { it.first }+1)*(cells.maxOf { it.second }-cells.minOf { it.second }+1)
    }
   }
   if(placed.size==words.size) {
    val minX=placed.minOf { it.x }; val minY=placed.minOf { it.y }
    return Puzzle(id,letters,placed.map { it.copy(x=it.x-minX,y=it.y-minY) },bonus-words.toSet(),difficulty,world)
   }
  }
  error("Unable to connect all answers for $id")
 }
 private fun fits(e: Entry, placed: List<Entry>): Boolean {
  val occupied=placed.flatMap { it.cells().entries }.associate { it.key to it.value }
  val cells=e.cells(); var crosses=0
  if(Pair(e.x-if(e.down)0 else 1,e.y-if(e.down)1 else 0) in occupied) return false
  if(Pair(e.x+if(e.down)0 else e.word.length,e.y+if(e.down)e.word.length else 0) in occupied) return false
  for((pos,c) in cells) {
   val old=occupied[pos]
   if(old!=null) {
    if(old!=c || placed.any { it.down==e.down && pos in it.cells() }) return false
    crosses++
   } else {
    val neighbours=if(e.down) listOf(pos.first-1 to pos.second,pos.first+1 to pos.second) else listOf(pos.first to pos.second-1,pos.first to pos.second+1)
    if(neighbours.any { it in occupied }) return false
   }
  }
  return crosses>0
 }
}
data class Seed(val letters: String, val targets: String, val extras: String = "", val difficulty: Difficulty = Difficulty.BEGINNER)
object Library {
 val worlds=listOf("Sunrise Garden","Enchanted Forest","Blossom Valley","Crystal Lake","Golden Meadow","Moonlight Garden")
 val seeds=listOf(
 Seed("CATS","CAT ACT SAT","CATS CAST"), Seed("POTS","POT TOP STOP","POTS SPOT TOPS"),
 Seed("EATS","EAT TEA SEA","ATE SEAT EAST"), Seed("STAR","ART RAT STAR","TAR RATS ARTS"),
 Seed("CARE","CAR EAR CARE","ARE RACE ACRE"), Seed("SAND","AND SAD SAND","ADS"),
 Seed("STONE","STONE TONE NOTE","ONES NEST SENT",Difficulty.INTERMEDIATE),
 Seed("PLANT","PLANT PLAN PANT","ANT TAN TAP",Difficulty.INTERMEDIATE),
 Seed("HEART","HEART HEAT RATE","EARTH HARE TEAR",Difficulty.INTERMEDIATE),
 Seed("SMILE","SMILE MILE LIME","LIES ISLE SLIM",Difficulty.INTERMEDIATE),
 Seed("TRAIL","TRAIL TAIL LAIR","RAIL LIAR ART",Difficulty.INTERMEDIATE),
 Seed("SHARE","SHARE HEAR HARE","SHEAR RASH SEA",Difficulty.INTERMEDIATE),
 Seed("GARDENS","GARDENS GARDEN DANGER","RANGE SAND READ",Difficulty.ADVANCED),
 Seed("PAINTER","PAINTER PAINT TRAIN","PRINT RAIN PARENT",Difficulty.ADVANCED),
 Seed("LISTENS","LISTENS LISTEN SILENT","TINSEL ENLIST LINES",Difficulty.ADVANCED),
 Seed("FLOWERS","FLOWERS FLOWER LOWER","FLOWS WOLF SLOW",Difficulty.ADVANCED),
 Seed("STREAMS","STREAMS STREAM MASTER","TAMERS TEAMS SMART",Difficulty.ADVANCED),
 Seed("PLANETS","PLANETS PLANET PLANTS","PLANT PLANE LEAST",Difficulty.ADVANCED),
 Seed("GARDENERS","GARDENERS GARDENER GARDENS","GARDEN DANGER RANGES",Difficulty.EXPERT),
 Seed("PAINTERS","PAINTERS PAINTER PARENTS","PAINT PRINT TRAIN",Difficulty.EXPERT),
 Seed("LISTENERS","LISTENERS LISTENER SILENT","LISTEN ENLIST TINSEL",Difficulty.EXPERT),
 Seed("TRIANGLES","TRIANGLES TRIANGLE INTEGRAL","RELATING ALERTING ALTERING",Difficulty.EXPERT),
 Seed("CREATIONS","CREATIONS CREATION REACTION","REACTIONS CATIONS CANOE",Difficulty.EXPERT),
 Seed("STREAMING","STREAMING MASTERING STEAMING","TAMER STREAM TEAMS",Difficulty.EXPERT)
 )
 val puzzles: List<Puzzle> by lazy { seeds.mapIndexed { i,s ->
  Crossword.generate("en-${i+1}",s.letters,s.targets.split(' '),s.extras.split(' ').filter { it.isNotBlank() }.toSet(),s.difficulty,i/4,811+i)
 } }
 fun daily(date: LocalDate, language: String): Puzzle {
  require(language in setOf("en-GB","en-US"))
  val index=Math.floorMod(date.toEpochDay() + language.hashCode().toLong(),puzzles.size.toLong()).toInt()
  return puzzles[index].copy(id="daily:$language:$date",world=(date.toEpochDay()%6).toInt())
 }
}
enum class Verdict { TARGET, BONUS, REPEATED, INVALID }
fun judge(puzzle: Puzzle, raw: String, found: Set<String>, bonuses: Set<String>): Verdict {
 val word=Words.normalise(raw)
 return when {
  !Words.canForm(word,puzzle.letters) -> Verdict.INVALID
  word in found || word in bonuses -> Verdict.REPEATED
  word in puzzle.answers -> Verdict.TARGET
  word in puzzle.bonus -> Verdict.BONUS
  else -> Verdict.INVALID
 }
}
data class TracePoint(val x: Float,val y: Float)
class TileTrace {
 val indices=mutableListOf<Int>()
 fun add(index: Int) { if(indices.size>1 && indices[indices.lastIndex-1]==index) indices.removeAt(indices.lastIndex) else if(index !in indices) indices.add(index) }
 /** Interpolate a sampled pointer segment so fast swipes cannot skip crossed tiles. */
 fun trace(from: TracePoint,to: TracePoint,centres: List<TracePoint>,radius: Float) {
  require(radius>0f)
  val dx=to.x-from.x;val dy=to.y-from.y;val lengthSquared=dx*dx+dy*dy
  centres.mapIndexedNotNull { index,c ->
   val t=if(lengthSquared==0f)0f else (((c.x-from.x)*dx+(c.y-from.y)*dy)/lengthSquared).coerceIn(0f,1f)
   val distanceX=c.x-(from.x+t*dx);val distanceY=c.y-(from.y+t*dy)
   if(distanceX*distanceX+distanceY*distanceY<=radius*radius)index to t else null
  }.sortedBy { it.second }.forEach { add(it.first) }
 }
 fun reset() = indices.clear()
}
enum class Command { START, NEXT, SHUFFLE, HINT, REPEAT, PROGRESS, DAILY, PAUSE, HOME, WORD, AMBIGUOUS }
data class VoiceInput(val command: Command, val word: String = "")
object Commands {
 private val english=mapOf("start puzzle" to Command.START,"next level" to Command.NEXT,"shuffle letters" to Command.SHUFFLE,"give me a hint" to Command.HINT,"repeat the word" to Command.REPEAT,"show my progress" to Command.PROGRESS,"open daily challenge" to Command.DAILY,"pause game" to Command.PAUSE,"go home" to Command.HOME)
 fun parse(text: String, language: String): VoiceInput {
  if(!language.startsWith("en")) return VoiceInput(Command.AMBIGUOUS)
  val value=text.trim().lowercase(Locale.ROOT).removeSuffix(".")
  return english[value]?.let { VoiceInput(it) } ?: if(value.all { it.isLetter() } && value.length>=3) VoiceInput(Command.WORD,Words.normalise(value)) else VoiceInput(Command.AMBIGUOUS)
 }
}
data class EntitlementReceipt(val sku: String, val cancelled: Boolean, val entitlement: Boolean)
object Entitlements {
 const val SKU="wordbloom.premium"
 fun apply(current: Boolean, receipt: EntitlementReceipt): Boolean = if(receipt.sku==SKU && receipt.entitlement) !receipt.cancelled else current
}
