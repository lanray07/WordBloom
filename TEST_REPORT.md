# Test report — 16 October 2026

This report separates executed results from prepared tests. Final artifact/build evidence is also recorded in `reports/` when available.

## Executed

- Engine JUnit: 11 tests passed, 0 failures, 0 errors. Evidence: `engine/build/test-results/test/TEST-com.wordbloom.engine.EngineTest.xml` and `engine/build/reports/tests/test/index.html`.
- All 24 authored puzzles were generated and checked for solvable letter bags, answer/cell consistency and connectivity.
- Reproducible layout generation was checked over 16 seeds for each of 24 bags (384 cases inside one JUnit test).
- Tile reuse/backtracking, invalid/bonus/repeated answers, NFC accents, impossible inputs, English commands, daily IDs and entitlement cancellation/wrong-SKU rules were tested.
- Python localisation: 4 tests passed for placeholder round-trip, mismatch, duplication and empty-output rejection.
- Source extraction/completeness command ran; it reports untranslated/pending-review content instead of inventing completed locales.
- Debug APK and Android instrumentation APK compilation succeeded during development. Later full-build status must be read from the final report/log.
- Initial lint identified a missing Android-test BOM and an unescaped local SDK path. Both were corrected before the final run.
- A normal emulator launch reported missing hypervisor acceleration. Software-only attempts exited before Android boot; the final attempt returned Windows -1073741819.

Five JVM development-billing contract cases are included for explicit test-mode labels, successful persistence, failed purchase, restore and revocation. They use the isolated development adapter and never contact Amazon. Their executed result is recorded in the final package verification below.

## Prepared, not device-executed

`GameUiTest` contains accessible tile entry/word completion, Room close/reopen persistence with atomic wallet/progress, and an opt-in eight-screen capture tour. Run on a dedicated fresh test installation. The screenshot tour is skipped unless `capture=true` is supplied.

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
python tools/capture_screenshots.py --adb PATH_TO_ADB --serial DEVICE_SERIAL --variant tablet
python tools/compose_screenshots.py --variant tablet
```

The capture tool clears only the WordBloom test app's data. Device size/configuration must match the selected variant. Repeat for phone. Sources are mandatory; the composition script does not create fake gameplay screens.

Fire runtime, full offline/device interruption flows, visual/accessibility QA, microphone/TTS, live purchase/refund/restore/account switching, actual price display and performance/memory testing are pending. Pure parser/entitlement tests are not end-to-end service tests. No test or screenshot is represented as passed/captured unless executed.

Off-device Compose validation ran successfully: 2 parameterised JUnit cases produced 20 renders (ten scenes each for an 800×1280 phone and 1280×800 landscape tablet). Production screen functions and valid engine fixtures are used. Initial renderer attempts exposed API mismatches; the final optional configuration uses API 35 compile stubs and API 34 layoutlib while production remains on API 36. These renders check layout, not Android services or physical-device behaviour. The report is `app/build/reports/paparazzi/debug/index.html` and snapshots are in `app/src/test/snapshots/images/`.

Visual review caught and repaired phone viewport crowding, header spacing and tablet orientation. Original icon/promo exports were inspected, and image dimensions were verified. Marketing concepts use the production renders and are explicitly watermarked as pending device QA. Real screenshots remain outstanding. A later source audit added premium completion statistics and the additional completion hint; final Android compilation covers those changes, while live entitlement-driven reward flow remains a device-test gate.

## Final package verification

Production API 36 debug, release and instrumentation APK builds and lint passed. Lint: 0 errors, 23 warnings (including dependency-version suggestions and intentionally unused preparation assets). Engine: 11 passed; billing contract: 5 passed; localisation: 4 passed; visual: 2 passed with 20 renders. Debug signing verified. Release is unsigned; owner signing required. The development purchase adapter and Google Play Billing are absent from release DEX. A separate signed .dev APK with explicit simulated-purchase labels is provided for developer QA; its manifest, build flag and signature were verified without device execution.

Fast-swipe tracing now preserves the original down and interpolates crossed tiles; regression tests cover a fast segment, backtracking and a near miss. An interrupted R8 build held a generated output open; its stale daemon was terminated and the clean final build passed. The Amazon SDK emits stack-map warnings under R8, so the final store-delivered purchase flow still needs App Tester/live verification.
