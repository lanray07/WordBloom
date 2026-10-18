# Localisation — 18 October 2026

All ten requested editions are active in the current candidate: English UK/US,
German, Spanish, French, Italian, Dutch, Polish and Portuguese Brazil/Portugal.
The earlier Amazon console APK and descriptions still describe English gameplay;
they must be replaced before submission.

DeepL generated eight additional editions, each with 179 interface templates and
241 total source entries including resources and marketing copy. Codex separately
reviewed every UI entry, listing/caption draft, target/bonus set and displayed
voice command. Corrections are reproducible in editorial-overrides.json. The
review is AI-assisted editorial/lexical validation, not native human certification.
The original requirement calls for separate linguistic validation; it does not
require an outside human certificate. Approvals record the actual method/date
and hashes, rather than inferring approval from machine generation.

Seventeen puzzle target sets were improved, including an exact German duplicate
and obscure introductory words. Seven authored vocabularies supply eight regional
editions with 24 puzzles each. These are curated allowlists, not comprehensive
dictionaries; Portuguese editions share vocabulary but have regional commands
and separately saved progress. English UK/US intentionally share progress.

Frequency review covers 491 non-English and 123 English word observations.
Five non-English and five English rarity flags were checked against primary
dictionaries and retained with reasons in reports/rare-word-adjudication.json.
Frequency is a review aid, not a validity or difficulty certificate. Further
fluent regional/difficulty review would improve confidence; none is claimed.

The installer validates source/message/gameplay/command hashes before installing
bundles and changing ApprovedLanguages. Run tools/install_reviewed_languages.py
--check-only to detect stale reviews. Recompiling vocabulary preserves edits;
do not use --regenerate-drafts on reviewed input. The local DeepL key is read
only into the generation process and is excluded from source and APK assets.

Android 9 placeholder-regex compatibility was fixed on the real Fire tablet.
Count labels avoid English plural fragments; Portuguese commands match the parser.
Large-font layouts stack the board and wrap whole controls. Locale dates, NFC
accents, dynamic messages and accessibility labels use the selected language.
Arabic/Hindi remain preparation only and are not advertised as supported.

Physical draft-prototype results are in reports/localized-draft-device-qa.json
(16 orientation cases). Final normal-app results are separately bound to their
APK in reports/localized-device-qa.json. Advanced layout tests seed prerequisites
and call the VM to open a long/accented puzzle; they do not verify a purchase.
Amazon-delivered minified-release testing and matching console copy remain gates.
