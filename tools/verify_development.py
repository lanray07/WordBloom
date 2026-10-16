"""Verify and copy an explicitly isolated development APK; no device or store calls."""
from pathlib import Path
import hashlib
import json
import os
import re
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
sdk = Path(os.environ.get('ANDROID_HOME') or Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk')
tools = sdk / 'build-tools/36.0.0'
apk = ROOT / 'app/build/outputs/apk/debug/app-debug.apk'
log = (ROOT / 'development-build.log').read_text(encoding='utf-8-sig', errors='replace')
assert log.rfind('BUILD SUCCESSFUL') > log.rfind('BUILD FAILED')
badging = subprocess.run([str(tools / 'aapt2.exe'), 'dump', 'badging', str(apk)],
                         capture_output=True, text=True, check=True).stdout
assert "package: name='com.wordbloom.garden.dev'" in badging
assert "versionName='0.1.0-dev'" in badging
config = (ROOT / 'app/build/generated/source/buildConfig/debug/com/wordbloom/garden/BuildConfig.java').read_text()
assert re.search(r'DEVELOPMENT_BILLING\s*=\s*true', config)
signature = subprocess.run([shutil.which('java'), '-jar', str(tools / 'lib/apksigner.jar'),
                            'verify', '--verbose', '--print-certs', str(apk)], capture_output=True, text=True)
assert signature.returncode == 0
with zipfile.ZipFile(apk) as archive:
    dex = b''.join(archive.read(name) for name in archive.namelist() if name.endswith('.dex'))
    assert b'DevelopmentBilling' in dex and b'DEVELOPMENT BUILD' in dex and b'TEST ONLY' in dex
destination = ROOT / 'dist/WordBloom-development.apk'
destination.parent.mkdir(exist_ok=True)
shutil.copy2(apk, destination)
proof = {
    'package': 'com.wordbloom.garden.dev', 'version': '0.1.0-dev',
    'signature_verified': True, 'development_billing_flag': True,
    'visible_test_only_labels_present': True, 'device_execution_performed': False,
    'sha256': hashlib.sha256(destination.read_bytes()).hexdigest(),
    'purpose': 'Developer QA only; simulated entitlement, no real payment. Separate data from normal app.',
}
(ROOT / 'reports/development-build.json').write_text(json.dumps(proof, indent=2))
(ROOT / 'reports/development-signature.txt').write_text(signature.stdout + signature.stderr)
print(json.dumps(proof, indent=2))
