# Required multilingual release review

The publisher requires German, Spanish, French, Italian, Dutch, Polish,
Portuguese Brazil and Portuguese Portugal before the first release. English-only
submission is not authorized. The privacy disclosures and working name were
approved separately on 18 October 2026; that is not a language review.

## Prepared work

- `ui-source.json` contains the interface resources, dynamic message templates,
  accessibility descriptions and their source locations. `{0}`, `{1}` and other
  placeholders must survive translation, even when their order changes.
- `gameplay-drafts.json` contains 24 original draft word sets per vocabulary
  language. Portuguese regional editions share vocabulary. The engine prepares
  192 edition-specific puzzle IDs across eight locale editions, not 192 unique
  dictionaries. Solvability and accent preservation have automated checks.
- `engine/src/main/kotlin/com/wordbloom/engine/Puzzle.kt` contains the draft
  localized spoken commands. Check these alongside the interface examples.
- The app supports reviewed translation bundles, locale-specific progress and
  daily selection, and regional text-to-speech selection. Unreviewed editions
  stay unavailable in the application.

## Translation generation

Create a DeepL API account and key at https://www.deepl.com/en/your-account/keys.
Set `DEEPL_API_KEY` as a Windows **user** environment variable. Do not put it in
chat, source, APK assets or Git. The script reads that one user variable without
printing it, so an already-running Codex process can use the saved setting.
Free keys ending `:fx` use the Free endpoint; other keys use the Pro endpoint.

```powershell
python tools/extract_ui_catalog.py
python tools/localize.py --translate
```

Generation uses cached batches and preserves placeholders/product terminology.
It translates interface/marketing copy, **never puzzle answers**. Provider output
is a draft and is not installed automatically.

## Native review needed

Arrange a fluent reviewer for each language, with separate regional checks for
Brazilian/European Portuguese. Review:

1. Every generated interface/message entry in
   `localization/generated/messages/<locale>.json`, plus the localized listing.
2. All target words, spelling, inflections, accents, appropriateness for all ages
   and difficulty ordering in `gameplay-drafts.json`. These are curated allowlists,
   not complete dictionaries. Structural tests do not establish language quality.
3. The spoken command examples and recognition phrases in `voice-command-drafts.json`.

Correct drafts first. Run `python tools/prepare_language_packs.py` to compile the
reviewed JSON vocabulary without overwriting reviewers' edits, then run engine
tests. The `--regenerate-drafts` option explicitly resets that input to the initial
authored drafts and must not be used on reviewed content. Spoken-command
corrections must also be applied to `Puzzle.kt` and re-extracted before approval.
Then record the actual reviewer/date and approval hashes
in `approvals.json`. `ui_source_sha256`, `messages_sha256` and
`gameplay_sha256` and `voice_commands_sha256` bind approval to the reviewed bytes. Do not mark reviews
approved without the review having happened.

```powershell
python tools/install_reviewed_languages.py
```

The installer refuses missing, incomplete or changed reviews before modifying
installed assets. After installation, rebuild, test every locale on the Fire
tablet (including accented/repeated letters, language switching and long labels),
refresh listing copy/assets to match actual support, upload the final candidate,
and run Amazon's store-delivered Live App Testing. Submission remains blocked
until those checks pass. Do not submit the current English-only console APK.

## Frequency evidence

`reports/word-frequency-review.json` contains wordfreq 3.1.1 observations for 488
candidate words, with source/licence attribution and limitations. Twenty
low-frequency words are flagged. Frequency evidence is a review aid, not a
normative dictionary or a language/suitability approval. Portuguese regional
usage needs separate attention. The separate lexical correction log links to
the primary dictionary entry used for the Italian replacement.

To reproduce without installing packages globally:

```powershell
python -m pip install --target dist/qa-python -r tools/requirements-qa.txt
python tools/review_word_frequency.py --runtime dist/qa-python
```
