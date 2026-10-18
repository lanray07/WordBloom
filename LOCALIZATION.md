# Localisation — 18 October 2026

The first release must support English UK/US, German, Spanish, French, Italian,
Dutch, Polish and Portuguese Brazil/Portugal. The publisher explicitly declined
an English-only release. The console APK remains English-only and must not be
submitted as the multilingual release.

The app now has a shared translation catalogue for interface text, dynamic
messages and accessibility labels, with locale-aware dates, voice language,
word commands, daily selection and saved journeys. Approved language registration
and translation assets are installed together. English editions share the
existing journey; draft language editions use namespaced progress IDs.

`localization/ui-source.json` contains 184 source templates and locations.
`tools/extract_ui_catalog.py` handles nested Kotlin interpolation and Android
resource whitespace. `tools/localize.py` translates resources, message templates,
listing prose and captions in cached batches. It reads `DEEPL_API_KEY` from the
process or the Windows user environment without printing it. No key enters the
app or repository. DeepL generation completed on 18 October: all eight additional
locales have 184 interface templates and 246 total source entries with no missing
entries. The local key was loaded only into the generation process.
`localization/review/` contains per-language CSVs;
`reports/translation-generation.json` records structural checks and review flags.

Seven authored vocabulary drafts supply 24 word sets each; the Portuguese draft
is shared by the two regional editions. Eight locale editions produce 192 draft
puzzle IDs. Target words are not translations of English answers. Tests cover
letter bags, connected layouts, Unicode accents and activation safeguards.
Difficulty labels are initial length bands and require native frequency/order
review. These are curated allowlists, not comprehensive dictionaries.

Native UI/vocabulary/voice-command review has not happened. Drafts stay disabled.
`tools/install_reviewed_languages.py` requires complete translations and named,
dated approvals bound to source/translation/vocabulary/command hashes before
installing assets or updating `ApprovedLanguages`. See
[the review handoff](localization/REVIEW_HANDOFF.md) for exact inputs and commands.
After activation, every locale still needs device layout, accented input,
language-switching and purchase-copy checks, followed by updated store metadata
and screenshots. Structural tests alone do not certify language quality.

Arabic/Hindi remain preparation only; neither script-specific input nor puzzle
support is claimed. Store descriptions are already localized into ten locales,
but currently and accurately describe English gameplay. They must be updated
when the reviewed multilingual candidate is actually enabled and tested.
