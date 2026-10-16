package com.wordbloom.garden

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import com.wordbloom.engine.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

private val Pine=Color(0xFF244E43)
private val Sage=Color(0xFF70866B)
private val Cream=Color(0xFFFAF6ED)
private val Peach=Color(0xFFECC0AA)
private val Gold=Color(0xFFE6C678)
class MainActivity: ComponentActivity() {
 private val vm: GameViewModel by viewModels()
 private lateinit var voice: Voice
 private lateinit var billing: Billing
 private lateinit var ambience: Ambience
 private var voiceText by mutableStateOf("")
 private var listening by mutableStateOf(false)
 private val permission=registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if(granted)voice.listen() else { voiceText="Microphone permission denied. Swipe, tap or type to play." } }
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  billing=BillingFactory.create(this)
  ambience=Ambience(this)
  voice=Voice(this, { text -> command(text) }, { text, active -> voiceText=text;listening=active })
  setContent {
   val s by vm.state.collectAsStateWithLifecycle();val settings by vm.settings.collectAsStateWithLifecycle();val store by billing.state.collectAsStateWithLifecycle()
   val colours=if(settings.dark) darkColorScheme(primary=Color(0xFFA9CEB5),background=Color(0xFF132B25),surface=Color(0xFF203C33),onSurface=Color(0xFFF6EEDD),onBackground=Color(0xFFF6EEDD),secondary=Peach) else lightColorScheme(primary=Pine,background=Cream,surface=Color.White,onSurface=Pine,onBackground=Pine,secondary=Sage)
   val base=LocalDensity.current
   val uiScope=rememberCoroutineScope()
   CompositionLocalProvider(LocalDensity provides Density(base.density,base.fontScale*if(settings.large)1.2f else 1f),LocalLayoutDirection provides LayoutDirection.Ltr) {
    MaterialTheme(colorScheme=colours) {
     LaunchedEffect(settings.dark) {
      @Suppress("DEPRECATION")
      run { window.statusBarColor=colours.background.toArgb();window.navigationBarColor=colours.background.toArgb() }
      WindowCompat.getInsetsController(window,window.decorView).apply {
       isAppearanceLightStatusBars=!settings.dark
       isAppearanceLightNavigationBars=!settings.dark
      }
     }
     LaunchedEffect(store.premium) { vm.premiumAccess(store.premium) }
     LaunchedEffect(settings.music) { ambience.enabled(settings.music) }
     LaunchedEffect(settings.language) { voice.locale=Locale.forLanguageTag(settings.language) }
     LaunchedEffect(s.message) { if(settings.narration && s.message.isNotBlank())voice.say(s.message) }
     LaunchedEffect(s.lastWord) { if(s.lastWord.isNotEmpty() && settings.effects) { try { ToneGenerator(AudioManager.STREAM_MUSIC,25).also { tone -> try { tone.startTone(ToneGenerator.TONE_PROP_ACK,100);kotlinx.coroutines.delay(130) } finally { tone.release() } } }catch(_: Exception){} } }
     BackHandler(s.page!="home") { vm.page("home") }
     Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
      Column(Modifier.safeDrawingPadding().fillMaxSize()) {
       if(BuildConfig.DEVELOPMENT_BILLING)Text("DEVELOPMENT BUILD · TEST PURCHASES · NO REAL MONEY",Modifier.fillMaxWidth().background(Peach).padding(8.dp),color=Pine,textAlign=TextAlign.Center,fontSize=12.sp)
       Header(s.page,store.premium) { vm.page(it) }
       if(!s.ready) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) { CircularProgressIndicator() }
       else Box(Modifier.fillMaxSize(),contentAlignment=Alignment.TopCenter) {
        when(s.page) {
         "game" -> GameScreen(s,settings,voiceText,listening,voice.available,{ vm.submit(it) },{ vm.shuffle() },{ vm.hint(it) },{ vm.starting() },{ vm.next(store.premium) },{ vm.replay() },{ talk() },{ voice.say(s.lastWord.ifBlank { s.puzzle?.letters?.toList()?.joinToString(", ")?:"" }) })
         "worlds" -> Worlds(s,store.premium,{ vm.start(it,store.premium) },{ vm.page("premium") })
         "garden" -> GardenScreen(s,store.premium)
         "settings" -> SettingsScreen(settings,voice.available,{ key,value -> uiScope.launch { vm.settingsRepo.set(key,value) } },{ language -> uiScope.launch { vm.settingsRepo.language(language) } })
         "premium" -> Premium(store,{ billing.purchase() },{ billing.restore() })
         else -> Home(s,{ vm.start(premium=store.premium) },{ vm.daily() },{ vm.page(it) })
        }
       }
      }
     }
    }
   }
  }
 }
 private fun talk() { if(listening)voice.stop() else if(!voice.available)voice.listen() else if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)voice.listen() else permission.launch(Manifest.permission.RECORD_AUDIO) }
 private fun command(text: String) {
  when(val input=Commands.parse(text,vm.settings.value.language)) {
   else -> when(input.command) {
    Command.START -> vm.start(premium=billing.state.value.premium)
    Command.NEXT -> if(vm.state.value.puzzle?.answers?.all { it in vm.state.value.found }==true)vm.next(billing.state.value.premium) else vm.notify("Finish this puzzle before going to the next level")
    Command.SHUFFLE -> vm.shuffle()
    Command.HINT -> vm.hint(false)
    Command.REPEAT -> voice.say(vm.state.value.lastWord.ifBlank { "Find a word first" })
    Command.PROGRESS -> vm.page("garden")
    Command.DAILY -> vm.daily()
    Command.PAUSE,Command.HOME -> vm.page("home")
    Command.WORD -> if(vm.state.value.page=="game")vm.submit(input.word) else vm.notify("Open a puzzle to play a word")
    else -> vm.notify("Try ‘shuffle letters’, ‘give me a hint’, or say one word")
   }
  }
 }
 override fun onResume() { super.onResume();if(::billing.isInitialized)billing.refresh();if(::ambience.isInitialized)ambience.enabled(vm.settings.value.music) }
 override fun onPause() { if(::voice.isInitialized)voice.stop();if(::ambience.isInitialized)ambience.pause();super.onPause() }
 override fun onDestroy() { if(::voice.isInitialized)voice.close();if(::ambience.isInitialized)ambience.close();super.onDestroy() }
}
@Composable private fun Header(page: String,premium: Boolean,go: (String)->Unit) {
 BoxWithConstraints(Modifier.fillMaxWidth()) {
  val wide=maxWidth>650.dp
  Row(Modifier.fillMaxWidth().padding(horizontal=if(wide)36.dp else 12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
   TextButton(onClick={go("home")}) { Text("✿",fontSize=28.sp);Spacer(Modifier.width(8.dp));Text(stringResource(R.string.app_name),fontFamily=FontFamily.Serif,fontSize=25.sp,fontWeight=FontWeight.Bold) }
   Spacer(Modifier.weight(1f))
   if(wide) { TextButton(onClick={go("worlds")}){Text(stringResource(R.string.ui_006))};TextButton(onClick={go("garden")}){Text(stringResource(R.string.garden))};TextButton(onClick={go("settings")}){Text(stringResource(R.string.settings))} }
   else { TextButton(onClick={go(if(page=="settings")"home" else "settings")}){ Text(if(page=="settings")"Home" else "Settings") } }
   if(wide) OutlinedButton(onClick={go("premium")}) { Text(if(premium)"Premium ✓" else "Explore premium") }
  }
 }
 HorizontalDivider(color=Sage.copy(alpha=.15f))
}
@Composable private fun Eyebrow(text: String) { Text(text.uppercase(),fontSize=11.sp,letterSpacing=2.sp,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.primary) }
@Composable private fun Title(text: String,size: Int=38) { Text(text,fontFamily=FontFamily.Serif,fontSize=size.sp,lineHeight=(size+5).sp) }
@Composable private fun Home(s: GameState,play: ()->Unit,daily: ()->Unit,go: (String)->Unit) {
 Column(Modifier.widthIn(max=1200.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
  BoxWithConstraints {
   val wide=maxWidth>700.dp
   val intro: @Composable ()->Unit = {
    Column(Modifier.padding(if(wide)24.dp else 4.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
     Eyebrow(stringResource(R.string.ui_007))
     Title(stringResource(R.string.ui_008),if(wide)52 else 38)
     Text(stringResource(R.string.ui_009),fontSize=16.sp,lineHeight=25.sp)
     Button(onClick=play,contentPadding=PaddingValues(28.dp,16.dp),shape=RoundedCornerShape(16.dp)) { Text("${if(s.completed.isEmpty())"Begin your journey" else "Continue your journey"}  →",fontSize=16.sp) }
     Text(stringResource(R.string.ui_010),fontSize=10.sp,letterSpacing=1.sp,color=MaterialTheme.colorScheme.primary)
    }
   }
   if(wide) Row(verticalAlignment=Alignment.CenterVertically) { Box(Modifier.weight(1f)){intro()};Box(Modifier.weight(1.1f).height(360.dp).clip(RoundedCornerShape(28.dp))){GardenArt(0,6);Surface(Modifier.align(Alignment.BottomStart).padding(20.dp),color=Cream.copy(alpha=.9f),shape=RoundedCornerShape(16.dp)){Text(stringResource(R.string.ui_011),Modifier.padding(16.dp),color=Pine,fontFamily=FontFamily.Serif,fontSize=18.sp)}} }
   else Column { intro();Spacer(Modifier.height(20.dp));Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp))){GardenArt(0,6)} }
  }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) { Box(Modifier.weight(1f)) { Title(stringResource(R.string.ui_012),24) };Text("✿  ${s.completed.size} ${if(s.completed.size==1)"bloom" else "blooms"}",fontSize=13.sp) }
  FlowRow(horizontalArrangement=Arrangement.spacedBy(14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
   HomeCard("01 / A FRESH LITTLE CHALLENGE","Your daily word escape",LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),"Play today  →",daily)
   HomeCard("02 / ONE WORD AT A TIME","A world waiting to grow","Six nature-inspired chapters","Explore the journey  →",{go("worlds")})
   HomeCard("03 / YOUR OWN QUIET CORNER","Watch your garden bloom","Every completed puzzle plants a flower","Visit my garden  →",{go("garden")})
  }
  Text(stringResource(R.string.ui_013),fontFamily=FontFamily.Serif,fontSize=16.sp,color=MaterialTheme.colorScheme.primary)
 }
}
@Composable private fun HomeCard(label: String,title: String,description: String,action: String,click: ()->Unit) {
 Surface(Modifier.widthIn(max=360.dp).defaultMinSize(minWidth=260.dp).clickable(onClick=click),shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface, border=BorderStroke(1.dp,Sage.copy(alpha=.2f))) {
  Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) { Eyebrow(label);Title(title,24);Text(description,fontSize=13.sp);Text(action,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.SemiBold,fontSize=14.sp) }
 }
}
@Composable private fun GameScreen(s: GameState,settings: Settings,voiceText: String,listening: Boolean,available: Boolean,submit: (String)->Unit,shuffle: ()->Unit,hint: (Boolean)->Unit,startHint: ()->Unit,next: ()->Unit,replay: ()->Unit,mic: ()->Unit,say: ()->Unit) {
 val p=s.puzzle?:return
 var typed by remember(p.id) { mutableStateOf("") }
 val done=p.answers.all { it in s.found }
 val compact=LocalConfiguration.current.screenHeightDp<800 && LocalConfiguration.current.screenWidthDp<700
 Column(Modifier.widthIn(max=1200.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp,vertical=if(compact)12.dp else 16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(if(compact)8.dp else 12.dp)) {
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
   Column(Modifier.weight(1f)) { Eyebrow(if(p.id.startsWith("daily"))"Daily word escape" else "${Library.worlds[p.world]} · Level ${Library.puzzles.indexOfFirst { it.id==p.id }+1}");Title(if(done)"A beautiful discovery" else "Let your words bloom",if(compact)24 else 28) }
   Text("✦ ${s.hints} hints",fontWeight=FontWeight.SemiBold)
  }
  LinearProgressIndicator(progress={s.found.size.toFloat()/p.answers.size},modifier=Modifier.fillMaxWidth().height(5.dp).clip(CircleShape))
  BoxWithConstraints(Modifier.fillMaxWidth()) {
   val wide=maxWidth>700.dp
   val board: @Composable ()->Unit = {
    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(if(compact)8.dp else 12.dp)) {
     Box(Modifier.fillMaxWidth().height(if(wide)330.dp else if(compact)(p.height*24).dp.coerceIn(100.dp,220.dp) else 220.dp).clip(RoundedCornerShape(24.dp))) { GardenArt(p.world,s.found.size+2,.28f);CrosswordBoard(p,s,settings,Modifier.fillMaxSize().padding(20.dp));BloomCelebration(done,settings.reduced,Modifier.matchParentSize()) }
     Text("${s.found.size} of ${p.answers.size} words found  ·  ${p.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}",fontSize=13.sp)
     if(s.bonus.isNotEmpty()) Text("Bonus words: ${s.bonus.joinToString(" · ")}",fontSize=13.sp)
    }
   }
   val controls: @Composable ()->Unit = {
    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
     if(done) {
      Box(Modifier.size(210.dp),contentAlignment=Alignment.Center) { GardenArt(p.world,6);Text("✿",fontSize=76.sp,color=Pine) }
      Title(stringResource(R.string.ui_014),24)
      Button(onClick=next,contentPadding=PaddingValues(25.dp,14.dp)) { Text(if(p.id.startsWith("daily"))"Back to the garden" else "Next puzzle  →") }
      if(!p.id.startsWith("daily"))TextButton(onClick=replay){Text("Replay puzzle")}
     } else {
      LetterWheel(p,s.revision,settings,s.highlighted,submit)
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
       OutlinedButton(onClick=shuffle){Text(stringResource(R.string.ui_015))}
       OutlinedButton(onClick={hint(false)}){Text(stringResource(R.string.ui_016))}
      }
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
       TextButton(onClick=startHint){Text(stringResource(R.string.ui_017))};TextButton(onClick={hint(true)}){Text(stringResource(R.string.ui_018))}
      }
     }
    }
   }
   if(wide) Row(horizontalArrangement=Arrangement.spacedBy(32.dp),verticalAlignment=Alignment.CenterVertically) { Box(Modifier.weight(1.2f)){board()};Box(Modifier.weight(1f)){controls()} }
   else Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(if(compact)6.dp else 10.dp)) {board();controls()}
  }
  Text(s.message,modifier=Modifier.semantics { liveRegion=LiveRegionMode.Polite },textAlign=TextAlign.Center,fontSize=15.sp,color=MaterialTheme.colorScheme.primary)
  if(!done) {
   Row(Modifier.widthIn(max=500.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
    OutlinedTextField(value=typed,onValueChange={typed=it.take(24)},modifier=Modifier.weight(1f),label={Text(stringResource(R.string.ui_019))},singleLine=true,shape=RoundedCornerShape(16.dp))
    Button(onClick={submit(typed);typed=""},enabled=typed.isNotBlank()){Text(stringResource(R.string.ui_020))}
   }
  }
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   OutlinedButton(onClick=mic){Text(if(listening)"Stop listening" else if(available)"◉ Speak" else "Voice availability")}
   TextButton(onClick=say){Text(stringResource(R.string.ui_021))}
  }
  if(voiceText.isNotBlank())Text(voiceText,textAlign=TextAlign.Center,fontSize=13.sp,modifier=Modifier.semantics { liveRegion=LiveRegionMode.Polite })
  Text(stringResource(R.string.ui_022),fontSize=11.sp,textAlign=TextAlign.Center)
 }
}
@Composable private fun CrosswordBoard(p: Puzzle,s: GameState,settings: Settings,modifier: Modifier) {
 val revealed=p.entries.filter { it.word in s.found }.flatMap { it.cells().keys }.toSet()
 BoxWithConstraints(modifier,contentAlignment=Alignment.Center) {
  val cell=minOf(maxWidth/p.width,maxHeight/p.height,48.dp)
  Column(verticalArrangement=Arrangement.spacedBy(3.dp),horizontalAlignment=Alignment.CenterHorizontally) {
   repeat(p.height) { y -> Row(horizontalArrangement=Arrangement.spacedBy(3.dp)) {
    repeat(p.width) { x ->
     val c=p.cells[x to y];val visible=(x to y) in revealed || "$x,$y" in s.revealed
     if(c==null)Spacer(Modifier.size((cell-3.dp).coerceAtLeast(4.dp)))
     else Box(Modifier.size((cell-3.dp).coerceAtLeast(4.dp)).clip(RoundedCornerShape(7.dp)).background(if(visible)Pine else Color.White.copy(alpha=.94f)).border(1.dp,if(settings.contrast)Pine else Sage.copy(alpha=.22f),RoundedCornerShape(7.dp)).semantics { contentDescription=if(visible)"Row ${y+1}, column ${x+1}: $c" else "Row ${y+1}, column ${x+1}: empty letter" },contentAlignment=Alignment.Center) {
      Text(if(visible)c.toString() else "",fontSize=if(cell<28.dp)14.sp else 22.sp,fontWeight=FontWeight.SemiBold,color=if(visible)Cream else Pine)
     }
    }
   } }
  }
 }
}
@Composable private fun LetterWheel(p: Puzzle,revision: Int,settings: Settings,highlighted: Char?,submit: (String)->Unit) {
 var letters by remember(p.id) { mutableStateOf(p.letters.toList()) }
 var chosen by remember(p.id) { mutableStateOf(emptyList<Int>()) }
 LaunchedEffect(revision) { if(revision>0)letters=letters.shuffled(kotlin.random.Random(revision));chosen=emptyList() }
 val word=chosen.joinToString(""){letters[it].toString()}
 fun add(i: Int) { chosen=if(chosen.size>1 && chosen[chosen.lastIndex-1]==i)chosen.dropLast(1) else if(i !in chosen)chosen+i else chosen }
 Column(horizontalAlignment=Alignment.CenterHorizontally) {
  Text(word.ifEmpty { "Connect the letters" },fontSize=if(word.isEmpty())14.sp else 23.sp,letterSpacing=if(word.isEmpty())0.sp else 3.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.height(34.dp).semantics { liveRegion=LiveRegionMode.Polite })
  val density=LocalDensity.current
  val wheelSize=if(LocalConfiguration.current.screenHeightDp<800 && LocalConfiguration.current.screenWidthDp<700)208.dp else 272.dp
  val side=with(density){wheelSize.toPx()};val center=side/2;val radius=side*.365f;val tile=50.dp
  val points=letters.indices.map { val a=2*PI*it/letters.size-PI/2;Offset(center+cos(a).toFloat()*radius,center+sin(a).toFloat()*radius) }
  val origin=remember(p.id) { arrayOf(Offset.Zero) }
  Box(Modifier.size(wheelSize).pointerInput(p.id,letters) {
    // Observe the original down without consuming taps or scrolling.
    awaitEachGesture {
     val down=awaitFirstDown(requireUnconsumed=false,pass=PointerEventPass.Initial)
     origin[0]=down.position
     waitForUpOrCancellation(pass=PointerEventPass.Initial)
    }
   }.pointerInput(p.id,letters) {
    var previous=Offset.Zero
    fun hit(pos: Offset): Int?=points.indices.minByOrNull { (points[it]-pos).getDistance() }?.takeIf { (points[it]-pos).getDistance()<side*.12f }
    detectDragGestures(onDragStart={previous=origin[0];chosen=emptyList();hit(previous)?.let { index->add(index) }},onDrag={change,_->
     change.consume()
     val trace=TileTrace().also { it.indices.addAll(chosen) }
     trace.trace(TracePoint(previous.x,previous.y),TracePoint(change.position.x,change.position.y),points.map { TracePoint(it.x,it.y) },side*.12f)
     chosen=trace.indices.toList();previous=change.position
    },onDragEnd={ val text=chosen.joinToString(""){letters[it].toString()};if(text.isNotEmpty())submit(text);chosen=emptyList() },onDragCancel={chosen=emptyList()})
   }) {
   Canvas(Modifier.fillMaxSize()) {
    drawCircle(Sage.copy(alpha=.08f),radius=side*.49f)
    drawCircle(Sage.copy(alpha=.17f),radius=radius,style=Stroke(1.dp.toPx()))
    chosen.zipWithNext().forEach { (a,b)->drawLine(Pine.copy(alpha=.5f),points[a],points[b],12.dp.toPx(),StrokeCap.Round) }
   }
   points.forEachIndexed { i,pos ->
    val selected=i in chosen
    val tileColour by animateColorAsState(if(selected)Pine else if(highlighted==letters[i])Gold else if(settings.contrast)Color.White else Color(0xFFF1E6D4),tween(if(settings.reduced)0 else 160),label="tile colour")
    Box(Modifier.offset { IntOffset((pos.x-with(density){tile.toPx()}/2).roundToInt(),(pos.y-with(density){tile.toPx()}/2).roundToInt()) }.size(tile).clip(CircleShape).background(tileColour).border(if(selected)2.dp else 1.dp,if(settings.contrast)Pine else Sage.copy(alpha=.3f),CircleShape).clickable { add(i) }.semantics { contentDescription="Letter ${letters[i]}, tile ${i+1}${if(selected)", selected" else ""}";role=Role.Button },contentAlignment=Alignment.Center) {
     Text(letters[i].toString(),fontSize=25.sp,color=if(selected)Cream else Pine,fontFamily=FontFamily.Serif,fontWeight=FontWeight.Bold)
    }
   }
   Text("✿",modifier=Modifier.align(Alignment.Center),fontSize=26.sp,color=Sage)
  }
  Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
   TextButton(onClick={chosen=chosen.dropLast(1)},enabled=chosen.isNotEmpty()){Text(stringResource(R.string.ui_023))}
   TextButton(onClick={chosen=emptyList()},enabled=chosen.isNotEmpty()){Text(stringResource(R.string.ui_024))}
   TextButton(onClick={submit(word);chosen=emptyList()},enabled=chosen.isNotEmpty()){Text(stringResource(R.string.ui_025))}
  }
 }
}
@Composable private fun Worlds(s: GameState,premium: Boolean,play: (Int)->Unit,unlock: ()->Unit) {
 var difficulty by remember { mutableStateOf<Difficulty?>(null) }
 Column(Modifier.widthIn(max=1100.dp).verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
  Eyebrow(stringResource(R.string.ui_006));Title(stringResource(R.string.ui_026))
  Text(stringResource(R.string.ui_027))
  FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { FilterChip(selected=difficulty==null,onClick={difficulty=null},label={Text(stringResource(R.string.ui_028))});Difficulty.entries.forEach { d->FilterChip(selected=difficulty==d,onClick={difficulty=d},label={Text(d.name.lowercase().replaceFirstChar { it.uppercase() })}) } }
  Library.worlds.forEachIndexed { world,name ->
   val indices=(world*4 until world*4+4).filter { difficulty==null || Library.puzzles[it].difficulty==difficulty }
   if(indices.isNotEmpty()) Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surface) {
    Column {
     Box(Modifier.fillMaxWidth().height(135.dp)) { GardenArt(world,s.completed.count { it in Library.puzzles.filter { it.world==world }.map { it.id } });Surface(Modifier.align(Alignment.BottomStart).padding(12.dp),shape=RoundedCornerShape(10.dp),color=Cream){Text("${world+1}. $name",Modifier.padding(10.dp),fontFamily=FontFamily.Serif,color=Pine,fontSize=20.sp)} }
     FlowRow(Modifier.padding(14.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
      indices.forEach { i->val completed=Library.puzzles[i].id in s.completed;val allowed=i==0 || Library.puzzles[i-1].id in s.completed || completed
       OutlinedButton(onClick={if(world>0 && !premium)unlock() else play(i)},enabled=allowed || (world>0 && !premium)) { Text("${if(completed)"✓" else if(!allowed || (world>0 && !premium))"◇" else "✿"}  ${i+1}") }
      }
     }
    }
   }
  }
 }
}
@Composable private fun GardenScreen(s: GameState,premium: Boolean=false) {
 val complete=s.completed.count { !it.startsWith("daily") }
 val blooms=s.completed.size
 val dailyCount=s.completed.count { it.startsWith("daily") }
 Column(Modifier.widthIn(max=1050.dp).verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
  Eyebrow(stringResource(R.string.ui_029));Title(stringResource(R.string.ui_030))
  Box(Modifier.fillMaxWidth().height(340.dp).clip(RoundedCornerShape(28.dp))){GardenArt((complete/4).coerceAtMost(5),blooms)}
  Title("$blooms ${if(blooms==1)"bloom" else "blooms"} planted",30)
  Text("${complete/4} of 6 chapters restored · $dailyCount daily ${if(dailyCount==1)"escape" else "escapes"} completed")
  FlowRow(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   listOf(1 to "First bloom",4 to "Garden keeper",12 to "Wildflower soul",24 to "Full bloom").forEach { (count,label)->Surface(shape=RoundedCornerShape(18.dp),color=if(blooms>=count)Peach else Sage.copy(alpha=.12f)){Column(Modifier.padding(22.dp)){Text(if(blooms>=count)"✿" else "◇",fontSize=30.sp,color=Pine);Text(label,fontFamily=FontFamily.Serif,color=if(blooms>=count)Pine else MaterialTheme.colorScheme.onSurface);Text(if(count==1)"1 puzzle" else "$count puzzles",fontSize=12.sp,color=if(blooms>=count)Pine else MaterialTheme.colorScheme.onSurface)}} }
  }
  if(premium) {
   Title("Your garden in numbers",24)
   Text("${s.completed.size*3} words in completed puzzles · ${((complete/24f)*100).toInt()}% of the journey restored")
   FlowRow(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
    Difficulty.entries.forEach { difficulty ->
     val solved=Library.puzzles.count { it.difficulty==difficulty && it.id in s.completed }
     Surface(shape=RoundedCornerShape(14.dp),color=Sage.copy(alpha=.12f)) {
      Column(Modifier.padding(16.dp)) { Text("$solved / 6",fontSize=23.sp,fontFamily=FontFamily.Serif);Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() },fontSize=13.sp) }
     }
    }
   }
  }
  Title(stringResource(R.string.ui_031),24)
  val days=s.completed.filter { it.startsWith("daily") }.sortedDescending()
  Text(if(days.isEmpty())"Your first daily escape is waiting. Play whenever you like — no streak to protect." else days.joinToString("\n") { it.substringAfterLast(':') })
 }
}
@Composable private fun SettingsScreen(settings: Settings,recognition: Boolean,set: (String,Boolean)->Unit,language: (String)->Unit,scroll: ScrollState = rememberScrollState()) {
 Column(Modifier.widthIn(max=760.dp).verticalScroll(scroll).padding(24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
  Eyebrow(stringResource(R.string.ui_032));Title(stringResource(R.string.ui_033))
  listOf(Triple("narration","Spoken guidance",settings.narration),Triple("effects","Gentle word sounds",settings.effects),Triple("music","Garden ambience",settings.music),Triple("contrast","High-contrast letter tiles",settings.contrast),Triple("reduced","Reduce motion",settings.reduced),Triple("large","Larger text",settings.large),Triple("dark","Evening theme",settings.dark)).forEach { (key,title,value) ->
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text(title,Modifier.weight(1f));Switch(checked=value,onCheckedChange={set(key,it)},modifier=Modifier.semantics { contentDescription=title }) }
  }
  HorizontalDivider();Title(stringResource(R.string.ui_034),24)
  Row { listOf("en-GB" to "English UK","en-US" to "English US").forEach { (tag,name)->FilterChip(selected=settings.language==tag,onClick={language(tag)},label={Text(name)}) } }
  Text(stringResource(R.string.ui_035),fontSize=13.sp)
  Title(stringResource(R.string.ui_036),24)
  Text(if(recognition)"A compatible recognition service is available. Use Speak in a puzzle; your device’s service may send speech to its provider." else "No compatible recognition service was found. Swipe, tap and text entry provide the full game.")
  Text(stringResource(R.string.ui_037),fontSize=14.sp)
  HorizontalDivider();Title(stringResource(R.string.ui_038),24)
  Text(stringResource(R.string.ui_039),fontSize=14.sp,lineHeight=22.sp)
  Text(stringResource(R.string.ui_040),fontSize=12.sp)
 }
}
@Composable private fun Premium(store: BillingState,purchase: ()->Unit,restore: ()->Unit) {
 Column(Modifier.widthIn(max=760.dp).verticalScroll(rememberScrollState()).padding(28.dp),verticalArrangement=Arrangement.spacedBy(20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
  Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(28.dp))){GardenArt(5,12)}
  Eyebrow(stringResource(R.string.ui_041));Title(if(store.premium)"The whole garden is yours." else "Let the whole garden bloom.")
  Text(stringResource(R.string.ui_042),textAlign=TextAlign.Center,lineHeight=24.sp)
  Text(stringResource(R.string.ui_043),fontWeight=FontWeight.SemiBold)
  if(!store.premium) Button(onClick=purchase,enabled=store.price!=null,contentPadding=PaddingValues(28.dp,16.dp)){Text(store.price?.let { "Unlock premium · $it" }?:"Store price unavailable")}
  OutlinedButton(onClick=restore){Text(stringResource(R.string.ui_044))}
  Text(store.message,textAlign=TextAlign.Center,modifier=Modifier.semantics { liveRegion=LiveRegionMode.Polite })
  Text(stringResource(R.string.ui_045),fontSize=13.sp,textAlign=TextAlign.Center)
 }
}

/** Original vector landscape, drawn locally at any resolution. No image downloads. */
@Composable fun GardenArt(world: Int,blooms: Int,opacity: Float=1f) {
 val palettes=listOf(listOf(0xFFF4D8B0,0xFFF6EDD5,0xFFACB890,0xFF658A74),listOf(0xFFBAC8B7,0xFFE3E4C7,0xFF789784,0xFF345E51),listOf(0xFFEBD0D4,0xFFFFE9DC,0xFFC6ACAA,0xFF8D9B7A),listOf(0xFFBFD8DB,0xFFECEDD7,0xFF8DB5B0,0xFF4D8F99),listOf(0xFFF0CE95,0xFFFFE7B2,0xFFC5B778,0xFF86986A),listOf(0xFF263D53,0xFF6A758E,0xFF536D79,0xFF264C50))
 val pal=palettes[world.coerceIn(0,5)].map { Color(it) }
 Canvas(Modifier.fillMaxSize().semantics { contentDescription="Original illustrated ${Library.worlds[world.coerceIn(0,5)]}, $blooms flowers" }) {
  val w=size.width;val h=size.height
  drawRect(Brush.verticalGradient(listOf(pal[0],pal[1])),alpha=opacity)
  drawCircle(if(world==5)Cream else Color(0xFFFFF1CF),w*.065f,Offset(w*.77f,h*.23f),alpha=opacity)
  fun hill(y: Float,c: Color,offset: Float) { val path=Path().apply { moveTo(0f,h*y);cubicTo(w*.22f,h*(y-offset),w*.48f,h*(y+offset),w,h*(y-.08f));lineTo(w,h);lineTo(0f,h);close() };drawPath(path,c,alpha=opacity) }
  hill(.54f,pal[2],.25f);hill(.7f,pal[3],.18f)
  if(world==3)drawOval(Color(0xFFADD2CE),Offset(w*.22f,h*.63f),Size(w*.64f,h*.24f),alpha=opacity)
  // Warm curving garden path.
  val path=Path().apply { moveTo(w*.54f,h*.58f);cubicTo(w*.25f,h*.72f,w*.85f,h*.79f,w*.57f,h);lineTo(w*.82f,h);cubicTo(w*1.03f,h*.78f,w*.37f,h*.7f,w*.56f,h*.58f);close() }
  drawPath(path,Color(0xFFE6D2AE),alpha=.8f*opacity)
  // Silhouetted trees, fern-like leaves and an ornamental garden arch.
  for(i in 0..4) {
   val x=w*(.06f+i*.095f);val y=h*(.61f+(i%2)*.06f);val r=h*(.12f+(i%3)*.018f)
   drawLine(pal[3],Offset(x,y),Offset(x,y-r*1.45f),w*.008f,alpha=opacity)
   drawOval(if(world==2)Color(0xFFD6A9B1) else pal[2].copy(red=(pal[2].red*.8f)),Offset(x-r*.42f,y-r*2),Size(r*.84f,r*1.5f),alpha=opacity)
  }
  val arch=Path().apply { moveTo(w*.61f,h*.66f);lineTo(w*.61f,h*.43f);cubicTo(w*.61f,h*.24f,w*.78f,h*.24f,w*.78f,h*.43f);lineTo(w*.78f,h*.66f) }
  drawPath(arch,Color(0xFFEEE3C9),style=Stroke(w*.013f),alpha=opacity)
  if(world==1) {
   repeat(6) { i ->
    val x=w*(if(i<3).04f+i*.1f else .79f+(i-3)*.09f);val y=h*(.65f+(i%2)*.04f)
    val tall=h*(.3f+(i%3)*.04f)
    drawLine(Color(0xFF284E42),Offset(x,y),Offset(x,y-tall),w*.008f,alpha=opacity)
    repeat(3) { tier -> val top=y-tall-tier*h*.025f;val bottom=y-h*.05f-tier*h*.09f
     val pine=Path().apply { moveTo(x,top);lineTo(x-w*.065f,bottom);lineTo(x+w*.065f,bottom);close() }
     drawPath(pine,if(tier==0)Color(0xFF345E51) else Color(0xFF476F5B),alpha=opacity)
    }
   }
  }
  if(world==2) {
   listOf(.13f,.91f).forEach { fraction ->
    val x=w*fraction;val y=h*.48f
    drawLine(Color(0xFF8D7567),Offset(x,h*.77f),Offset(x,y-h*.15f),w*.01f,alpha=opacity)
    repeat(7) { petal -> val a=petal*2*PI/7;drawCircle(if(petal%2==0)Color(0xFFE6B5BB) else Color(0xFFF2C4CA),h*.08f,Offset(x+cos(a).toFloat()*w*.06f,y+sin(a).toFloat()*h*.06f),alpha=opacity) }
   }
  }
  if(world==3) {
   repeat(3) { i -> drawLine(Cream.copy(alpha=.55f),Offset(w*(.3f+i*.04f),h*(.71f+i*.035f)),Offset(w*(.66f+i*.01f),h*(.71f+i*.035f)),h*.004f) }
  }
  if(world==4) {
   repeat(30) { i -> val x=w*((i*41%100)/100f);val y=h*(.88f+(i%4)*.035f);val top=y-h*.15f
    drawLine(Color(0xFFB4914E),Offset(x,y),Offset(x,top),w*.0015f,alpha=opacity)
    repeat(4) { grain -> drawOval(Gold,Offset(x+if(grain%2==0)0f else -w*.008f,top+grain*h*.017f),Size(w*.009f,h*.025f),alpha=opacity) }
   }
  }
  if(world==5)drawCircle(pal[0],w*.06f,Offset(w*.8f,h*.21f),alpha=opacity)
  val rng=kotlin.random.Random(185+world)
  repeat(65) { i ->
   val x=rng.nextFloat()*w;val y=h*(.74f+rng.nextFloat()*.26f);val stem=h*(.035f+rng.nextFloat()*.08f)
   drawLine(Color(0xFF315B45),Offset(x,y),Offset(x,y-stem),w*.002f,alpha=opacity)
   drawOval(Color(0xFF8BA37A),Offset(x,y-stem*.7f),Size(w*.018f,h*.018f),alpha=opacity)
   if(i<min(blooms*5,65)) {
    val r=w*(.004f+rng.nextFloat()*.005f);val colour=if(i%3==0)Peach else if(i%3==1)Gold else Cream
    repeat(5) { petal->val a=petal*2*PI/5;drawCircle(colour,r,Offset(x+cos(a).toFloat()*r,y-stem+sin(a).toFloat()*r),alpha=opacity) }
    drawCircle(Color(0xFFD5A854),r*.65f,Offset(x,y-stem),alpha=opacity)
   }
  }
  if(world==5)repeat(22) { val rng2=kotlin.random.Random(it);drawCircle(Cream,1.5f,Offset(rng2.nextFloat()*w,rng2.nextFloat()*h*.4f),alpha=.7f*opacity) }
 }
}

/** Render-only entry point exercising the same production Composables without device services. */
@Composable internal fun VisualScreen(page: String, s: GameState, settings: Settings = Settings()) {
 MaterialTheme(colorScheme=lightColorScheme(primary=Pine,background=Cream,surface=Color.White,onSurface=Pine,onBackground=Pine)) {
  Surface(Modifier.fillMaxSize(),color=Cream) {
   Column {
    Header(page,false) {}
    Box(Modifier.weight(1f),contentAlignment=Alignment.TopCenter) {
     when(page) {
      "home" -> Home(s,{}, {}, {})
      "game" -> GameScreen(s,settings,"",false,false,{}, {}, {}, {}, {}, {}, {}, {})
      "worlds" -> Worlds(s,false,{}, {})
      "garden" -> GardenScreen(s)
      "settings" -> SettingsScreen(settings,false,{ _,_ -> },{})
      "voice" -> SettingsScreen(settings,false,{ _,_ -> },{},rememberScrollState(with(LocalDensity.current){360.dp.roundToPx()}))
      "english" -> SettingsScreen(settings,false,{ _,_ -> },{},rememberScrollState(with(LocalDensity.current){200.dp.roundToPx()}))
      else -> Premium(BillingState(message="Amazon product availability depends on device"),{}, {})
     }
    }
   }
  }
 }
}

@Composable private fun BloomCelebration(complete: Boolean,reduced: Boolean,modifier: Modifier) {
 val burst=remember { Animatable(1f) }
 LaunchedEffect(complete,reduced) {
  if(complete && !reduced) { burst.snapTo(0f);burst.animateTo(1f,tween(1300,easing=LinearOutSlowInEasing)) }
  else burst.snapTo(1f)
 }
 Canvas(modifier.clearAndSetSemantics {}) {
  val t=burst.value
  if(t<1f)repeat(24) { i ->
   val a=i*2*PI/24;val travel=size.minDimension*.48f*t
   val x=center.x+cos(a).toFloat()*travel
   val y=center.y+sin(a).toFloat()*travel-size.height*.28f*t+size.height*.3f*t*t
   drawCircle(if(i%3==0)Peach else if(i%3==1)Gold else Sage,(3f+i%3)*(1f-t),Offset(x,y),alpha=1f-t)
  }
 }
}
