"""Build an isolated .qa app with draft languages; never activate production.

Copies source into ignored dist, labels every screen LANGUAGE QA, and enables
draft catalogues only in that copied engine. No approval record is altered.
"""
import hashlib
import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path
from install_reviewed_languages import NAMES
from package_delivery import production_source_hash

ROOT = Path(__file__).resolve().parents[1]


def main():
    destination = ROOT / 'dist/localization-qa-project'
    destination.mkdir(parents=True, exist_ok=True)
    for name in ['gradle', 'app/src', 'engine/src']:
        shutil.copytree(ROOT / name, destination / name, dirs_exist_ok=True)
    for name in ['build.gradle.kts', 'settings.gradle.kts', 'gradle.properties', 'gradlew', 'gradlew.bat',
                 'app/build.gradle.kts', 'app/proguard-rules.pro', 'engine/build.gradle.kts']:
        shutil.copy2(ROOT / name, destination / name)
    sdk = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk'
    (destination / 'local.properties').write_text('sdk.dir=' + sdk.as_posix() + '\n')
    required = json.loads((ROOT / 'localization/approvals.json').read_text())['required_for_release']
    locales = ['en-GB', 'en-US', *required]
    for locale in required:
        source = ROOT / f'localization/generated/messages/{locale}.json'
        target = destination / f'app/src/main/assets/localization/{locale}.json'
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
    registry = destination / 'engine/src/main/kotlin/com/wordbloom/engine/ApprovedLanguages.kt'
    registry.write_text('package com.wordbloom.engine\n/** ISOLATED LANGUAGE QA ONLY; NOT APPROVED FOR RELEASE. */\nobject ApprovedLanguages {\n val locales=listOf(' +
                        ','.join(json.dumps(x) for x in locales) + ')\n val names=mapOf(' +
                        ','.join(json.dumps(x) + ' to ' + json.dumps(NAMES[x], ensure_ascii=False) for x in locales) + ')\n}\n', encoding='utf-8')
    build = destination / 'app/build.gradle.kts'
    build.write_text(build.read_text(encoding='utf-8').replace('debug {', 'debug {\n   applicationIdSuffix = ".qa"\n   versionNameSuffix = "-language-qa"', 1), encoding='utf-8')
    activity = destination / 'app/src/main/java/com/wordbloom/garden/MainActivity.kt'
    activity.write_text(activity.read_text(encoding='utf-8').replace('Header(s.page,store.premium)',
                        'UiText("LANGUAGE QA · DRAFT TRANSLATIONS · NOT FOR RELEASE",Modifier.fillMaxWidth().background(Peach).padding(4.dp),color=Pine,fontSize=10.sp)\n       Header(s.page,store.premium)', 1), encoding='utf-8')
    resources = destination / 'app/src/main/res/values/strings.xml'
    resources.write_text(resources.read_text(encoding='utf-8').replace('<string name="app_name">WordBloom</string>', '<string name="app_name">WordBloom Language QA</string>'), encoding='utf-8')
    result = subprocess.run([str(destination / 'gradlew.bat'), ':app:assembleDebug', ':app:assembleDebugAndroidTest'], cwd=destination)
    if result.returncode:
        raise SystemExit(result.returncode)
    apk = destination / 'app/build/outputs/apk/debug/app-debug.apk'
    bundles = {}
    with zipfile.ZipFile(apk) as archive:
        for locale in required:
            contents = (ROOT / f'localization/generated/messages/{locale}.json').read_bytes()
            assert archive.read(f'assets/localization/{locale}.json') == contents, 'QA APK has stale translation assets; rebuild before testing'
            bundles[locale] = hashlib.sha256(contents).hexdigest()
    shutil.copy2(apk, ROOT / 'dist/WordBloom-language-qa.apk')
    report = {'application_id': 'com.wordbloom.garden.qa', 'not_for_release': True,
              'approved_for_release': False, 'canonical_production_source_sha256': production_source_hash(),
              'apk_sha256': hashlib.sha256(apk.read_bytes()).hexdigest(),
              'translation_bundle_sha256': bundles,
              'locales': locales, 'scope': 'Isolated copy with draft registry/assets and persistent QA banner; canonical production languages unchanged.'}
    (ROOT / 'reports/localization-qa-build.json').write_text(json.dumps(report, indent=2) + '\n')
    print('Built isolated language QA APK; production registry and approvals unchanged.')


if __name__ == '__main__':
    main()
