"""Exercise isolated draft or reviewed normal-debug languages; no purchases.

Does not certify Amazon-delivered release behavior or native human review.
"""
import argparse
import hashlib
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--adb', required=True)
    parser.add_argument('--serial', required=True)
    parser.add_argument('--locales', nargs='+')
    parser.add_argument('--orientations', nargs='+', default=['landscape', 'portrait'])
    parser.add_argument('--production', action='store_true', help='Test the reviewed normal debug app after language activation')
    args = parser.parse_args()
    def adb(*values, timeout=60):
        return subprocess.check_output([args.adb, '-s', args.serial, *values], timeout=timeout, stderr=subprocess.STDOUT).decode('utf-8', errors='replace')
    assert adb('get-state').strip() == 'device'
    adb('shell', 'input', 'keyevent', 'KEYCODE_WAKEUP')
    if re.search(r'KeyguardServiceDelegate\s+showing=true', adb('shell', 'dumpsys', 'window', 'policy')):
        raise SystemExit('Unlock the tablet physically before running language QA.')
    original = {name: adb('shell', 'settings', 'get', 'system', name).strip() for name in ['font_scale', 'accelerometer_rotation', 'user_rotation']}
    package = 'com.wordbloom.garden' if args.production else 'com.wordbloom.garden.qa'
    apk = ROOT / ('app/build/outputs/apk/debug/app-debug.apk' if args.production else 'dist/WordBloom-language-qa.apk')
    test = ROOT / ('app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk' if args.production else 'dist/localization-qa-project/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')
    for path in [apk, test]:
        assert 'Success' in adb('install', '-r', str(path))
    required = json.loads((ROOT / 'localization/approvals.json').read_text())['required_for_release']
    if args.production:
        required = json.loads((ROOT / 'store/publisher-decisions.json').read_text())['required_gameplay_locales']
    locales = args.locales or required
    assert set(locales) <= set(required)
    summary = {'date': '2026-10-18', 'application_id': package, 'not_for_release': not args.production,
               'device': adb('shell', 'getprop', 'ro.product.model').strip(),
               'apk_sha256': hashlib.sha256(apk.read_bytes()).hexdigest(), 'cases': [],
               'scope': 'Draft language UI/input/progress checks in isolated QA copy. Long/accented puzzle selection bypasses the paywall through a VM test call; no entitlement cache changed, no purchase made. Not native certification or production release QA.'}
    path = ROOT / ('reports/localized-device-qa.json' if args.production else 'reports/localized-draft-device-qa.json')
    try:
        for locale in locales:
            for orientation in args.orientations:
                assert orientation in ['landscape', 'portrait']
                name = ('production-' if args.production else '') + locale + '-' + orientation
                adb('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
                adb('shell', 'settings', 'put', 'system', 'user_rotation', '1' if orientation == 'landscape' else '0')
                adb('shell', 'settings', 'put', 'system', 'font_scale', '1.0' if orientation == 'landscape' else '1.5')
                assert 'Success' in adb('shell', 'pm', 'clear', package)
                result = adb('shell', 'am', 'instrument', '-w', '-e', 'class', 'com.wordbloom.garden.LocalizationDeviceTest',
                             '-e', 'localizationQA', 'true', '-e', 'reviewedProduction', str(args.production).lower(), '-e', 'locale', locale, '-e', 'orientation', orientation,
                             package + '.test/androidx.test.runner.AndroidJUnitRunner', timeout=240)
                public = ROOT / 'reports/language-tests'
                public.mkdir(exist_ok=True)
                (public / (name + '.txt')).write_text('\n'.join(line.rstrip() for line in result.replace('\r', '').strip().splitlines()) + '\n', encoding='utf-8')
                passed = 'OK (1 test)' in result and 'FAILURES' not in result
                captures = ROOT / 'dist/language-device-evidence' / name
                captures.mkdir(parents=True, exist_ok=True)
                try:
                    adb('pull', f'/sdcard/Android/data/{package}/files/language-qa/{locale}/.', str(captures))
                except subprocess.CalledProcessError:
                    if passed:
                        raise
                summary['cases'].append({'locale': locale, 'orientation': orientation, 'system_font_scale': 1.0 if orientation == 'landscape' else 1.5,
                                         'in_app_larger_text': args.production and orientation=='portrait',
                                         'passed': passed, 'evidence': 'reports/language-tests/' + name + '.txt', 'local_captures': captures.relative_to(ROOT).as_posix()})
                print(name + ': ' + ('PASS' if passed else 'FAIL'), flush=True)
                if not passed:
                    raise AssertionError(result)
    finally:
        restored = []
        pending_restore = {}
        for name, value in original.items():
            try:
                adb('shell', 'settings', 'put', 'system', name, value)
                restored.append(name)
            except Exception:
                pending_restore[name] = value
        summary['restored_settings'] = restored
        summary['settings_restore_pending'] = pending_restore
        summary['all_requested_cases_passed'] = len(summary['cases']) == len(locales) * len(args.orientations) and all(case['passed'] for case in summary['cases'])
        summary['passed'] = summary['all_requested_cases_passed']
        summary['locales_passed'] = [locale for locale in locales if len([case for case in summary['cases'] if case['locale']==locale and case['passed']])==len(args.orientations)]
        if args.production:
            summary['scope'] = 'Reviewed normal Amazon debug app with actual activated locale assets. Advanced input/layout cases bypass the paywall through a VM test call; no entitlement cache changed or purchase made. This is unminified device QA, not Amazon-delivered release certification.'
        path.write_text(json.dumps(summary, indent=2) + '\n')


if __name__ == '__main__':
    main()
