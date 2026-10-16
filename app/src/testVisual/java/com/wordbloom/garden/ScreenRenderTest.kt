package com.wordbloom.garden
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.detectEnvironment
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.wordbloom.engine.Library
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
@RunWith(Parameterized::class)
class ScreenRenderTest(private val width: Int,private val height: Int,private val scale: Density,private val variant: String) {
 @get:Rule val paparazzi=Paparazzi(environment=detectEnvironment().copy(compileSdkVersion=34),deviceConfig=DeviceConfig.PIXEL_5.copy(screenWidth=width,screenHeight=height,orientation=if(variant=="tablet")ScreenOrientation.LANDSCAPE else ScreenOrientation.PORTRAIT,softButtons=false,density=scale),theme="android:Theme.Material.Light.NoActionBar",useDeviceResolution=true)
 @Test fun screens() {
  val puzzle=Library.puzzles.first()
  val initial=GameState(ready=true,puzzle=puzzle)
  for(page in listOf("home","game","worlds","garden","settings","voice","english"))paparazzi.snapshot(name="$variant-$page") { VisualScreen(page,if(page=="garden")initial.copy(completed=Library.puzzles.take(4).map { it.id }.toSet()) else initial) }
  paparazzi.snapshot(name="$variant-daily") { VisualScreen("game",initial.copy(puzzle=Library.daily(java.time.LocalDate.of(2026,10,16),"en-GB"))) }
  paparazzi.snapshot(name="$variant-daily-garden") { VisualScreen("garden",initial.copy(completed=setOf("daily:en-GB:2026-10-16"))) }
  paparazzi.snapshot(name="$variant-crossword") { VisualScreen("game",initial.copy(found=setOf("CAT"),message="Beautiful! CAT")) }
 }
 companion object {
  @JvmStatic @Parameterized.Parameters(name="{3}") fun devices(): List<Array<Any>> = listOf(
   arrayOf(1280,800,Density.MEDIUM,"tablet"),arrayOf(800,1280,Density.XHIGH,"phone")
  )
 }
}
