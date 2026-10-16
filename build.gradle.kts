buildscript {
 repositories { google(); mavenCentral() }
 dependencies { if (project.findProperty("visualQA") == "true") classpath("app.cash.paparazzi:paparazzi-gradle-plugin:1.3.5") }
}
plugins {
 id("com.android.application") version "8.11.1" apply false
 kotlin("android") version "2.1.20" apply false
 kotlin("jvm") version "2.1.20" apply false
 kotlin("plugin.compose") version "2.1.20" apply false
 kotlin("kapt") version "2.1.20" apply false
}
