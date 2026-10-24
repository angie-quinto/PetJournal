# PetJournal testing

## Coverage

- JVM unit tests cover weight conversion, reminder recurrence, Room migration, health/reminder repository persistence, and backup export/restore validation.
- Compose instrumentation tests cover initial empty/no-pet states for health, care, and timeline. They are compile-checked in the current environment; runtime execution still requires an emulator or connected Android device.
- Manual release checks should cover notification delivery and permission denial, image picking/display/cleanup, accessibility with large text, and backup selection/restore confirmation.

## Commands

From the repository root on Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:connectedDebugAndroidTest
```

The last command requires an available Android device or emulator. `:app:compileDebugAndroidTestKotlin` verifies instrumentation test source compilation without a device.
