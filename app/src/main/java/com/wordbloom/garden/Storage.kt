package com.wordbloom.garden

import android.content.Context
import androidx.room.*
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

@Entity(tableName="progress")
data class Progress(@PrimaryKey val id: String, val found: String="", val bonus: String="", val revealed: String="", val complete: Boolean=false)
@Entity(tableName="wallet")
data class Wallet(@PrimaryKey val id: Int=0, val hints: Int=8)
@Dao interface GameDao {
 @Query("SELECT * FROM progress") suspend fun all(): List<Progress>
 @Query("SELECT * FROM progress WHERE id=:id") suspend fun get(id: String): Progress?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun put(progress: Progress)
 @Query("SELECT * FROM wallet WHERE id=0") suspend fun wallet(): Wallet?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putWallet(wallet: Wallet)
 @Transaction suspend fun save(progress: Progress, wallet: Wallet) { put(progress); putWallet(wallet) }
}
@Database(entities=[Progress::class, Wallet::class],version=1,exportSchema=false)
abstract class GameDatabase: RoomDatabase() { abstract fun game(): GameDao }
val Context.settingsStore by preferencesDataStore("settings")
data class Settings(val narration: Boolean=false,val effects: Boolean=true,val music: Boolean=false,val contrast: Boolean=false,val reduced: Boolean=false,val large: Boolean=false,val dark: Boolean=false,val language: String="en-GB")
class SettingsRepository(private val context: Context) {
 val flow=context.settingsStore.data.map { p -> Settings(p[booleanPreferencesKey("narration")]?:false,p[booleanPreferencesKey("effects")]?:true,p[booleanPreferencesKey("music")]?:false,p[booleanPreferencesKey("contrast")]?:false,p[booleanPreferencesKey("reduced")]?:false,p[booleanPreferencesKey("large")]?:false,p[booleanPreferencesKey("dark")]?:false,p[stringPreferencesKey("language")]?:"en-GB") }
 suspend fun set(key: String,value: Boolean) { context.settingsStore.edit { it[booleanPreferencesKey(key)]=value } }
 suspend fun language(value: String) { require(value in setOf("en-GB","en-US"));context.settingsStore.edit { it[stringPreferencesKey("language")]=value } }
}
