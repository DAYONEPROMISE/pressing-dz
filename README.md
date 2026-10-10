# PressingDZ — offline ticket tracker for Algerian pressings

100% offline Android app (Kotlin, Jetpack Compose Material 3, Room). No account, no network.
Drop-off tickets (client, items, price DZD, ready date), status reçu → prêt → livré,
daily takings, French UI with Arabic/RTL support via `supportsRtl`.

## Build (CI)
`./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` — APK at
`app/build/outputs/apk/debug/app-debug.apk`.

## License
MIT — clean-room code, no third-party app code.
