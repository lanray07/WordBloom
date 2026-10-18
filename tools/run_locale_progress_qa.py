"""Verify persisted locale boundaries on the dedicated normal-debug QA install.

Clears only WordBloom data for each pair. Makes no purchases. Run after installing
the matching normal app and instrumentation APKs with the language matrix.
"""
import argparse
import hashlib
import json
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--adb', required=True)
    parser.add_argument('--serial', required=True)
    args = parser.parse_args()

    def adb(*values):
        return subprocess.check_output([args.adb, '-s', args.serial, *values], timeout=180, stderr=subprocess.STDOUT).decode('utf-8', errors='replace')

    original = {name: adb('shell', 'settings', 'get', 'system', name).strip()
                for name in ['font_scale', 'accelerometer_rotation', 'user_rotation']}
    report = {'date': '2026-10-18', 'cases': [], 'scope': 'Normal debug real UI and persisted progress; no purchase.',
              'apk_sha256': hashlib.sha256((ROOT / 'app/build/outputs/apk/debug/app-debug.apk').read_bytes()).hexdigest()}
    try:
        for name, value in [('font_scale', '1.0'), ('accelerometer_rotation', '0'), ('user_rotation', '1')]:
            adb('shell', 'settings', 'put', 'system', name, value)
        for source, target, shared in [('en-GB', 'de', False), ('pt-BR', 'pt-PT', False), ('en-GB', 'en-US', True)]:
            assert 'Success' in adb('shell', 'pm', 'clear', 'com.wordbloom.garden')
            result = adb('shell', 'am', 'instrument', '-w', '-e', 'class', 'com.wordbloom.garden.LocaleProgressDeviceTest',
                         '-e', 'localeProgress', 'true', '-e', 'sourceLocale', source, '-e', 'targetLocale', target,
                         '-e', 'shared', str(shared).lower(), 'com.wordbloom.garden.test/androidx.test.runner.AndroidJUnitRunner')
            passed = 'OK (1 test)' in result and 'FAILURES' not in result
            path = ROOT / f'reports/language-tests/progress-{source}-{target}.txt'
            path.parent.mkdir(exist_ok=True)
            path.write_text('\n'.join(line.rstrip() for line in result.replace('\r', '').strip().splitlines()) + '\n', encoding='utf-8')
            report['cases'].append({'source': source, 'target': target, 'shared': shared, 'passed': passed, 'evidence': path.relative_to(ROOT).as_posix()})
            print(f'{source} -> {target}: ' + ('PASS' if passed else 'FAIL'), flush=True)
            assert passed, result
    finally:
        for name, value in original.items():
            adb('shell', 'settings', 'put', 'system', name, value)
        report['passed'] = len(report['cases']) == 3 and all(case['passed'] for case in report['cases'])
        (ROOT / 'reports/locale-progress-qa.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()
