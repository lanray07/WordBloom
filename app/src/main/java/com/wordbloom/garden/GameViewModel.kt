package com.wordbloom.garden

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.wordbloom.engine.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class GameState(val ready: Boolean=false,val page: String="home",val puzzle: Puzzle?=null,val found: Set<String> = emptySet(),val bonus: Set<String> = emptySet(),val revealed: Set<String> = emptySet(),val completed: Set<String> = emptySet(),val hints: Int=8,val message: String="A little wordplay. A little wonder.",val lastWord: String="",val revision: Int=0,val highlighted: Char?=null)
class GameViewModel(app: Application): AndroidViewModel(app) {
 private val db=Room.databaseBuilder(app,GameDatabase::class.java,"wordbloom.db").build()
 private val dao=db.game()
 val settingsRepo=SettingsRepository(app)
 val settings=settingsRepo.flow.stateIn(viewModelScope,SharingStarted.Eagerly,Settings())
 private val mutable=MutableStateFlow(GameState())
 val state=mutable.asStateFlow()
 private var busy=false
 private var premium=false
 fun premiumAccess(value: Boolean) { premium=value }
 init { viewModelScope.launch {
  withContext(Dispatchers.Default) { Library.puzzles }
  val all=dao.all();val wallet=dao.wallet()?:Wallet()
  mutable.value=mutable.value.copy(ready=true,completed=all.filter { it.complete }.map { it.id }.toSet(),hints=wallet.hints)
 } }
 fun page(page: String) { mutable.value=mutable.value.copy(page=page,message="") }
 fun notify(message: String) { mutable.value=mutable.value.copy(message=message) }
 fun allowed(index: Int,premium: Boolean)= index in Library.puzzles.indices && (index<4 || premium) && (index==0 || Library.puzzles[index-1].id in state.value.completed || Library.puzzles[index].id in state.value.completed)
 fun start(index: Int=resumeIndex(),premium: Boolean=false) {
  if(!state.value.ready) return
  if(!allowed(index,premium)) { page(if(index>=4 && !premium) "premium" else "worlds");return }
  load(Library.puzzles[index])
 }
 fun resumeIndex(): Int = Library.puzzles.indexOfFirst { it.id !in state.value.completed }.let { if(it<0)0 else it }
 fun daily() { if(state.value.ready) load(Library.daily(LocalDate.now(),settings.value.language)) }
 private fun load(puzzle: Puzzle) { viewModelScope.launch {
  val p=dao.get(puzzle.id)?:Progress(puzzle.id)
  mutable.value=state.value.copy(page="game",puzzle=puzzle,found=unpack(p.found),bonus=unpack(p.bonus),revealed=unpack(p.revealed),message="Find ${puzzle.answers.size} words. Swipe or tap the letters.",highlighted=null,lastWord="")
 } }
 fun replay() { val s=state.value;val p=s.puzzle?:return;if(p.id.startsWith("daily"))return;update(s.copy(found=emptySet(),bonus=emptySet(),revealed=emptySet(),lastWord="",message="A fresh look at a familiar puzzle. Replay earns no additional hints.")) }
 fun next(premium: Boolean) { val p=state.value.puzzle?:return;if(p.id.startsWith("daily"))page("home") else start((Library.puzzles.indexOfFirst { it.id==p.id }+1)%Library.puzzles.size,premium) }
 fun shuffle() { mutable.value=state.value.copy(highlighted=null,revision=state.value.revision+1,message="Letters shuffled") }
 fun starting() { val s=state.value;val p=s.puzzle?:return;mutable.value=s.copy(highlighted=p.answers.firstOrNull { it !in s.found }?.first(),message="Look for the highlighted starting letter") }
 fun submit(raw: String) {
  if(busy)return
  val s=state.value;val p=s.puzzle?:return;val word=Words.normalise(raw)
  when(judge(p,word,s.found,s.bonus)) {
   Verdict.TARGET -> update(s.copy(found=s.found+word,lastWord=word,highlighted=null,message="Beautiful! $word"))
   Verdict.BONUS -> { val reward=if(p.id in s.completed)0 else 1;update(s.copy(bonus=s.bonus+word,hints=s.hints+reward,lastWord=word,message="Bonus word! $word${if(reward>0)" · +1 hint" else ""}")) }
   Verdict.REPEATED -> notify("You already found $word")
   Verdict.INVALID -> notify("That word is not in this puzzle’s curated word list")
  }
 }
 fun hint(whole: Boolean) {
  if(busy)return
  val s=state.value;val p=s.puzzle?:return
  val word=p.answers.firstOrNull { it !in s.found }?:return
  val cost=if(whole)3 else 1
  if(s.hints<cost) { notify("Earn hints by completing puzzles or finding bonus words");return }
  if(whole) update(s.copy(found=s.found+word,hints=s.hints-cost,lastWord=word,message="Revealed $word"))
  else {
   val e=p.entries.first { it.word==word }
   val visible=p.entries.filter { it.word in s.found }.flatMap { it.cells().keys }.map { "${it.first},${it.second}" }.toSet()+s.revealed
   val cell=e.cells().keys.firstOrNull { "${it.first},${it.second}" !in visible }
   if(cell==null) { update(s.copy(found=s.found+word,lastWord=word,message="All its letters are visible: $word"));return }
   update(s.copy(hints=s.hints-cost,revealed=s.revealed+"${cell.first},${cell.second}",message="One new letter, a little closer"))
  }
 }
 private fun update(proposed: GameState) {
  if(busy)return;busy=true
  viewModelScope.launch {
   try {
    val p=proposed.puzzle?:return@launch
    val complete=proposed.found.containsAll(p.answers)
    val first=complete && p.id !in proposed.completed
    val reward=if(premium)4 else 3
    val next=proposed.copy(completed=if(complete)proposed.completed+p.id else proposed.completed,hints=proposed.hints+if(first)reward else 0,message=if(first)"Puzzle complete. Your garden is growing! +$reward hints" else proposed.message)
    dao.save(Progress(p.id,next.found.joinToString("|"),next.bonus.joinToString("|"),next.revealed.joinToString("|"),complete || p.id in next.completed),Wallet(hints=next.hints))
    val live=state.value
    mutable.value=if(live.puzzle?.id==p.id)next.copy(page=live.page) else live.copy(completed=next.completed,hints=next.hints)
   } catch(e: Exception) { notify("Progress could not be saved. Please try again.") } finally { busy=false }
  }
 }
 private fun unpack(value: String)=value.split('|').filter { it.isNotEmpty() }.toSet()
 override fun onCleared() { db.close() }
}
