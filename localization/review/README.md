# Language review package — 18 October 2026

DeepL drafts are ready for German, Spanish, French, Italian, Dutch, Polish,
Portuguese Brazil and Portuguese Portugal. Each edition covers 184 interface
templates and 246 total source entries including resources, listing prose and
screenshot captions. Repeated source text shares a cached translation.

Open the CSV for your language in a spreadsheet or text editor. Each row includes
the English source, draft translation, source locations and automated flags.
Reviewer notes and approval cells are intentionally blank. Flags identify long
text or unchanged wording; common words such as Premium or Dutch hints can be
valid. `EEEE, d MMMM` is a date-format pattern, not ordinary interface prose.

Correct the authoritative `localization/generated/messages/<locale>.json` bundle
as needed; CSV annotations alone do not change the app. Review the generated
listing/captions, all puzzle words and spoken commands too. Keep placeholders
such as `{0}` exactly once wherever required, while changing their order naturally.
Check formality, gender/plurals, puzzle terminology, garden names, accents, all-ages
suitability and Brazilian versus European Portuguese. Check chapter-name
consistency between interface and listing copy. Descriptive garden labels were
refined with DeepL context; they still require native editorial approval.

The generated listings describe the currently installed English-only gameplay.
Their disclosure must be rewritten after all reviewed language packs are activated
and tested. These files are review drafts and have not been uploaded to Amazon.

The gameplay vocabulary is authored separately and is not machine-translated
from English answers. Seven vocabulary drafts supply eight regional editions;
Portuguese editions currently share words. Frequency checks flagged 20 rare
words for special attention and do not establish validity or suitability.

Follow `localization/REVIEW_HANDOFF.md` to record the actual reviewer/date and
hashes after review. No native approval has been inferred from generation or
automated checks. Non-English editions remain disabled until review is complete.

`dist/WordBloom-language-review.zip` collects the CSVs, message bundles, listing
drafts, vocabulary, commands, frequency evidence and review instructions. It
contains no API key or device receipt identifiers.
