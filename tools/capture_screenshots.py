"""Run on a dedicated emulator/test device. Clears ONLY WordBloom test app data."""
import argparse,json,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser();ap.add_argument('--adb',default='adb');ap.add_argument('--serial',required=True);ap.add_argument('--variant',choices=['phone','tablet'],required=True);args=ap.parse_args()
def adb(*cmd):
 r=subprocess.run([args.adb,'-s',args.serial,*cmd],capture_output=True,text=True,check=True)
 return r.stdout
state=adb('get-state').strip()
if state!='device':raise SystemExit('Device not ready')
# Install compiled artifacts first; a release build is never modified for captures.
adb('install','-r',str(ROOT/'app/build/outputs/apk/debug/app-debug.apk'))
adb('install','-r',str(ROOT/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'))
adb('shell','pm','clear','com.wordbloom.garden')
output=adb('shell','am','instrument','-w','-e','capture','true','-e','class','com.wordbloom.garden.GameUiTest#screenshotTour','com.wordbloom.garden.test/androidx.test.runner.AndroidJUnitRunner')
print(output)
if 'FAILURES' in output or 'INSTRUMENTATION_FAILED' in output or 'OK (1 test)' not in output:raise SystemExit('Capture instrumentation did not pass')
folder=ROOT/'store/raw'/args.variant;folder.mkdir(parents=True,exist_ok=True)
adb('pull','/sdcard/Android/data/com.wordbloom.garden/files/screenshots/.',str(folder))
manifest=json.loads((ROOT/'store/screenshot-manifest.json').read_text())
for shot in manifest['captures']:
 if not (folder/(shot['id']+'.png')).exists():raise SystemExit('Missing capture: '+shot['id'])
print('Eight real application captures available at',folder)
