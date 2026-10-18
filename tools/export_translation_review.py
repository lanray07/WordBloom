"""Validate generated drafts and export reviewer-friendly interface CSVs.

CSV approval cells are deliberately blank. The JSON bundles remain authoritative;
this report establishes structural validity, not linguistic approval.
"""
import csv
import hashlib
import json
import re
import unicodedata
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path

from localize import QUALIFIERS, strings, validate

ROOT = Path(__file__).resolve().parents[1]


def main():
    source = json.loads((ROOT / 'localization/ui-source.json').read_text(encoding='utf-8'))['entries']
    resource_source = strings()
    required = json.loads((ROOT / 'localization/approvals.json').read_text())['required_for_release']
    provider = json.loads((ROOT / 'localization/report.json').read_text(encoding='utf-8'))
    report = {'provider': 'DeepL', 'status': 'generated drafts; native review required',
              'linguistic_approval': False, 'locales': {},
              'listing_scope': 'Translations retain the current English-only gameplay disclosure. Revise copy after reviewed packs are activated.'}
    folder = ROOT / 'localization/review'
    folder.mkdir(exist_ok=True)
    for locale in required:
        path = ROOT / f'localization/generated/messages/{locale}.json'
        bundle = json.loads(path.read_text(encoding='utf-8'))
        entries = bundle['entries']
        assert bundle['locale'] == locale and set(entries) == set(source), locale
        assert provider['languages'][locale]['missing'] == [], locale
        flags = []
        rows = []
        for name, item in entries.items():
            assert item['source'] == source[name]['source'], (locale, name)
            validate(item['source'], item['translation'])
            assert not re.search(r'</?(?:x|keep)\b', item['translation']), (locale, name)
            if 'WordBloom' in item['source']:
                assert 'WordBloom' in item['translation'], (locale, name)
            warnings = []
            visible = re.sub(r'\{\d+\}|%(?:\d+\$)?[sdf]', '', item['source'])
            if (item['translation'] == item['source'] and item['source'] != 'WordBloom'
                    and re.search(r'[A-Za-z]{3}', visible)):
                warnings.append('unchanged text: verify shared terminology or missed translation')
            if len(item['translation']) > 1.7 * max(1, len(item['source'])) and len(item['source']) >= 8:
                warnings.append('expanded text: inspect device wrapping')
            if unicodedata.normalize('NFC', item['translation']) != item['translation']:
                warnings.append('non-NFC text: review accent normalization')
            if warnings:
                flags.append({'id': name, 'source': item['source'], 'translation': item['translation'], 'flags': warnings})
            rows.append([name, item['source'], item['translation'], '; '.join(source[name]['locations']),
                         '; '.join(warnings), '', ''])
        xml = ROOT / ('localization/generated/values-' + QUALIFIERS.get(locale, locale)) / 'strings.xml'
        resources = {node.attrib['name']: node.text or '' for node in ET.parse(xml).getroot()}
        assert set(resources) == set(resource_source), locale
        for name, value in resources.items():
            validate(resource_source[name], value)
        with (folder / f'{locale}.csv').open('w', encoding='utf-8-sig', newline='') as stream:
            writer = csv.writer(stream)
            writer.writerow(['id', 'English source', 'Draft translation', 'Source locations', 'Automated flags', 'Reviewer notes', 'Approved by reviewer'])
            writer.writerows(rows)
        report['locales'][locale] = {'interface_entries': len(entries), 'all_source_entries': len(provider['languages'][locale]['checks']),
                                     'missing_entries': 0, 'bundle_sha256': hashlib.sha256(path.read_bytes()).hexdigest(),
                                     'review_flags': flags}
    (ROOT / 'reports/translation-generation.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    files = list(folder.rglob('*'))
    files += [ROOT / name for name in ['localization/REVIEW_HANDOFF.md', 'localization/gameplay-drafts.json',
                                      'localization/voice-command-drafts.json', 'localization/approvals.json',
                                      'reports/translation-generation.json', 'reports/translation-refinements.json',
                                      'reports/word-frequency-review.json', 'DICTIONARY_LICENSES.md']]
    files += list((ROOT / 'reports/third-party').glob('*'))
    for locale in required:
        files.append(ROOT / f'localization/generated/messages/{locale}.json')
        files += list((ROOT / f'localization/generated/store/{locale}').glob('*'))
    destination = ROOT / 'dist/WordBloom-language-review.zip'
    destination.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(destination, 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(set(files)):
            if path.is_file():
                archive.write(path, path.relative_to(ROOT))
    print('Validated all eight drafts and exported interface review CSVs; no linguistic approvals granted.')


if __name__ == '__main__':
    main()
