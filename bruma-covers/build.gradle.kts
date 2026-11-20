plugins { id("com.android.library") }
android {
 namespace = "com.brumastudio.pocket.covers"
 compileSdk = 36
 defaultConfig { minSdk = 26 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies { implementation(project(":bruma-library")) }
