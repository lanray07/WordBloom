# Fire tablet compatibility — 18 October 2026

The connected **KFTRWI** Fire tablet runs Android 9 / API 28 and has a 1200×1920
natural display. Normal-debug physical testing now covers landscape and portrait,
continuous swipes/backtracking, typing, completion, persistence/recreation,
accessibility settings, 150% system text size and gameplay with Wi-Fi disabled.
See `reports/fire-device-qa.json` for the tested APK hash and case transcripts.
The dense expert daily board was also visually inspected with large text and the
evening/high-contrast settings; real QA captures are in `store/raw/tablet/`.

The device has no recognition service; the unavailable-service message and
swipe/tap/type fallback were tested. TTS engines are installed. The publisher confirmed clear English letter speech; regional audio and
VoiceView quality still need hands-on observation; speech recognition round trips
are not claimed from this hardware configuration.

Amazon App Tester checks passed for the normal Amazon debug adapter: price
display using a UK fixture, cancellation, purchase/fulfilment, restore, account
switch and test-receipt cancellation. No real money was charged. Final
store-delivered minified-release testing remains pending. Multilingual device
evidence is recorded separately in reports/localized-device-qa.json.

Compile/target SDK 36, minSdk 26, JDK 17 and core-library desugaring. No mandatory
Google Play Services, Firebase or Google billing. API below 26 is unsupported.
Amazon accepted the earlier production APK and selected twelve compatible Fire
tablet entries, zero Fire TV and zero automotive entries. That filter is not
physical certification of all twelve models. Only the model above was tested.

Earlier API 36/1280×800 emulator attempts could not boot (missing hypervisor;
software fallback access violation). No kernel drivers or virtualization settings
were changed. Phone layouts have renderer evidence; no physical phone was tested.
