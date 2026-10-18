# Dictionary and asset provenance

No competitor puzzle database or proprietary level sequence is copied. No raw external dictionary or frequency corpus is bundled in the Android application. English targets and bonus lists in `engine/src/main/kotlin/com/wordbloom/engine/Puzzle.kt` are authored selections of ordinary words for this project. There is no third-party word-list licence to reproduce. The owner may use/modify the selection and arrangements; separate content review is recorded in reports/editorial-review.json.

Non-English targets are authored curated selections, not a redistributed dictionary. Source/method, frequency attribution, spelling checks, reviewer and validation evidence are recorded in localization/approvals.json and reports/editorial-review.json. `tools/validate_pack.py` requires provenance/reviewer fields but does not replace linguistic review or connected-layout tests.

Garden SVGs, Android Canvas scenes and locally synthesised ambient audio are original project assets. No stock photography, recorded samples or downloaded font files are bundled. System fonts are selected at runtime. See `assets/LICENSE.txt` and the reproducible soundtrack generator `tools/generate_ambience.py`.

Amazon SDK, AndroidX and Kotlin dependencies retain their own licences. Generate and review a third-party notices inventory from the resolved release graph before distribution, including Amazon SDK terms.

## Development frequency review added 18 October 2026

The separate QA tool uses **wordfreq 3.1.1**, by **Robyn Speer**, to look up
frequencies for authored candidate words. Its code is Apache 2.0; its included
data has CC BY-SA 4.0 attribution/share-alike terms and additional attribution
notes. See the preserved upstream notices in `reports/third-party/` and
https://github.com/rspeer/wordfreq/blob/master/NOTICE.md. Credit also goes to the
SUBTLEX authors Marc Brysbaert et al. (freely available data), Leeds Internet
Corpus, Wikipedia, ParaCrawl, OPUS OpenSubtitles 2018 / OpenSubtitles and Google
Books Ngrams. The derived frequency report is shared under CC BY-SA 4.0, with
those source credits. The full corpus and Python packages stay in ignored local
QA storage, outside the APK and delivery source archive.

This corpus is a usage snapshot through about 2021. Observed frequency does not
establish standard spelling, regional preference, child suitability or complete
dictionary membership. The current report checks 491 non-English and 123 English
word observations (Portuguese editions share vocabulary). Seventeen target sets
were improved in a separate editorial pass. Five non-English and five English
rarity flags remain in the corpus report and have primary-dictionary
adjudications in reports/rare-word-adjudication.json. Corpus frequency alone is
not linguistic approval. The separate AI editorial/lexical review method and
limitations are recorded honestly; no native human certification is claimed.
