# Submission readiness â€” 18 October 2026

**Not ready for submission.** The publisher requires translated gameplay in all
eight additional locale editions before release. The connected Fire tablet has
now executed gameplay/storage/recreation checks and Amazon sandbox purchasing;
final device evidence is in `reports/fire-device-qa.json` when present. DeepL drafts are complete. Native review, multilingual device QA and the final
store-delivered release test remain required. An enabled Submit App button does
not override these conditions.

| Deliverable | Implemented | Remaining gate |
|---|---|---|
| Kotlin/Compose/MVVM | Native source, pure engine, Room, DataStore, flows | Device lifecycle/orientation QA |
| Word-connect gameplay | Swipe/tap/type, repeat-safe tiles, undo/reset, shuffle, target/bonus validation, hints, completion | Gesture/device UI testing |
| Puzzle library | 24 authored English puzzles; six per difficulty | Human vocabulary/difficulty review; expansion towards 300 |
| Garden progression | Six scenes, sequential unlocks, first-completion rewards, replay, four milestones | Visual device QA |
| Daily play | Local-date/locale selection, saved completion/history, offline catalogue | Timezone/clock-change device scenarios; optional reminders not implemented |
| Voice | Push-to-talk, permissions, capability checks, parser, TTS, focus, errors/touch fallback | English letter TTS confirmed; VoiceView confirmation; recognition unavailable on this tablet |
| Monetisation | Amazon SDK listener, product prices, purchase, receipt/type checks, account-scoped cache, restore/cancel handling | App Tester purchase/cancel/restore/refund/account-switch checks executed; final store-delivered test pending; no server RVS |
| Localisation | English editions, NFC, locale registry, development translation/cache/glossary/placeholder tooling | 184 interface/message templates extracted; draft packs and reviewed-bundle integration prepared; DeepL drafts complete; native review/locale device QA pending |
| Accessibility | Larger text, contrast, reduced motion, dark theme, tile semantics, text fallback | VoiceView spoken-announcement confirmation; large-font device inspection passed |
| Original art/audio | Canvas garden scenes, SVG masters/icons, adaptive/splash/notification assets, ambient loop | Store icon/promo PNGs exported and visually inspected; English device visual review passed; multilingual visual review pending |
| Screenshots | Three production Compose render compositions uploaded; 32 older concepts retained separately | Eight real Fire QA captures prepared; recapture/review after final localization before replacing listing images |
| Listing/privacy/support | Ten localized console descriptions, public privacy/support pages, keywords and checklists | Publisher approved privacy disclosures and confirmed the name on 18 October; all-ages/export were confirmed on 17 October; copy must match the final multilingual build |
| Packaging/tests | See TEST_REPORT.md for executed results | Final multilingual candidate and Amazon store-delivered verification; Amazon handles signing |

Secondary Android distribution remains a product decision: Amazon's Appstore for non-Amazon Android devices ended in August 2025. This source supports Android phone/tablet layouts and offline free play; premium sales outside supported Fire devices require a separately configured distribution/billing adapter. See MONETIZATION.md for the official source.

Existing similarly named listings were disclosed. The publisher confirmed the
WordBloom name on 18 October 2026. This records publisher approval, not an
independent trademark/legal opinion.

The multilingual target is not met by translating English answers: no non-English gameplay pack is enabled. The current English-only listing copy must be updated after reviewed multilingual packs are activated. Eight concepts per device format now exist in `store/concepts/`, with English UK/US variants. They use production Compose renders and carry a device-QA watermark. They are not device captures or approved upload assets.

The engine uses an intentionally limited curated vocabulary. It may reject real English words outside the target/bonus allowlist. Market it as a curated puzzle collection, not a comprehensive dictionary.

The connected KFTRWI Fire tablet (Android 9 / API 28, 1200×1920 natural display)
replaces the earlier no-device blocker. Speech recognition is absent and its
touch/text fallback was tested. Audio perception/VoiceView listening and the
final Amazon-delivered release still need hands-on review. One tested model does
not certify every model in the console's compatibility filter.

Inputs still needed from the publisher: arrange native approval of each language's interface, vocabulary and spoken
commands; accept the final Live App Testing invitation on the tablet when ready.
Privacy/name approval is already recorded. No private signing key is required
for this Amazon-only upload. The source, candidate APKs and tests remain
reviewable while the release is blocked.

Amazon draft progress: production APK accepted; ten localized descriptions and three artwork files saved; premium entitlement has seven text locales and a £3.99 base price. Required draft fields are complete: three production Compose render images were accepted, all ages is selected, and export compliance was explicitly confirmed and checked. Submit App is enabled but was not clicked. Nothing was submitted. See store/CONSOLE_DRAFT.md.

17 October 2026: all-ages target audience was explicitly confirmed by the publisher and saved in Amazon.

17 October 2026: three fresh independent store-render cases passed. These provide real production UI render sources for the listing images, not physical-device certification. The publisher explicitly authorized the export-compliance checkbox; it is checked without submitting or publishing the app.

18 October 2026: publisher requires all listed gameplay languages and approved
privacy/name decisions. See `store/publisher-decisions.json`,
`localization/REVIEW_HANDOFF.md` and `reports/release-gate.json`. The English-only
console APK and premium item remain unsubmitted.
