plugins { id("com.android.application") }
android {
 namespace = "com.brumastudio.pocket"
 compileSdk = 36
 testBuildType = providers.gradleProperty("pocketTestBuildType").orNull ?: "debug"
 defaultConfig { applicationId = "com.brumastudio.pocket"; minSdk = 26; targetSdk = 36; versionCode = 7; versionName = "1.0.1"; testInstrumentationRunner = "com.brumastudio.pocket.PocketChecks"; ndk { abiFilters += (providers.gradleProperty("pocketAbis").orNull ?: "arm64-v8a").split(",") } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 val pocketKey = providers.environmentVariable("BRUMA_POCKET_KEYSTORE").orNull
 val pocketStorePassword = providers.environmentVariable("BRUMA_POCKET_STORE_PASSWORD").orNull
 val pocketKeyPassword = providers.environmentVariable("BRUMA_POCKET_KEY_PASSWORD").orNull
 signingConfigs {
  if(!pocketKey.isNullOrBlank()&&!pocketStorePassword.isNullOrBlank()&&!pocketKeyPassword.isNullOrBlank())create("pocketRelease") {
   storeFile=file(pocketKey);storePassword=pocketStorePassword
   keyAlias=providers.environmentVariable("BRUMA_POCKET_KEY_ALIAS").orNull ?: "bruma-pocket-upload"
   keyPassword=pocketKeyPassword
  }
 }
 buildTypes { getByName("release") { isMinifyEnabled = false;signingConfig=signingConfigs.findByName("pocketRelease") } }
}
dependencies { implementation(project(":bruma-core")); implementation(project(":bruma-library")); implementation(project(":bruma-input")); implementation(project(":bruma-covers")); implementation(project(":bruma-saves")) }
