# Localisation

Available puzzle editions: English UK and US, sharing 24 common-spelling puzzles. Selection changes voice locale and daily identity. Spanish, French, German, Italian, Brazilian/European Portuguese, Dutch and Polish are explicitly pending dictionary/linguistic review in `localization/packs.json`.

Arabic/Hindi have registry and RTL preparation only. No script-specific input/dictionary support is claimed. The manifest supports RTL and the engine supports NFC and locale case conversion; future packs still require explicit grapheme/accent/tile rules.

## Translation workflow

1. Add source UI strings to Android `values/strings.xml`. Static migration has begun; remaining hardcoded/interpolated messages are audited. Full extraction is not complete.
2. Run `python tools/localize.py` for completeness reports covering UI strings, listing prose and screenshot captions, and an English-US review output without credentials.
3. Set `DEEPL_API_KEY` outside the repo. Optionally set `DEEPL_ENDPOINT` to an approved DeepL HTTPS endpoint. Run with `--translate`.
4. Placeholder tokens and protected product terms use ignored XML nodes. Cache keys include source, locale and glossary. Missing/duplicate placeholders and empty output fail validation. Reports flag all outputs for review and show length ratios.
5. Review `localization/generated`, complete dynamic extraction, install approved Android resources and inspect every locale layout. Generated translations are not shipped automatically.
6. Acquire/review independent language dictionaries. Record provenance, safety/frequency bands and orthography. Run the pack schema gate and add connected engine tests before activation. Never translate English answers to make a pack.

No translation API request was made without credentials. Non-English UI, metadata and screenshot copy remain pending. Native linguistic sign-off and licensed frequency corpora have not been obtained. Keys are never included in Android resources or builds.
