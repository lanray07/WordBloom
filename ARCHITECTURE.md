# Architecture

`engine` is pure Kotlin/JVM: curated vocabulary, deterministic connected crosswords, letter inventory checks, tile tracing, answer verdicts, English command parsing and entitlement rules. It has no Android or networking dependency.

`GameViewModel` exposes immutable StateFlow state. `Storage.kt` owns Room and DataStore. The DAO commits puzzle discoveries and hint balances together, before rewards appear in the UI. Historical completion survives replay. The UI offers accessible clickable letter tiles and typed words alongside swiping.

`MainActivity.kt` contains responsive Compose screens and original Canvas landscapes. `Voice` owns recogniser/TTS lifecycles and transient focus; `Ambience` owns the local looping soundscape and music focus. Recognition stops on pause. `Billing` exposes a shared state/refresh/purchase/restore contract. `AmazonBilling` is the normal adapter; its private cache is scoped to Amazon user ID and account changes discard incomplete receipt pages. Variant-specific factories prevent the release build from referencing development purchasing. Only an explicit development debug property selects the isolated, clearly labelled `.dev` adapter.

`tools` contains localisation, pack validation and capture/composition workflows. `localization` contains the activation registry, glossary, cache and pending review report. `assets` contains editable original SVGs; `store` contains honest English listing drafts and a capture manifest.

Only curated vocabulary is accepted. Generator failure is explicit if any target cannot be placed. NFC/locale case conversion is available, but current tile input is not a grapheme-cluster engine for Arabic/Hindi. Those locales are not enabled.

Daily IDs contain locale and local date. An open daily puzzle remains pinned until navigation changes, and already completed dates remain saved across timezone changes. Offline clock tampering cannot be authoritatively prevented. No competitive streak depends on clock trust.

All first puzzle completions, including daily play, contribute persistent garden blooms and achievements. Chapter unlocking and journey percentage use normal story completion only. Current interface/puzzle editions are English and explicitly use left-to-right layout, so an RTL device locale cannot mirror Latin crossword cells or wheel hit coordinates. Arabic/Hindi activation still requires the documented script and input work.

Future work: split the growing Compose UI into screen files, externalise remaining dynamic/adapter messages, and compile larger reviewed puzzle packs at development time. Keep the pure engine independent.
