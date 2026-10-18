"""Read-only release gate. Form completeness alone never authorizes submission."""
import hashlib
import json
import re
import subprocess
import sys
from pathlib import Path
from package_delivery import production_source_hash

ROOT=Path(__file__).resolve().parents[1]

def read(path):
 p=ROOT/path
 return json.loads(p.read_text(encoding='utf-8')) if p.exists() else {}

def main():
 decisions=read('store/publisher-decisions.json');reviews=read('localization/approvals.json')
 source=(ROOT/'engine/src/main/kotlin/com/wordbloom/engine/ApprovedLanguages.kt').read_text(encoding='utf-8')
 available=re.findall(r'"([^"]+)"',re.search(r'val locales\s*=\s*listOf\(([^)]*)\)',source)[1])
 required=decisions['required_gameplay_locales']
 device=read('reports/fire-device-qa.json');live=read('reports/live-app-test.json')
 multilingual=read('reports/localized-device-qa.json')
 progress=read('reports/locale-progress-qa.json')
 console=read('reports/amazon-console-verification.json')
 verified=read('reports/verification-summary.json')
 review_validation=subprocess.run([sys.executable,str(ROOT/'tools/install_reviewed_languages.py'),'--check-only'],capture_output=True)
 debug=ROOT/'app/build/outputs/apk/debug/app-debug.apk'
 release=ROOT/'app/build/outputs/apk/release/app-release-unsigned.apk'
 checks={
  'publisher_privacy_approval':decisions.get('privacy_and_sdk_disclosures_approved') is True,
  'publisher_name_confirmation':decisions.get('wordbloom_name_clearance_confirmed_by_publisher') is True,
  'verified_source_matches_current_production_source':verified.get('production_source_sha256')==production_source_hash(),
  'all_required_language_editions_active':set(required)<=set(available),
  'linguistic_reviews_recorded':all(reviews.get('locales',{}).get(locale,{}).get('ui_approved') is True and reviews.get('locales',{}).get(locale,{}).get('gameplay_approved') is True for locale in required if not locale.startswith('en')),
  'reviewed_content_hashes_match_current_inputs':review_validation.returncode==0,
  'current_debug_device_matrix_passed':device.get('all_cases_passed') is True and debug.exists() and device.get('apk_sha256')==hashlib.sha256(debug.read_bytes()).hexdigest(),
  'all_locales_device_tested':multilingual.get('passed') is True and set(required)<=set(multilingual.get('locales_passed',[])) and debug.exists() and multilingual.get('apk_sha256')==hashlib.sha256(debug.read_bytes()).hexdigest(),
  'locale_progress_boundaries_tested':progress.get('passed') is True and len(progress.get('cases',[]))==3 and debug.exists() and progress.get('apk_sha256')==hashlib.sha256(debug.read_bytes()).hexdigest(),
  'console_candidate_and_copy_match_final_release':console.get('metadata_matches_final_language_support') is True and set(required)<=set(console.get('supported_gameplay_locales',[])) and release.exists() and console.get('uploaded_release_apk_sha256')==hashlib.sha256(release.read_bytes()).hexdigest(),
  'store_delivered_release_test_passed':live.get('passed') is True and release.exists() and live.get('release_apk_sha256')==hashlib.sha256(release.read_bytes()).hexdigest(),
 }
 result=dict(ready_to_submit=all(checks.values()),checks=checks,required_locales=required,active_locales=available,form_complete_does_not_mean_release_ready=True,submission_performed=console.get('production_submitted') is True)
 (ROOT/'reports/release-gate.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(result,indent=2))
 return 0 if result['ready_to_submit'] else 1

if __name__=='__main__':raise SystemExit(main())
