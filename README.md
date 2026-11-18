# No Snooze Alarm (Android)

Private Android alarm app starter project. It is not a Play Store app.

## Current behaviour
- Multiple independent one-time alarms.
- Select audio using Android's document picker; the app persists access to the selected URI.
- Exact alarm scheduling via `AlarmManager.setAlarmClock`.
- Alarm playback uses a foreground media-playback service and alarm audio attributes.
- Full-screen alarm activity with only a CANCEL control.
- Playback stops automatically when the selected file reaches its end.
- Alarms are restored after device reboot / app update.

## Build
1. Install Android Studio (current stable) on a computer.
2. Open this folder as an existing Android Studio project.
3. Allow Gradle sync to download Android Gradle Plugin / Kotlin plugin.
4. Connect the Samsung phone with USB debugging enabled, or choose Build > Build APK(s).
5. Install the generated `app-debug.apk` on the phone.

## Important setup on Samsung
After installation:
1. Open the app and allow notifications.
2. Allow Alarms & reminders / exact alarms if Android asks.
3. In Settings > Apps > No Snooze Alarm > Battery, select Unrestricted if available.
4. In Samsung Settings > Battery and device care > Battery > Background usage limits, make sure the app is not in Sleeping or Deep sleeping apps.
5. Test with a short audio file and a test alarm a few minutes ahead, with the phone locked.

## Known limitations / validation still required
- This starter implements one-time alarms, not recurring weekday schedules.
- Android / Samsung may still suppress full-screen launch depending on OS version, notification settings, permissions, power state, or firmware policy. The alarm notification remains the fallback.
- No app can play an alarm if the phone is powered off, has run out of battery, is force-stopped by the user, or Android revokes required access.
- A reboot receiver restores scheduled alarms, but a force-stop prevents broadcasts until the user opens the app again.
- Audio is played from the chosen document URI; removing/moving the source file or revoking its permission can make playback fail.
- Please test with the exact Samsung model and One UI / Android version before relying on it for important wake-ups.

## Package
`com.personal.nosnooze`
