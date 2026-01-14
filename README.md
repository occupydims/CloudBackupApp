# CloudBackupApp (UI prototype)

This is a simple Kotlin + Jetpack Compose Android app.

- `MainScreen` recreates the layout from your screenshot.
- Every card/tile/button navigates to a blank template screen showing the tapped label as the header.

## Build (Android Studio)
1. Open this folder in Android Studio.
2. Let it download dependencies, then **Run**.

### If Android Studio complains about missing `gradle/wrapper/gradle-wrapper.jar`
Some environments strip binary files when sharing. Fix options:
- In Android Studio: `File > New > Import Project...` then set **Gradle** to “Use local Gradle distribution” (Settings > Build Tools > Gradle).
- Or on a machine with Gradle installed, run:
  - `gradle wrapper --gradle-version 8.2`
  - then reopen the project.

## What to change next
Replace `TemplateScreen` contents with real pages.
