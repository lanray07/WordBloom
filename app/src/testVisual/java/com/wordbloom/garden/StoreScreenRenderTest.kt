package com.wordbloom.garden

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.wordbloom.engine.Library
import org.junit.Rule
import org.junit.Test

/** Each image uses an independent renderer and the production screen tree. */
class StoreScreenRenderTest {
 @get:Rule val paparazzi = Paparazzi(
  environment=detectEnvironment().copy(compileSdkVersion=34),
  deviceConfig=DeviceConfig.PIXEL_5.copy(screenWidth=1280,screenHeight=800,
   orientation=ScreenOrientation.LANDSCAPE,softButtons=false,density=Density.MEDIUM),
  theme="android:Theme.Material.Light.NoActionBar",useDeviceResolution=true)

 @Test fun connect() {
  paparazzi.snapshot(name="store-connect") {
   VisualScreen("game",GameState(ready=true,puzzle=Library.puzzles.first(),
    found=setOf("CAT"),message="Beautiful! CAT"))
  }
 }
 @Test fun journey() {
  paparazzi.snapshot(name="store-journey") {
   VisualScreen("worlds",GameState(ready=true,puzzle=Library.puzzles.first()))
  }
 }
 @Test fun garden() {
  paparazzi.snapshot(name="store-garden") {
   VisualScreen("garden",GameState(ready=true,puzzle=Library.puzzles.first(),
    completed=Library.puzzles.take(4).map { it.id }.toSet()))
  }
 }
}
