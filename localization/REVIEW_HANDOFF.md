# Linguistic review record — 18 October 2026

The required eight additional editions received separate Codex editorial/lexical
review, recorded in approvals.json with review_kind ai_editorial_lexical. All ten
editions are activated. No native human certification is claimed or required by
the original separate-linguistic-validation instruction.

Each approval binds ui-source.json, generated/messages/<locale>.json,
gameplay-drafts.json and voice-command-drafts.json to exact SHA-256 hashes.
The installer refuses missing, incomplete or changed reviews. Run:

```powershell
python tools/install_reviewed_languages.py --check-only
python tools/export_translation_review.py
```

Authoritative vocabulary is gameplay-drafts.json. tools/prepare_language_packs.py
compiles it without resetting edits; do not use --regenerate-drafts after review.
UI corrections belong in editorial-overrides.json so cached provider generation
preserves them. Correct parser commands in engine Puzzle.kt as well as examples.
Changed content needs another actual review and new hashes before installation.
CSV annotations alone do not modify the app.

The review inspected all UI templates, listing prose/captions, words, accents,
inflections, all-ages suitability, difficulty and regional phrasing. It replaced
17 target sets and corrected purchase/speech terminology, counters, formality,
chapter names and Portuguese commands. See reports/editorial-review.json and
reports/editorial-word-changes.json. All 614 word observations were inspected;
ten low-frequency flags were adjudicated with primary dictionaries in
reports/rare-word-adjudication.json. No dictionary definitions/corpus is bundled.
Further fluent regional and audience/difficulty review would increase confidence.

The archive dist/WordBloom-language-review.zip contains CSVs, bundles, authored
vocabulary, commands, review methods and evidence. Approval cells reflect current
hash-bound review records; they do not imply human review. Multilingual marketing copy is now saved and verified in the Amazon draft. Final normal-app tablet QA is recorded apart
from the earlier visibly marked .qa prototype. Amazon-delivered release QA is
still required before submission.
