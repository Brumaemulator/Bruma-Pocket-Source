# Bruma Pocket 1.0.0

Independent Android application for GB, GBC and GBA. Package `com.brumastudio.pocket`, versionCode 6, minSdk 26, targetSdk 36, ARM64. Source distribution only: no APK/AAB, ROMs, BIOS, commercial artwork, accounts, SDK downloads or private keys.

Frontend/adaptations: GPL-3.0-or-later. Engine mGBA 0.10.5: MPL-2.0, blip_buf: LGPL-2.1-or-later, inih: BSD-3-Clause; see LICENSE, NOTICE and docs/LICENCIAS.md for all notices and provenance. Original mGBA sources are unchanged.

## Build

JDK 21, Android SDK platform/build-tools 36, NDK 28.2.13676358, CMake 4.1.2. Gradle 9.6.0 wrapper, Android Gradle Plugin 9.4.0, effective Kotlin stdlib 2.2.10. Set ANDROID_HOME to your own SDK path. On Windows run `gradlew.bat :app:assembleDebug`; Linux/macOS: `sh gradlew :app:assembleDebug`. This builds the complete app and statically linked dependencies from source with your own debug key. You can modify blip_buf/mGBA and relink using the same recipe. No vendor signing key is necessary to build your own version.

For your own signed release, supply BRUMA_POCKET_KEYSTORE, BRUMA_POCKET_STORE_PASSWORD, BRUMA_POCKET_KEY_PASSWORD and optionally BRUMA_POCKET_KEY_ALIAS (default bruma-pocket-upload), then run `:app:bundleRelease :app:assembleRelease`. Without these variables release outputs are unsigned. Never commit credentials. Developer keys and the Android SDK/JDK are not provided. Release signing for Google Play is managed separately by Play App Signing.

Default ABI arm64-v8a. For an isolated emulator QA build only: `-PpocketAbis=x86_64`. First build downloads open development dependencies; --offline works after caching. AGP/Gradle/compiler are build tools, not proprietary Google client SDKs embedded in Pocket.

## Features

GBA/GBC/GB filters, favorites, read-only folder access, ZIP import without retaining a second library, session-only extraction when a provider requires it, native mGBA emulation, adjustable portrait/landscape controls, controller mapping and fast-forward, nine UI languages, battery .sav import/export, auto/manual states and full Pocket backups. Raw .sav files require the exact same game/version; save states are not portable between engines. Save inside the game before exporting .sav.

Cover downloads are optional and connect to Libretro/GitHub. No game or third-party artwork is bundled or hosted in this repository. Custom images can be selected locally. Artwork rights remain with their owners and are separate from code licenses. No account, analytics, billing, creator/referral client, advertising SDK or DRM.

A main-menu option opens the separate BRUMA Emulator listing in Google Play; no tracking is implemented. Privacy: https://brumastudio.netlify.app/bruma-pocket/privacidad

## Source and QA

This release includes exact app/library sources and upstream code necessary for rebuilding. Unused upstream desktop/test artwork, generated diagnostic ROM binaries, build caches, SDK/JDK, keys and private paths are omitted. Original test generators and original diagnostic assembly sources are provided in tools/. Run instrumentation only in an isolated QA device using the own generated PocketQA folder; never run the complete suite against a real game collection.

Public source releases: https://github.com/Brumaemulator/Bruma-Pocket-Source/releases
Contact: brumaemulator@gmail.com
