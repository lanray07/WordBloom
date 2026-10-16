plugins { id("com.android.application"); kotlin("android"); kotlin("plugin.compose"); kotlin("kapt") }
android {
 namespace = "com.wordbloom.garden"
 // Visual tests use API-35 stubs and an API-34 layoutlib; production builds target/compile API 36.
 compileSdk = if (project.findProperty("visualQA") == "true") 35 else 36
 defaultConfig {
  applicationId = "com.wordbloom.garden"
  minSdk = 26
  targetSdk = 36
  versionCode = 1
  versionName = "0.1.0"
  buildConfigField("boolean", "DEVELOPMENT_BILLING", "false")
  testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 }
 buildFeatures { compose = true; buildConfig = true }
 val ownerKeyStore = System.getenv("WORDBLOOM_KEYSTORE")
 if (!ownerKeyStore.isNullOrBlank()) {
  signingConfigs.create("ownerRelease") {
   storeFile = file(ownerKeyStore)
   storePassword = System.getenv("WORDBLOOM_STORE_PASSWORD")
   keyAlias = System.getenv("WORDBLOOM_KEY_ALIAS")
   keyPassword = System.getenv("WORDBLOOM_KEY_PASSWORD")
  }
 }

 buildTypes {
  debug {
   if (project.findProperty("devBilling") == "true") {
    applicationIdSuffix = ".dev"
    versionNameSuffix = "-dev"
    buildConfigField("boolean", "DEVELOPMENT_BILLING", "true")
   }
  }
  release { if (!ownerKeyStore.isNullOrBlank()) signingConfig = signingConfigs.getByName("ownerRelease"); isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17; isCoreLibraryDesugaringEnabled = true }
 kotlinOptions { jvmTarget = "17" }
 packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
dependencies {
 implementation(project(":engine"))
 implementation(platform("androidx.compose:compose-bom:2025.04.01"))
 implementation("androidx.activity:activity-compose:1.10.1")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui-tooling-preview")
 debugImplementation("androidx.compose.ui:ui-tooling")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
 implementation("androidx.room:room-runtime:2.7.1")
 implementation("androidx.room:room-ktx:2.7.1")
 kapt("androidx.room:room-compiler:2.7.1")
 implementation("androidx.datastore:datastore-preferences:1.1.7")
 implementation("com.amazon.device:amazon-appstore-sdk:3.0.9")
 coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
 testImplementation("junit:junit:4.13.2")
 androidTestImplementation("androidx.test.ext:junit:1.2.1")
 androidTestImplementation("androidx.test:runner:1.6.2")
 androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))
 androidTestImplementation("androidx.compose.ui:ui-test-junit4")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
}

if (project.findProperty("visualQA") == "true") {
 apply(plugin = "app.cash.paparazzi")
 android.sourceSets.getByName("test").java.srcDir("src/testVisual/java")
}
