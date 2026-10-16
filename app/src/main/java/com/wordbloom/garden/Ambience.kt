package com.wordbloom.garden
import android.content.Context
import android.media.*
class Ambience(context: Context) {
 private val audio=context.getSystemService(AudioManager::class.java)
 private var wanted=false
 private var closed=false
 private val player=MediaPlayer.create(context,R.raw.garden_ambience).apply { isLooping=true;setVolume(.35f,.35f) }
 private val focus=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setOnAudioFocusChangeListener { change ->
  if(!closed)when { change==AudioManager.AUDIOFOCUS_GAIN && wanted -> player.start();change<0 -> player.pause() }
 }.build()
 fun enabled(value: Boolean) { if(closed)return;wanted=value;if(value && audio.requestAudioFocus(focus)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED)player.start() else { player.pause();audio.abandonAudioFocusRequest(focus) } }
 fun pause() { if(closed)return;wanted=false;player.pause();audio.abandonAudioFocusRequest(focus) }
 fun close() { if(closed)return;pause();closed=true;player.release() }
}
