package com.wordbloom.garden

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.speech.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class Voice(private val activity: Activity,private val result: (String)->Unit,private val status: (String,Boolean)->Unit): RecognitionListener {
 private var recognizer: SpeechRecognizer?=null
 private var ready=false
 private var tts: TextToSpeech?=null
 private val audio=activity.getSystemService(AudioManager::class.java)
 private var focus: AudioFocusRequest?=null
 val available get()=SpeechRecognizer.isRecognitionAvailable(activity)
 var locale: Locale=Locale.UK
 init { tts=TextToSpeech(activity) { code -> ready=code==TextToSpeech.SUCCESS }
  tts?.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
   override fun onStart(id: String?) {}
   override fun onDone(id: String?) { releaseFocus() }
   @Deprecated("Platform callback") override fun onError(id: String?) { releaseFocus() }
  })
 }
 private fun releaseFocus() { focus?.let { audio.abandonAudioFocusRequest(it) };focus=null }

 fun say(text: String) {
  if(!ready)return
  releaseFocus()
  focus=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()).setOnAudioFocusChangeListener { if(it==AudioManager.AUDIOFOCUS_LOSS)tts?.stop() }.build()
  if(audio.requestAudioFocus(focus!!)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED)return
  tts?.language=locale
  tts?.speak(text,TextToSpeech.QUEUE_FLUSH,null,"wordbloom")
 }
 fun listen() {
  if(!available) { status("Speech recognition unavailable. You can swipe, tap, or type words.",false);return }
  if(activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) { status("Microphone permission is needed for push-to-talk only.",false);return }
  tts?.stop()
  try {
   focus=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()).setOnAudioFocusChangeListener { change -> if(change==AudioManager.AUDIOFOCUS_LOSS)stop() }.build()
   if(audio.requestAudioFocus(focus!!)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { status("Audio is busy. Try again shortly.",false);return }
   recognizer?.destroy();recognizer=SpeechRecognizer.createSpeechRecognizer(activity).also { it.setRecognitionListener(this) }
   status("Listening…",true)
   recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,locale.toLanguageTag()).putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1))
  } catch(e: Exception) { stop();status("Speech service could not start. Touch controls remain available.",false) }
 }
 fun stop() { recognizer?.cancel();tts?.stop();releaseFocus();status("",false) }
 fun close() { stop();recognizer?.destroy();tts?.stop();tts?.shutdown() }
 override fun onReadyForSpeech(params: Bundle?) { status("Listening… Speak a command or a word.",true) }
 override fun onBeginningOfSpeech() {}
 override fun onRmsChanged(rmsdB: Float) {}
 override fun onBufferReceived(buffer: ByteArray?) {}
 override fun onEndOfSpeech() { status("Recognising…",true) }
 override fun onError(error: Int) { stop();status(when(error) { SpeechRecognizer.ERROR_NO_MATCH -> "No word recognised. Try again.";SpeechRecognizer.ERROR_NETWORK,SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service needs a connection. Use touch to play offline.";SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission unavailable.";else -> "Speech unavailable ($error). Try again or use touch." },false) }
 override fun onResults(results: Bundle?) { val text=results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull();stop();if(text!=null) { status("Heard: $text",false);result(text) } }
 override fun onPartialResults(partialResults: Bundle?) {}
 override fun onEvent(eventType: Int,params: Bundle?) {}
}
