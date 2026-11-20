plugins { id("com.android.library") }
android {
 namespace = "com.brumastudio.pocket.core"
 compileSdk = 36
 defaultConfig { minSdk = 26 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 ndkVersion = "28.2.13676358"
 defaultConfig { ndk { abiFilters += (providers.gradleProperty("pocketAbis").orNull ?: "arm64-v8a").split(",") }; externalNativeBuild { cmake { cppFlags += "-std=c++20" } } }
 externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "4.1.2" } }
}
