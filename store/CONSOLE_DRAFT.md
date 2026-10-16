# Amazon console draft preparation

The new-app form is open in the signed-in Amazon Developer console. Entered:

- App title: WordBloom – Word Connect & Puzzle Games
- SKU: `wordbloom.android`
- Category: Games → Words
- Refinements: Casual, Indie

**Not saved or submitted.** Amazon requires a public customer-support email before
creating the app record. The account has no default support contact. No personal
email address has been inferred from Git configuration or published as support.
The public support email and any support/privacy URLs must come from the owner.

`amazon-metadata.json` contains ten localized listing drafts (en-GB, en-US, es,
fr, de, it, pt-BR, pt-PT, nl, pl). `tools/prepare_store_metadata.py` regenerates
them and checks current documented description and feature limits. Enter only
languages offered by the Amazon console. Store-text localization is separate
from app-interface localization and gameplay dictionaries. All these descriptions
explicitly identify English puzzles. Native linguistic review is pending.

Copy uses direct product identification, natural word-connect/crossword/offline
search terms, concrete counts and clear free/premium boundaries. These choices
help readers and search systems understand the app; ranking or inclusion in AI
answers cannot be guaranteed. No medical, learning-outcome or unsupported game
mode claims are included.

`upload-assets.json` records the selected 114px and 512px icons and 1024×500px
promotional image, with dimensions and hashes. Artwork was visually inspected.
No asset has been uploaded yet because the app record does not exist. Watermarked
concept screenshots are excluded. The required real-device screenshots remain
missing. No test/development APK is selected for upload.

The app remains free to start with an optional one-time premium purchase. No
price, distribution-country choice, rating/legal attestation, privacy URL, or
production signing identity has been invented. Do not submit the app for review
or publish it without the owner's explicit instruction.

Source for field/asset rules, checked 16 October 2026:
https://developer.amazon.com/docs/app-submission/appstore-details.html
