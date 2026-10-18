> Current status — 18 October 2026: production successfully SUBMITTED and under
> Amazon review. Premium promoted from TEST and Approved. All release-gate
> checks passed, including delivered release purchase/cancel/offline/restore QA.
> The earlier draft/pending notes below are historical. See SUBMISSION_READINESS.md.

# Amazon console draft status

**Saved and configured; not submitted or published.**

App ID: `amzn1.devportal.mobileapp.91b139e68f6746fab1bdea19472cd193`

Release ID: `amzn1.devportal.apprelease.2ad95ad758e740b7bf34a38964ee6a3b`

- Title: WordBloom: Word Connect and Puzzle Games
- SKU: `wordbloom.android`
- Category: Games → Words; Casual and Indie refinements
- Free app, with optional one-time premium purchase
- Support contact reused from the publisher's existing RoutineNest submission;
  public repository support and privacy pages linked
- Production APK uploaded and accepted by Amazon validation, version 1; the
  app-specific public verification key is included; DRM is not applied
- Twelve compatible Fire tablet entries selected; zero Fire TV/automotive
  entries. This is console filtering, not runtime certification.
- Factual content-rating questionnaire complete; privacy questionnaire saved for
  review; privacy URL entered; no WordBloom account-creation flow

## Localization

Ten app descriptions are complete: English US/UK, German, Spanish, French,
Italian, Dutch, Polish, Portuguese Brazil/Portugal. Every saved short and long
text was checked after reopening against `amazon-metadata.json`. Titles, five
feature bullets and keywords are filled. Amazon tokenizes/deduplicates keywords.

Store localization is implemented. **Gameplay remains English UK/US only.**
Descriptions explicitly state this. Non-English interface and gameplay packs
still require implementation and linguistic review. Native editorial review of
listing translations is pending.

## Premium item

`wordbloom.premium` is an Entitlement, ready to submit but unsubmitted. The
proposed base price from the build prompt, £3.99, is saved with Amazon-calculated
marketplace conversions. Seven IAP text localizations are filled: English US/UK,
German, Spanish, French, Italian and Portuguese Brazil. The IAP console did not
offer Dutch, Polish or European Portuguese. Two branded icons are attached to
the default IAP locale. Exact copy is in `amazon-iap-metadata.json`.

## Artwork and review status

The 512px and 114px icons and 1024×500px promotional artwork are uploaded and
visible after reopening. Dimensions, hashes and status are in
`upload-assets.json`. Watermarked render concepts were excluded. No debug or
development APK was uploaded.

The required draft form is complete as of 17 October 2026:

- All ages was explicitly selected by the publisher and saved.
- Three 1280×800 PNG listing images were uploaded and accepted. They use fresh,
  independent production Compose renders, not device captures. Each preserves
  the actual UI and puzzle content and has a small source label. Source pixels,
  export dimensions and hashes are recorded in `premium-screenshots.json`.
- The publisher explicitly confirmed Amazon's export-compliance declaration.
  Its checkbox is checked in the open review form. The Submit App button became
  enabled. No submission action was performed.

The earlier watermarked concepts remain excluded. The new listing images are
separate exports requested by the publisher; their use closes the image-count
form gap and does not establish physical-device runtime behavior.

Real-device testing, live purchase checks, name clearance, native editorial
review and publisher review of privacy/SDK disclosures remain release gates.

Reviewer testing instructions are filled. The actual Amazon-generated sandbox product
file is saved as `content/amazon.sdktester.json`, and all seven exported purchase
descriptions were checked against the source copy. Amazon DevTest registration
requires a test certificate and a local device; it was not registered here.
Final app/IAP submission and publication have not been performed.

`reports/amazon-console-verification.json` records the verified draft state.
Local Amazon-form screenshots are in ignored `dist/`, outside source control.

Official rules: https://developer.amazon.com/docs/app-submission/appstore-details.html

## Publisher decisions on 18 October 2026

The publisher confirmed privacy/SDK disclosures and the WordBloom name, and
requires all eight additional gameplay locale editions before the first release.
The current English-only console APK must therefore remain unsubmitted even
though its form is complete. Translation infrastructure and original vocabulary
drafts are prepared locally; provider access, native approval, multilingual
runtime checks, matching final assets/copy and Amazon store-delivered testing
remain open. See `publisher-decisions.json` and `../localization/REVIEW_HANDOFF.md`.
Normal-debug Amazon App Tester purchase/cancel/restore/refund/account-switch
checks were executed on the connected Fire; this does not certify the final
minified Amazon-delivered candidate.

18 October 2026: DeepL draft generation is complete for all eight additional
locales. No new translations or APK were uploaded; native linguistic approval,
activation, multilingual device tests and matching final listing copy remain
required. The current console draft stays unsubmitted.


18 October editorial/lexical review: all ten locales are active in the local
candidate, with method and hashes in localization/approvals.json. This is AI
editorial/lexical validation, not native human certification. Current console
APK/copy still describe English gameplay. No new console upload, invitation,
submission or publication is implied by the local tablet results. Final
multilingual metadata/assets and candidate upload remain required before LAT.


## Current verified multilingual draft — 18 October 2026

Supersedes the earlier English-only progress notes above. Final multilingual APK
uploaded and verified by downloading the same bytes. All ten app descriptions
and seven IAP descriptions reopened and verified. Language fields include all
eight language families covering the ten locale editions. Three refreshed
current-UI listing renders uploaded, downloaded and hash-verified in correct
order. Updated reviewer instructions saved and verified; all-ages/export choices
remain as approved. Premium entitlement submitted to TEST only and Approved;
not promoted to live. Production app remains Ready to Submit and unsubmitted.
Private LAT setup and tester invitation are separate; no tester email is stored
in public source or reports. See reports/multilingual-console-update.json.

Private LAT submitted from the final saved candidate:
`amzn1.devportal.apprelease.1769101893d64945abd182c5142ea1d7`, channel
`amzn1.channel.7716864723744cf9a2a83689261b515a`. Only WordBloom Fire Release
Test is selected, with one authorized adult tester. Amazon shows Test in-progress;
the tester invitation is Delivered. No tester address is stored publicly.
This private test is separate from the unsubmitted production release.

Amazon-delivered minified release installed from com.amazon.venezia on KFTRWI.
Delivered code, resources, language assets and native libraries match the uploaded
candidate; the APK is not debuggable. Live GBP3.99 price, purchase cancellation,
restore without a purchase, multilingual puzzle completions, English progress
sharing and offline restart persistence passed. Successful premium purchase and
restore of its owned receipt remain pending the publisher's Verify and Pay step.
See reports/live-app-test.json for exact scope; production remains unsubmitted.

Amazon-delivered purchase/restore QA is now complete: the publisher completed
Verify and Pay; premium unlocked, a new puzzle awarded +4 hints, premium survived
an offline restart, and Restore Amazon purchase recovered the entitlement after
pm clear removed local app data. No sandbox property was enabled. This supersedes
the pending purchase notes above. Public submission is the remaining console
action, subject to tools/release_gate.py passing.
