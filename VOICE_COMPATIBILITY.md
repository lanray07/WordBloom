# Voice compatibility

Optional narration uses Android TextToSpeech. Recognition uses Android SpeechRecognizer only after capability detection and a press of Speak. Microphone permission is requested at that point. No continuous/background recording or raw-audio storage is implemented. Listening stops on activity pause and the recogniser is destroyed on teardown.

English commands: Start puzzle; Next level (after completion); Shuffle letters; Give me a hint; Repeat the word; Show my progress; Open daily challenge; Pause game; Go home. A single proposed word passes the same bag/allowlist validation as touch/text input. Ambiguous multiword input produces guidance. Other command locales are not claimed.

TTS uses the chosen English edition and transient audio focus. Listening stops narration, shows listening/recognising/result states and recovers from denied permission, no match, network errors and service failures. The looping ambience responds to audio focus loss. No recognition service is bundled.

Fire tablets must not be assumed to provide Google speech services. If recognition is absent, all gameplay remains available through swipe, tile taps and text. TTS requires a supported installed voice. Offline recognition is not advertised; the device provider may send speech externally or require connectivity. Gameplay/settings disclose this.

Pending device verification: model/service matrix, TTS voice availability, speech round trips, permission flow, interruptions, focus/cancellation timing and microphone hardware. Parser unit tests do not prove audio integration.
