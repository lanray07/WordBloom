# Test report — 18 October 2026

Results below distinguish automated engine checks, physical normal-debug testing,
Amazon sandbox testing and release checks that remain pending.

## Executed checks

- Engine: 11 existing gameplay/generation/entitlement cases, 6 language-pack cases
  and 3 translation-template cases passed. The language-pack tests prepare 192
  solvable locale-edition puzzles, preserve accents and reject unsupported language
  activation. They do not establish lexical correctness or child suitability.
- Development billing contract: 5 JVM cases passed. These exercise an isolated
  simulated adapter; they are separate from the real Amazon SDK sandbox below.
- Python localization/catalogue checks: placeholder preservation, malformed-output
  rejection, nested Kotlin extraction and batched request ordering passed. The
  exact current count is written in the final package section and reports.
- Production API 36 debug, unsigned release and instrumentation builds and lint
  are verified in `build-validation.log`; final artifact checks are below.

## Physical Fire tablet

Connected model: **KFTRWI**, Android 9 / API 28, natural resolution 1200×1920.
`reports/fire-device-qa.json` binds these results to the normal debug APK hash.
The test app was installed fresh for QA; only its own data was reset between
independent cases. No real purchase was made.

| Scenario | Result |
|---|---|
| Landscape tile/text entry, target completion, Room close/reopen persistence | Passed |
| Bonus word, invalid input, hint balance, activity recreation, voice fallback | Passed |
| Real continuous touch swipe and backtracking through the letter wheel | Passed |
| Contrast, reduced motion, larger text and evening theme across recreation | Passed |
| Portrait gameplay/recreation | Passed |
| Gameplay with device Wi-Fi disabled | Passed |
| Portrait at 150% system font size, plus in-app accessibility settings | Passed |

Each UI group asserts the requested orientation inside instrumentation. Public
case transcripts are in `reports/fire-tests/`; device configuration is restored
by the runner. Eight real application QA captures are in `store/raw/tablet/` and
reviewed together in `reports/fire-captures-preview.jpg`. The capture tour hides
the keyboard and waits for the physical frame; it asserts the garden page before
capturing it. These QA captures have not replaced the three accepted listing
render compositions. The image named `07-offline` is a home-screen illustration
for that topic; offline functionality is proved separately by `runtime-wifi-off`.

Manual visual inspection also covered the expert daily puzzle with a found
nine-letter word at 150% system text size, in-app larger text, high contrast and
the evening theme. The letters and controls remained readable; see
`reports/fire-large-text-expert.png`. One older normal-debug cold start measured
about 2.7 seconds and roughly 75 MB PSS. This is an observation, not a release
benchmark or a guarantee across all Fire models.

This tablet has no compatible speech-recognition service. Its unavailable-service
message and complete touch/text fallback passed. Installed TTS engines were
observed. The publisher subsequently replied yes to the English letter-speech
check; this is recorded as publisher-reported clear TTS in
`reports/manual-audio-qa.json`. VoiceView announcement quality and localized
pronunciation still need hands-on confirmation. No continuous/background recording was enabled.

## Amazon App Tester

The normal Amazon debug adapter (SDK 3.0.9) was exercised through the installed
Amazon App Tester with `debug.amazon.sandboxmode=debug`. Verified: account
isolation, SDK price display, dialog cancellation without entitlement, successful
purchase, FULFILLED transaction status, restoration after local data removal,
explicit Restore, and revocation after a cancelled test receipt.
`reports/amazon-sandbox-qa.json` records the APK/source hashes and limits. Sandbox
receipt identifiers stay in ignored local evidence, outside the public reports.

The installed tester used the exported default numeric USD 5.30 even when its
user marketplace was GB. A WordBloom-only GBP 3.99 test fixture verified SDK price
display; other existing products were preserved. This is not a verification of
the live Amazon retail price. The console base price was checked separately.

The original App Tester JSON fixture and User 1 / US selection were restored.
Sandbox mode was disabled, QA app data cleared, and font, rotation, Wi-Fi and
stay-awake settings restored. See `reports/fire-device-cleanup.json`.
The settings test waits for asynchronous DataStore updates before checking saved
values and checks them again after activity recreation.

## Localization review

All ten requested locale editions are active in the normal candidate after
separate Codex AI editorial/lexical review. DeepL supplied eight additional
editions with 179 UI templates and 241 total source entries each. Every UI entry,
listing/caption draft, target/bonus set and displayed command was inspected.
Seventeen word sets were improved. Counter grammar, purchase/speech meanings,
chapter names, register and Portuguese regional commands were corrected.
The method and exact hashes are in localization/approvals.json; native human
certification is not claimed. Further fluent regional/difficulty review would
increase confidence. Structural tests alone do not establish lexical correctness.

Frequency evidence covers 491 non-English and 123 English observations. Five
non-English and five English low-frequency flags were checked against primary
dictionaries and retained with reasons in reports/rare-word-adjudication.json.
No unobserved entries remain. No dictionary definitions/corpus is shipped.

Sixteen earlier draft-prototype tablet cases passed (eight languages in landscape
and portrait at 150% system text). Visual inspection found cramped German controls
at large text; production controls now wrap whole buttons and the board stacks
above the wheel when text is enlarged. Dense-grid letters now use an explicit
line height, correcting clipping seen in physical captures. An Android 9 ICU
regex crash was also fixed.
Final normal-app evidence belongs to reports/localized-device-qa.json, distinct
from reports/localized-draft-device-qa.json. All ten final normal-app portrait
cases passed at 150% system text plus the in-app larger-text option. The real
large-text overview is reports/multilingual-large-text-preview.jpg. Three
real-UI progress-switching cases also passed, including Portuguese isolation and
shared English progress. The switching automation was corrected to dismiss the
keyboard before reaching the header; no app progress fix was necessary.
Advanced layout checks seed only QA
progress and open a long/accented puzzle through a VM call, without changing an
entitlement or purchasing. Locale progress boundaries have a separate real-UI
check. Device screenshots are actual captures, not listing render compositions.

## Remaining release tests

- Amazon store-delivered **minified release** price/purchase/restore testing.
  App Tester is a debug sandbox; it does not certify the production build.
- Hands-on audio/VoiceView checks. Recognition on a service-equipped device is
  not claimed from this Fire tablet's fallback result.

Prior off-device Paparazzi runs produced 20 regression renders and 3 independent
listing renders. Those remain layout evidence, not device captures. Emulator
attempts before the tablet was connected failed at boot; physical testing above
replaces that earlier environment blocker. An isolated signed `.dev` APK is also
provided for developer QA and has not been device-certified or uploaded.

## Final console update — 18 October

The release APK downloaded from Amazon matches the local unsigned candidate
(99db48e41f6ca592c1ccefead4b40dc9f342925a0d2a8afdf0f1fe1cc0ffc7d1).
All ten app descriptions and seven premium descriptions were reopened and
verified. The premium entitlement is Approved for TEST only; it was not promoted
to live. Three fresh current-UI Compose listing render cases passed. Their
compositions were uploaded, downloaded and matched by SHA-256 in connect, journey,
garden order. Multilingual testing instructions survived a reload. Production
Submit App was not clicked. See reports/multilingual-console-update.json.

## Final package verification

Production API 36 debug, release and instrumentation APK builds and lint passed. Lint: 0 errors, 24 warnings (including dependency-version suggestions and intentionally unused preparation assets). Engine: 11 passed; billing contract: 5 passed; localisation: 9 passed; historical visual evidence: 2 suites with 20 earlier renders (not rerun for this candidate). Debug signing verified. Release is unsigned; Amazon applies store signing. The development purchase adapter and Google Play Billing are absent from release DEX. A separate signed .dev APK with explicit simulated-purchase labels is provided for developer QA; its manifest, build flag and signature were verified without device execution.

Fast-swipe tracing now preserves the original down and interpolates crossed tiles; regression tests cover a fast segment, backtracking and a near miss. The Amazon SDK emits stack-map warnings under R8, so the final store-delivered purchase flow still needs Amazon-delivered live verification.

Three refreshed independent Compose screen renders passed for the final listing images on 18 October. Their export dimensions and hashes were checked. These layoutlib renders use the current UI; physical runtime evidence remains separate.

Additional engine checks: language packs 6 passed; message translation 3 passed. All ten locale editions are active after separate hash-bound AI editorial/lexical review, without claiming native human certification.

Seven physical Fire device scenarios passed on the packaged normal debug APK: baseline gameplay/storage, recreation/hints/voice fallback, continuous touch swipes/backtracking, settings persistence, portrait, Wi-Fi disabled and large font. This is one Fire model and an unminified debug build; Amazon-delivered release testing remains pending. Multilingual cases are recorded separately.

All ten locale editions passed the normal-app portrait matrix at 150% system text plus in-app larger text. Locale selection, invalid/valid words, recreation, completion, garden, premium copy and advanced long/accented input were checked. Real screenshots were inspected separately; advanced puzzles use a test VM call and do not certify purchases.

Three real-UI switching checks passed: English/German progress isolation, Brazil/Portugal progress isolation, and intentionally shared UK/US English progress.
