"""Run on the connected, dedicated WordBloom QA installation.

Clears WordBloom data between independent cases. Restores Wi-Fi, font scale and
rotation after testing. Does not purchase anything or change Amazon accounts.
"""
import argparse
import hashlib
import json
import re
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--adb',required=True);ap.add_argument('--serial',required=True);ap.add_argument('--date',required=True);ap.add_argument('--fresh-test-install',action='store_true',required=True);args=ap.parse_args()
 def adb(*values,timeout=60):
  return subprocess.check_output([args.adb,'-s',args.serial,*values],timeout=timeout,stderr=subprocess.STDOUT).decode('utf-8',errors='replace')
 assert adb('get-state').strip()=='device'
 adb('shell','input','keyevent','KEYCODE_WAKEUP')
 policy=adb('shell','dumpsys','window','policy')
 if re.search(r'KeyguardServiceDelegate\s+showing=true',policy):
  raise SystemExit('Device is locked. Unlock it physically before running QA; no PIN is read or entered by this tool.')
 evidence=ROOT/'dist/device-evidence';evidence.mkdir(parents=True,exist_ok=True)
 original={name:adb('shell','settings','get',section,name).strip() for section,name in [('system','font_scale'),('system','accelerometer_rotation'),('system','user_rotation'),('global','wifi_on'),('global','stay_on_while_plugged_in')]}
 apk=ROOT/'app/build/outputs/apk/debug/app-debug.apk';test=ROOT/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
 for path in [apk,test]:assert 'Success' in adb('install','-r',str(path))
 summary=dict(date=args.date,device_model=adb('shell','getprop','ro.product.model').strip(),android_api=int(adb('shell','getprop','ro.build.version.sdk')),apk_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),build_type='normal Amazon debug adapter; no development billing',cases=[],scope='English device regression QA. Multilingual cases are recorded separately; Amazon-delivered release testing remains pending.')
 def run(name,method,flag=None):
  orientation='landscape' if 'landscape' in name else 'portrait'
  adb('shell','settings','put','system','accelerometer_rotation','0')
  adb('shell','settings','put','system','user_rotation','1' if orientation=='landscape' else '0')
  assert 'Success' in adb('shell','pm','clear','com.wordbloom.garden')
  options=['shell','am','instrument','-w','-e','class',method,'-e','orientation',orientation]
  if flag:options+=['-e',flag,'true']
  result=adb(*options,'com.wordbloom.garden.test/androidx.test.runner.AndroidJUnitRunner',timeout=180)
  (evidence/(name+'.txt')).write_text(result,encoding='utf-8')
  public=ROOT/'reports/fire-tests';public.mkdir(parents=True,exist_ok=True)
  (public/(name+'.txt')).write_text('\n'.join(line.rstrip() for line in result.replace('\r','').strip().splitlines())+'\n',encoding='utf-8')
  passed='OK (' in result and 'FAILURES' not in result and 'INSTRUMENTATION_FAILED' not in result
  summary['cases'].append(dict(id=name,passed=passed,orientation_asserted_by_instrumentation=orientation,evidence='reports/fire-tests/'+name+'.txt'))
  print(name+': '+('PASS' if passed else 'FAIL'),flush=True)
  assert passed,result
 try:
  adb('shell','svc','power','stayon','usb')
  adb('shell','settings','put','system','accelerometer_rotation','0')
  adb('shell','settings','put','system','user_rotation','1')
  run('baseline-landscape','com.wordbloom.garden.GameUiTest')
  run('runtime-landscape','com.wordbloom.garden.FireRuntimeTest#invalidBonusHintsRecreationAndVoiceFallback','runtime')
  run('touch-swipe-landscape','com.wordbloom.garden.FireRuntimeTest#continuousTouchSwipeAndBacktracking','gesture')
  run('settings-landscape','com.wordbloom.garden.FireRuntimeTest#accessibilitySettingsPersistAfterRecreation','settings')
  adb('shell','settings','put','system','accelerometer_rotation','0')
  adb('shell','settings','put','system','user_rotation','0')
  run('runtime-portrait','com.wordbloom.garden.FireRuntimeTest#invalidBonusHintsRecreationAndVoiceFallback','runtime')
  adb('shell','svc','wifi','disable')
  assert adb('shell','settings','get','global','wifi_on').strip()=='0'
  run('runtime-wifi-off','com.wordbloom.garden.FireRuntimeTest#invalidBonusHintsRecreationAndVoiceFallback','runtime')
  adb('shell','svc','wifi','enable' if original['wifi_on']=='1' else 'disable')
  adb('shell','settings','put','system','font_scale','1.5')
  run('settings-large-font-portrait','com.wordbloom.garden.FireRuntimeTest#accessibilitySettingsPersistAfterRecreation','settings')
 finally:
  restores=[]
  for name in ['font_scale','accelerometer_rotation','user_rotation']:
   try:adb('shell','settings','put','system',name,original[name]);restores.append(name)
   except Exception:pass
  try:adb('shell','svc','wifi','enable' if original['wifi_on']=='1' else 'disable');restores.append('wifi_on')
  except Exception:pass
  try:adb('shell','settings','put','global','stay_on_while_plugged_in',original['stay_on_while_plugged_in']);restores.append('stay_on_while_plugged_in')
  except Exception:pass
  summary['restored_device_settings']=restores
  summary['all_cases_passed']=len(summary['cases'])==7 and all(x['passed'] for x in summary['cases'])
  (ROOT/'reports/fire-device-qa.json').write_text(json.dumps(summary,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__':main()
