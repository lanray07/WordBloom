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

## Artwork and remaining fields

The 512px and 114px icons and 1024×500px promotional artwork are uploaded and
visible after reopening. Dimensions, hashes and status are in
`upload-assets.json`. Watermarked render concepts were excluded. No debug or
development APK was uploaded.

Amazon's final review screen identifies three outstanding form gaps:

1. Target audience: select at least one age group. The publisher has been asked
   to choose; inclusion of children invokes Amazon's certification.
2. At least three tablet screenshots. No connected device is available, and the
   emulator cannot boot here. Compose renders remain clearly marked concepts.
3. Export-compliance certification requires publisher review and remains unchecked.

Real-device testing, live purchase checks, name clearance, native editorial
review and publisher review of privacy disclosures remain release gates.
Reviewer testing instructions are filled. The actual Amazon-generated sandbox product
file is saved as `content/amazon.sdktester.json`, and all seven exported purchase
descriptions were checked against the source copy. Amazon DevTest registration
requires a test certificate and a local device; it was not registered here.
Final app/IAP submission and publication have not been performed.

`reports/amazon-console-verification.json` records the verified draft state.
Local Amazon-form screenshots are in ignored `dist/`, outside source control.

Official rules: https://developer.amazon.com/docs/app-submission/appstore-details.html
