# Amazon submission preparation

Do not submit automatically. No app has been uploaded or published.

## Identity and current requirements

Working title: **WordBloom – Word Connect & Puzzle Games**. Existing WordBloom puzzle games were found on [Google Play](https://play.google.com/store/apps/details?id=com.techixen.wordbloom) and [Apple](https://apps.apple.com/cy/app/wordbloom/id6780787374). Naming availability is **not cleared**. Store searches do not establish trademark clearance; approve a cleared final name before publication.

[Amazon ASO guidance](https://developer.amazon.com/apps-and-games/blogs/2024/06/app-store-optimization) describes a 200-character display-title limit. The current [Appstore Details documentation](https://developer.amazon.com/docs/app-submission/appstore-details.html), found through the FAQ's moved link and reviewed on 16 October 2026, specifies a 4000-character long description, a 2000-byte short description, and three to five product feature bullets. Plain text is required.

The same official page requires 114×114 and 512×512 PNG icons. Store icons should use full-edge art without imposed rounded corners. Screenshots: minimum three, maximum ten, PNG/JPEG, portrait or landscape. Listed sizes are 800×480, 1024×600, 1280×720, 1280×800, 1920×1080, 1920×1200 and 2560×1600. Therefore the prepared 800×1280 phone and 1280×800 tablet compositions match listed portrait/landscape sizes. Optional promotional art is 1024×500 PNG/JPEG, with text at least 50 px from edges, and must not be a screenshot or display price/ratings/buttons. Reconfirm specifications in the console before actual upload.

## Build and signing

`gradlew.bat :app:assembleRelease` creates a shrunk release APK. Without owner signing configuration it remains unsigned. Supply `WORDBLOOM_KEYSTORE`, `WORDBLOOM_STORE_PASSWORD`, `WORDBLOOM_KEY_ALIAS`, and `WORDBLOOM_KEY_PASSWORD` through the environment; keep all secrets out of version control. Do not invent the owner's signing identity. Verify signed artifacts with Android `apksigner verify --verbose --print-certs`; record the certificate and back up the keystore securely.

[Amazon's setup documentation](https://developer.amazon.com/docs/in-app-purchasing/iap-get-started.html) says Amazon applies an Amazon signature on submission. Still preserve the owner's distribution/update signing process and test the final store-delivered build.

## Submission gates

- Clear naming/identity and replace the working name/package if required.
- Review English vocabulary, audience suitability and difficulty. Expand only with validated original content.
- Configure the Amazon entitlement SKU/prices, required SDK app identity, App Tester and live receipt/restore/cancel flows.
- Run the Fire matrix and Android UI/persistence suite, including orientation, larger text and interruptions.
- Capture implemented phone/tablet screens, compose and visually review all eight marketing images. Screenshot 2 says Letter Connect rather than implying traditional word-search gameplay. Screenshot 8 describes the available English editions rather than unimplemented language packs.
- Reconfirm image specifications in the console and review exported original icon/promo rasters.
- Finalise privacy/support documents with actual publisher contact and hosting URLs. Complete ratings, IAP disclosures, third-party notices and device targeting.
- Set the final version, sign, verify checksum/certificate and upload only after explicit owner authorisation.

See `store/` for English drafts and capture tooling. No ranking guarantee or cognitive/medical benefit claim is made.
