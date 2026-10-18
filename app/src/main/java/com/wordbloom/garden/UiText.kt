package com.wordbloom.garden

import android.content.Context
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.wordbloom.engine.MessageTranslator
import org.json.JSONObject

internal val LocalUiLanguage=staticCompositionLocalOf { "en-GB" }
internal val LocalUiCatalog=staticCompositionLocalOf<UiCatalog?> { null }
internal class UiCatalog(private val context: Context) {
 private val translators=mutableMapOf<String,MessageTranslator>()
 fun text(source: String,language: String): String {
  if(language.startsWith("en"))return source
  return translators.getOrPut(language) {
   val entries=context.assets.open("localization/$language.json").bufferedReader().use { JSONObject(it.readText()).getJSONObject("entries") }
   MessageTranslator(entries.keys().asSequence().map { id -> entries.getJSONObject(id).let { it.getString("source") to it.getString("translation") } }.toList())
  }.text(source)
 }
}
@Composable internal fun rememberUiLabel(): (String)->String {
 val context=LocalContext.current;val language=LocalUiLanguage.current
 val fallback=remember(context.applicationContext) { UiCatalog(context.applicationContext) }
 val catalog=LocalUiCatalog.current?:fallback
 return remember(catalog,language) { { source: String -> catalog.text(source,language) } }
}
@Composable internal fun UiText(
 text: String,modifier: Modifier=Modifier,color: Color=Color.Unspecified,fontSize: TextUnit=TextUnit.Unspecified,
 fontFamily: FontFamily?=null,fontWeight: FontWeight?=null,letterSpacing: TextUnit=TextUnit.Unspecified,
 textAlign: TextAlign?=null,lineHeight: TextUnit=TextUnit.Unspecified,style: TextStyle=LocalTextStyle.current
) {
 val context=LocalContext.current
 val fallback=remember(context.applicationContext) { UiCatalog(context.applicationContext) }
 val catalog=LocalUiCatalog.current?:fallback
 androidx.compose.material3.Text(catalog.text(text,LocalUiLanguage.current),modifier,color,fontSize,fontWeight=fontWeight,
  fontFamily=fontFamily,letterSpacing=letterSpacing,textAlign=textAlign,lineHeight=lineHeight,style=style)
}
