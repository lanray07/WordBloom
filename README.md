# WordBloom â€” Android word-connect game

Native Kotlin / Jetpack Compose application for Android and compatible Fire tablets. No WordBloom account, ads, analytics, Firebase or Google Play Services requirement. The requested name remains a **working name** because an existing WordBloom word game was found.

## Build and install

Requires JDK 17 and Android SDK platform 36. Set `sdk.dir` in untracked `local.properties` or configure `ANDROID_HOME`.

```powershell
.\gradlew.bat :engine:test :app:assembleDebug :app:assembleRelease :app:assembleDebugAndroidTest :app:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.wordbloom.garden/.MainActivity
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Unsigned release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`. A debug APK is for testing, not production distribution.

## Play

Drag across letters and release, or tap letters and choose Submit word. Text entry is also available. Backtrack to the preceding tile to undo a swipe. A tile instance cannot be reused within one word. Reveal a letter for 1 hint or a word for 3; shuffling and a starting-letter highlight are free. Earn 3 hints on first completion (4 with premium) and 1 for a new bonus word. Replays do not award additional hints.

The first four puzzles, daily play, settings and optional voice are free. Amazon premium unlocks the remaining twenty puzzles and five worlds. Normal-build prices come from Amazon; normal builds never simulate purchases. Daily play selects from the included catalogue using local date and English edition.

## Included and pending

- 24 original, structurally validated English puzzles across four difficulty bands. The 300-level target remains pending editorial review and expansion.
- English UK and US share a common-spelling collection. Other gameplay languages are disabled pending dictionaries and linguistic review.
- Six original vector garden environments, milestones, daily history, large text, contrast, reduced motion and dark theme.
- Device-dependent push-to-talk and Android TTS. Recognition may need network access; all touch/text puzzles work offline.
- Room transactionally persists progress and hints. DataStore persists settings. Original ambient music and feedback tones have separate controls.

This is **not submission-ready**. Device QA, real screenshots, Amazon App Tester/live purchasing verification, naming clearance, final signing and language/content review remain gates. See `SUBMISSION_READINESS.md` and `TEST_REPORT.md`.

## Developer tools

`python tools/localize.py` emits translation completeness and extraction reports. Set `DEEPL_API_KEY` outside the repo, then add `--translate` for development-time UI translation. No credentials enter the APK. Generated translations stay outside Android source until reviewed. Run `python -m unittest discover -s tools -p test_*.py -v`.

Screenshot capture/composition tools require real device captures and refuse to fabricate missing screens. See `store/screenshot-manifest.json`. SVG/PNG artwork is in `assets/`; required icon PNGs and promotional art are in `store/artwork/`.

Off-device UI verification: `gradlew.bat -PvisualQA=true :app:recordPaparazziDebug --tests com.wordbloom.garden.ScreenRenderTest` uses API 35 stubs with an API 34 layoutlib for Windows/JDK 17 compatibility. Production builds compile/target API 36. The suite produces 20 phone/tablet screen renders; `python tools/compose_render_concepts.py` creates clearly labelled marketing concepts from them, separately from the real-device capture pipeline. Review these in `store/concepts/`.

Generated, reviewable crossword layouts and bonus vocabulary are in `content/puzzles-en.json`; they come directly from the Kotlin engine using `tools/ExportPuzzles.java`.

## Isolated development purchasing

`gradlew.bat -PdevBilling=true :app:assembleDebug` creates a development app with package ID `com.wordbloom.garden.dev`, a `-dev` version suffix, a visible development banner and clearly labelled test-only purchasing. Open Explore premium and use the TEST ONLY unlock to exercise premium features without real payment or store credentials. Restore reads only the development app's private test entitlement. Its progress/entitlement data are separate from the normal app.

The default debug build uses Amazon purchasing. The release source-set factory always uses Amazon, even when a development property is supplied. The development adapter is excluded from release DEX. Return to a build without the property before packaging normal APKs. `BillingContractTest` provides five JVM contract cases for the test adapter; these are separate from required Amazon App Tester/live verification.
