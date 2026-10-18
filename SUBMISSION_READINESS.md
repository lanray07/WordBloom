# Submission readiness — 18 October 2026

**Not yet ready to submit.** The multilingual candidate now supports all ten
requested locale editions after separate AI editorial/lexical review. The Amazon
console still contains the earlier English-only APK and copy. An enabled Submit
App button does not establish that the final candidate was tested or uploaded.

Completed work includes Kotlin/Compose gameplay, Room/DataStore progress,
curated 24-puzzle collections, six garden chapters, daily play, accessibility
settings, Amazon one-time premium integration and all requested language assets.
English UK/US share a journey; other locale editions have separate saved progress.
Portuguese editions share vocabulary with distinct commands and progress.
Translations contain 179 UI templates and 241 total source entries per added
locale. Codex reviewed the text, vocabulary and commands separately from DeepL
generation. Review hashes/methods are recorded in localization/approvals.json;
no native human certification is claimed. Seventeen target sets were improved;
ten corpus rarity flags have primary-dictionary adjudications.

The physical KFTRWI Fire tablet runs Android 9/API 28. Normal-debug gameplay,
persistence, swipes, recreation, accessibility, offline play and translated input
checks are reported in TEST_REPORT.md and hash-bound JSON evidence. The tablet
has no speech recognition service; touch/text fallback is tested. The publisher
confirmed clear English letter speech. VoiceView announcements and localized
audio perception have not been manually certified. One model does not certify
every Fire device in Amazon's compatibility filter.

The Amazon debug adapter previously passed App Tester price, cancel, purchase,
fulfilment, restore, refund/revocation and account-switch checks. These use a
sandbox and do not certify the minified release delivered by Amazon. A separate
.dev APK explicitly simulates purchases and must never be uploaded as production.

Remaining release work:

1. Update the ten console descriptions and language-support fields to match the
   multilingual candidate; review the three premium listing compositions against
   final UI, upload the final unsigned release APK, and verify saved draft values.
2. Use Amazon Live App Testing to install the Amazon-delivered minified release
   on the tablet and check live product price, purchase/cancel/restore behavior
   and gameplay. An invitation needs the tablet's actual Amazon-account email
   and explicit authorization to send it; the support email is not assumed.
3. Record remaining hands-on audio/accessibility observations and any findings.
   Speech recognition needs a device with a recognition service if advertised
   beyond the tested fallback. Avoid claiming tests that were not performed.
4. Run tools/release_gate.py against matching source/APK/device/console/LAT hashes
   before using the existing authorization to submit if complete.

Publisher decisions are already recorded: all ages, export certification,
privacy/SDK disclosures and the WordBloom name. Do not ask for those again.
Amazon applies store signing; a private owner signing key is not needed for this
Amazon-only upload. The free app has one-time premium access, base price GBP3.99.
Nothing has been submitted or published.

Original art/audio and three production Compose listing compositions are present;
the latter are renders, not physical device screenshots. Thirty-two older
watermarked concepts are retained only as concepts and must not be uploaded.
Generated localized listing drafts still retain the older English-gameplay
statement until the metadata revision. Curated word allowlists may reject valid
words outside the authored answers; do not advertise a comprehensive dictionary.
See store/CONSOLE_DRAFT.md and reports/release-gate.json for the separate console
and release evidence. Expansion beyond the current 24 authored puzzles per
collection and optional reminders remain product improvements, not claimed work.
