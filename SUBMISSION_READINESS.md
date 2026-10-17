# Submission readiness â€” 16 October 2026

**Not ready for submission.** The repository contains a native application and reproducible builds, rather than only mockups. Android/Fire runtime certification and multiple store/content deliverables remain open. Do not publish automatically.

| Deliverable | Implemented | Remaining gate |
|---|---|---|
| Kotlin/Compose/MVVM | Native source, pure engine, Room, DataStore, flows | Device lifecycle/orientation QA |
| Word-connect gameplay | Swipe/tap/type, repeat-safe tiles, undo/reset, shuffle, target/bonus validation, hints, completion | Gesture/device UI testing |
| Puzzle library | 24 authored English puzzles; six per difficulty | Human vocabulary/difficulty review; expansion towards 300 |
| Garden progression | Six scenes, sequential unlocks, first-completion rewards, replay, four milestones | Visual device QA |
| Daily play | Local-date/locale selection, saved completion/history, offline catalogue | Timezone/clock-change device scenarios; optional reminders not implemented |
| Voice | Push-to-talk, permissions, capability checks, parser, TTS, focus, errors/touch fallback | Physical speech service/TTS/permission/audio tests |
| Monetisation | Amazon SDK listener, product prices, purchase, receipt/type checks, account-scoped cache, restore/cancel handling | App/entitlement drafts and public SDK key configured; App Tester/live verification pending; no server RVS |
| Localisation | English editions, NFC, locale registry, development translation/cache/glossary/placeholder tooling | Complete dynamic string extraction; non-English dictionaries, translation and native review |
| Accessibility | Larger text, contrast, reduced motion, dark theme, tile semantics, text fallback | Screen reader and large-font runtime inspection |
| Original art/audio | Canvas garden scenes, SVG masters/icons, adaptive/splash/notification assets, ambient loop | Store icon/promo PNGs exported and visually inspected; device visual review pending |
| Screenshots | Three production Compose render compositions uploaded; 32 older concepts retained separately | Physical device captures and runtime visual approval remain pending |
| Listing/privacy/support | Ten localized console descriptions, public privacy/support pages, keywords and checklists | Publisher review, audience/export certifications and approved final name |
| Packaging/tests | See TEST_REPORT.md for executed results | Owner signing and final store-delivered verification |

Secondary Android distribution remains a product decision: Amazon's Appstore for non-Amazon Android devices ended in August 2025. This source supports Android phone/tablet layouts and offline free play; premium sales outside supported Fire devices require a separately configured distribution/billing adapter. See MONETIZATION.md for the official source.

The requested working name conflicts with existing word-game listings. This is a material release issue, not a claim that any particular trademark is registered or infringed. Clear the final name before marketing/publication.

The multilingual target is not met by translating English answers: no non-English gameplay pack is enabled. The screenshot plan accurately substitutes English puzzle editions for the requested multilingual claim and letter-connect wording for a traditional word-search claim. Eight concepts per device format now exist in `store/concepts/`, with English UK/US variants. They use production Compose renders and carry a device-QA watermark. They are not device captures or approved upload assets.

The engine uses an intentionally limited curated vocabulary. It may reject real English words outside the target/bonus allowlist. Market it as a curated puzzle collection, not a comprehensive dictionary.

Environment blocker: no connected device, absent Android Emulator hypervisor driver, and software emulator access violation before Android boot. Do not count prepared instrumentation or screen renders as physical-device certification. See FIRE_COMPATIBILITY.md.

Next owner-dependent steps: review privacy/SDK disclosures, provide a working test device/emulator for capture and App Tester/live checks, approve a cleared name and obtain language/content review. Amazon accepted the unsigned production APK and applies its own signing; a separate owner signing identity is needed for non-Amazon release distribution. All reversible local implementation and packaging work should remain available for review.

Amazon draft progress: production APK accepted; ten localized descriptions and three artwork files saved; premium entitlement has seven text locales and a £3.99 base price. Required draft fields are complete: three production Compose render images were accepted, all ages is selected, and export compliance was explicitly confirmed and checked. Submit App is enabled but was not clicked. Nothing was submitted. See store/CONSOLE_DRAFT.md.

17 October 2026: all-ages target audience was explicitly confirmed by the publisher and saved in Amazon.

17 October 2026: three fresh independent store-render cases passed. These provide real production UI render sources for the listing images, not physical-device certification. The publisher explicitly authorized the export-compliance checkbox; it is checked without submitting or publishing the app.
