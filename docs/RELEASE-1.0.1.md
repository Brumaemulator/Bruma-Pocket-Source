# Bruma Pocket 1.0.1 / 7

Package com.brumastudio.pocket. Android 8.0+ (API 26), target 36, ARM64.

Changes: selectable persistent light/dark themes for library, settings, dialogs and touch controls, translated in all nine app languages; dark uniform player background; original game aspect ratio with a fine portrait bezel and POWER indicator; portrait controller in the lower third; landscape fast-forward starts above R and remains movable. Removed the integer-scaling option and disabled stored integer-scaling preferences in the player. Existing manual controller positions remain respected.

Save behavior: pause, Android Back, backgrounding and exit no longer create or overwrite emulator snapshots. Saving a state remains explicit; Continue uses the last manually saved state. Native battery .sav writes remain enabled to retain saves made inside games. Existing saves are not deleted.

No engine, new dependency, SDK, package, privacy endpoint or permission change. Previous release sources and test reports are historical, not new verification claims.

Verification for this release is in the accompanying delivery validation report. Android ARM64 debug update was installed over the existing debug app preserving data; player appearance and light/dark switching were exercised; state hashes matched before and after Back. Production signing is independent of the debug phone build. No Play publication performed.
