# No Snooze Alarm

A simple, reliable personal alarm clock for Android 8.0+ (built and tuned for Samsung Galaxy M11 / Android 12).

- Multiple one-shot alarms, each with its own **date, time, label and audio file**
- **No snooze. Ever.** The ringing screen has a single **STOP ALARM** button
- Plays your chosen MP3/WAV/M4A/AAC/OGG **once, start to finish**, with no loop and no fade, then auto-dismisses
- Rings when the app is closed, the phone is locked, the screen is off, or you're in another app
- Survives reboot, app update, time change and time zone change
- Help screen with live permission status and Samsung battery setup steps

---

## Get the APK (no Android Studio needed)

1. Upload this whole folder to the GitHub repo (`toorqgt-glitch/no-snooze-alarm`), including the hidden `.github` folder.
2. GitHub runs **Actions → Build APK** automatically on every push. It takes about 4–6 minutes.
3. Open the finished run, scroll to **Artifacts**, and download **NoSnoozeAlarm-apk** (a zip containing `app-debug.apk`).
4. Copy `app-debug.apk` to the phone, open it, and allow "Install unknown apps" for your file manager.

You can re-run the build anytime from **Actions → Build APK → Run workflow**.

## Build locally (Android Studio)

Open the folder in Android Studio (Ladybug or newer), let Gradle sync, then click **Run** or **Build → Build APK(s)**.
Command line (JDK 17 + Android SDK 35): `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`

## First-run setup on the Samsung M11 (one time)

Open the app → **?** (Help & permissions). Every item should show ✅:

1. **Battery → Unrestricted**: Settings → Apps → No Snooze Alarm → Battery → Unrestricted
2. **Not sleeping**: Settings → Battery and device care → Battery → Background usage limits → remove from Sleeping / Deep sleeping apps, add to *Never sleeping apps*
3. **Alarms & reminders**: Allowed (on Android 12 this is on by default)
4. **Notifications**: Allowed, with the "Ringing alarms" category set to pop-up and shown on the lock screen
5. **Alarm volume** turned up
6. **Test it**: set an alarm 2 minutes ahead, lock the phone, and wait.

## How it works

| Piece | Role |
|---|---|
| `AlarmScheduler` | `AlarmManager.setAlarmClock()` (exact, Doze-proof). One PendingIntent per alarm id, always cancelled before re-scheduling, so there are no duplicates. Falls back to inexact if exact alarms are denied, and the UI warns you. |
| `AlarmReceiver` | Receives the trigger and immediately starts the foreground service (exact alarms are exempt from Android 12's background-start limits). |
| `AlarmService` | Foreground service (`mediaPlayback`). MediaPlayer with `USAGE_ALARM` and a partial wake lock, so it keeps playing with the screen off. No looping, no volume changes. Completion stops everything. Posts a full-screen-intent notification with a STOP ALARM action. |
| `AlarmRingingActivity` | Shows over the lock screen and turns the screen on. Big clock, label, date, one STOP button. Back is disabled. |
| `RescheduleReceiver` | `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `TIME_SET`, `TIMEZONE_CHANGED`, exact-alarm permission changes → re-schedules everything. |
| Room DB | Stores alarms as local wall-clock date/time, so a time zone change keeps the same clock time. |
| Audio | Storage Access Framework picker plus `takePersistableUriPermission`, so access survives reboots. Unused grants are released. |

**Edge cases handled**
- *Audio missing or unplayable*: the screen shows a clear message, then the phone's default alarm tone and vibration play until STOP (10-minute cap). A broken file never means a silent alarm.
- *Second alarm fires while one is ringing*: the new alarm takes over and the old one is dismissed.
- *Alarm missed while the phone was off*: it's marked past on next boot or app open. It doesn't ring late.
- *Force-stop wiped the alarms*: opening the app re-schedules everything.
- *Notification permission denied*: audio still plays; the warning banner and Help screen flag it.

**Logs**: Logcat, tag `NoSnoozeAlarm`.

## Honest limitations

Android cannot guarantee an alarm when the phone is **off**, the **battery is dead**, or the app has been **force-stopped** (until you open it again). Samsung's aggressive sleeping-app modes can also delay alarms, which is why the setup steps above matter.

## Tech

Kotlin · AndroidX · Material 3 · Room (KSP) · Coroutines · minSdk 26 · target/compile SDK 35 · Java & Kotlin JVM target 17 · AGP 8.7.3 · Gradle 8.11.1
