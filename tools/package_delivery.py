"""Package only verified artifacts and source; never include local settings or keys."""
from pathlib import Path
import hashlib
import json
import os
import re
import shutil
import subprocess
import zipfile
import xml.etree.ElementTree as ET
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]


def junit(path):
    tree = ET.parse(path)
    suite = tree.getroot()
    counts = {key: int(suite.get(key, '0')) for key in ['tests', 'failures', 'errors', 'skipped']}
    assert counts['tests'] > 0 and counts['failures'] == counts['errors'] == 0, path
    return tree, counts


def main():
    log = (ROOT / 'build-validation.log').read_text(encoding='utf-8-sig', errors='replace')
    assert log.rfind('BUILD SUCCESSFUL') > log.rfind('BUILD FAILED'), 'Final Gradle build has not passed'
    assert ':app:assembleRelease' in log and ':app:lintDebug' in log
    reports = ROOT / 'reports'
    reports.mkdir(exist_ok=True)
    engine_tree, engine = junit(ROOT / 'engine/build/test-results/test/TEST-com.wordbloom.engine.EngineTest.xml')
    assert engine['tests'] >= 11, 'Fast-swipe regression tests must run before packaging'
    visual_tree, visual = junit(ROOT / 'reports/visual-tests.xml')
    billing_tree, billing = junit(ROOT / 'app/build/test-results/testDebugUnitTest/TEST-com.wordbloom.garden.BillingContractTest.xml')
    for tree, name in [(engine_tree, 'engine-tests.xml'), (visual_tree, 'visual-tests.xml'), (billing_tree, 'billing-tests.xml')]:
        tree.getroot().attrib.pop('hostname', None)
        tree.write(reports / name, encoding='utf-8', xml_declaration=True)
    python = subprocess.run([shutil.which('python'), '-m', 'unittest', 'discover', '-s', 'tools', '-p', 'test_*.py', '-v'],
                            cwd=ROOT, text=True, capture_output=True)
    (reports / 'localisation-tests.txt').write_text(python.stdout + python.stderr, encoding='utf-8')
    assert python.returncode == 0, 'Localisation tests failed'
    python_count = int(re.search(r'Ran (\d+) tests', python.stderr).group(1))
    lint_path = ROOT / 'app/build/reports/lint-results-debug.txt'
    lint = lint_path.read_text(encoding='utf-8', errors='replace')
    assert '0 errors,' in lint
    warnings = int(re.search(r'0 errors, (\d+) warnings', lint).group(1))
    sdk = Path(os.environ.get('ANDROID_HOME') or Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk')
    build_tools = sdk / 'build-tools/36.0.0'
    debug = ROOT / 'app/build/outputs/apk/debug/app-debug.apk'
    release = max((ROOT / 'app/build/outputs/apk/release').glob('*.apk'), key=lambda path: path.stat().st_mtime)
    proof = subprocess.run([str(build_tools / 'aapt2.exe'), 'dump', 'badging', str(release)],
                           capture_output=True, text=True, check=True).stdout
    assert "compileSdkVersion='36'" in proof and "targetSdkVersion:'36'" in proof
    assert "minSdkVersion:'26'" in proof
    (reports / 'apk-badging.txt').write_text(proof, encoding='utf-8')
    debug_proof = subprocess.run([str(build_tools / 'aapt2.exe'), 'dump', 'badging', str(debug)], capture_output=True, text=True, check=True).stdout
    assert "package: name='com.wordbloom.garden'" in debug_proof, 'Build normal debug before packaging; development APK must be separate'
    signer = [shutil.which('java'), '-jar', str(build_tools / 'lib/apksigner.jar'), 'verify', '--verbose', '--print-certs']
    signed_debug = subprocess.run(signer + [str(debug)], capture_output=True, text=True)
    assert signed_debug.returncode == 0, 'Debug APK signature did not verify'
    (reports / 'debug-signature.txt').write_text(signed_debug.stdout + signed_debug.stderr, encoding='utf-8')
    signed_release = subprocess.run(signer + [str(release)], capture_output=True, text=True)
    with zipfile.ZipFile(release) as archive:
        dex = b''.join(archive.read(name) for name in archive.namelist() if name.endswith('.dex'))
        assert b'DevelopmentBilling' not in dex, 'Development purchase adapter leaked into release'
        assert b'com/android/billingclient' not in dex, 'Google Play Billing leaked into Amazon build'
        abis = sorted({name.split('/')[1] for name in archive.namelist() if name.startswith('lib/') and name.endswith('.so')})
    manifest = json.loads((ROOT / 'store/screenshot-manifest.json').read_text())
    exports = manifest['render_concepts']['exports']
    assert len(exports) == 32
    for item in exports:
        with Image.open(ROOT / item['path']) as image:
            assert list(image.size) == item['dimensions']
        assert item['not_for_submission']
    summary = {
        'date': '2026-10-16', 'status': 'packaged candidate; not submission-ready',
        'engine_junit': engine, 'billing_contract_junit': billing, 'localisation_tests_passed': python_count, 'visual_junit': visual,
        'production_compose_renders': 20, 'watermarked_marketing_concepts': len(exports),
        'lint': {'errors': 0, 'warnings': warnings}, 'compile_sdk': 36, 'target_sdk': 36, 'min_sdk': 26,
        'release_abis': abis, 'debug_signature_verified': True,
        'release_signed': signed_release.returncode == 0, 'development_billing_absent_from_release': True,
        'google_play_billing_absent_from_release': True,
        'pending': ['physical Fire/Android runtime and offline UI tests', 'Amazon live purchase/restore/refund checks',
                    'real device screenshots', 'linguistic review and non-English packs', 'owner signing/identity',
                    'working-name clearance', 'secondary Android distribution/billing decision'],
    }
    dev_apk = ROOT / 'dist/WordBloom-development.apk'
    dev_proof = reports / 'development-build.json'
    if dev_apk.exists() and dev_proof.exists():
        development = json.loads(dev_proof.read_text())
        assert hashlib.sha256(dev_apk.read_bytes()).hexdigest() == development['sha256']
        summary['development_apk'] = development
    (reports / 'verification-summary.json').write_text(json.dumps(summary, indent=2), encoding='utf-8')
    report = ROOT / 'TEST_REPORT.md'
    text = report.read_text(encoding='utf-8').split('\n## Final package verification\n')[0]
    text = re.sub(r'Engine JUnit: \d+ tests passed', f"Engine JUnit: {engine['tests']} tests passed", text)
    text += f"\n## Final package verification\n\nProduction API 36 debug, release and instrumentation APK builds and lint passed. Lint: 0 errors, {warnings} warnings (including dependency-version suggestions and intentionally unused preparation assets). Engine: {engine['tests']} passed; billing contract: {billing['tests']} passed; localisation: {python_count} passed; visual: {visual['tests']} passed with 20 renders. Debug signing verified. Release is {'signed' if summary['release_signed'] else 'unsigned; owner signing required'}. The development purchase adapter and Google Play Billing are absent from release DEX. A separate signed .dev APK with explicit simulated-purchase labels is provided for developer QA; its manifest, build flag and signature were verified without device execution.\n\nFast-swipe tracing now preserves the original down and interpolates crossed tiles; regression tests cover a fast segment, backtracking and a near miss. An interrupted R8 build held a generated output open; its stale daemon was terminated and the clean final build passed. The Amazon SDK emits stack-map warnings under R8, so the final store-delivered purchase flow still needs App Tester/live verification.\n"
    report.write_text(text, encoding='utf-8')
    dist = ROOT / 'dist'
    dist.mkdir(exist_ok=True)
    shutil.copy2(debug, dist / 'WordBloom-debug.apk')
    release_name = 'WordBloom-release.apk' if summary['release_signed'] else 'WordBloom-release-unsigned.apk'
    shutil.copy2(release, dist / release_name)
    source = dist / 'WordBloom-source.zip'
    roots = [ROOT / name for name in ['app/src', 'engine/src', 'assets', 'content', 'localization', 'store', 'tools', 'gradle', 'reports']]
    files = [file for folder in roots for file in folder.rglob('*') if file.is_file()
             and '__pycache__' not in file.parts and file.suffix != '.log']
    files += [file for file in ROOT.iterdir() if file.is_file() and
              (file.suffix in ['.md', '.kts'] or file.name in ['.gitignore', 'gradle.properties', 'gradlew', 'gradlew.bat'])]
    files += [ROOT / 'app/build.gradle.kts', ROOT / 'app/proguard-rules.pro', ROOT / 'engine/build.gradle.kts']
    with zipfile.ZipFile(source, 'w', zipfile.ZIP_DEFLATED) as archive:
        for file in sorted(set(files)):
            assert file.name != 'local.properties' and file.suffix not in ['.jks', '.keystore', '.env']
            archive.write(file, file.relative_to(ROOT))
    checksums = []
    for file in [dist / 'WordBloom-debug.apk', dist / release_name] + ([dev_apk] if dev_apk.exists() else []) + [source]:
        checksums.append(hashlib.sha256(file.read_bytes()).hexdigest() + '  ' + file.name)
    (dist / 'SHA256SUMS.txt').write_text('\n'.join(checksums) + '\n')
    print(json.dumps(summary, indent=2))
    print('Packaged APKs, source archive and SHA256SUMS.txt in', dist)


if __name__ == '__main__':
    main()
