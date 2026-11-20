pluginManagement { resolutionStrategy { eachPlugin { if(requested.id.id.startsWith("com.android.")) useModule("com.android.tools.build:gradle:9.4.0") } }; repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = "BrumaPocket"
include(":app", ":bruma-core", ":bruma-library", ":bruma-input", ":bruma-covers", ":bruma-saves")
