"""Read a fresh Android QA UI tree; never reuse a dump after a null-root error."""
import argparse
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--adb',required=True);ap.add_argument('--serial',required=True);ap.add_argument('--out',default='dist/device-evidence/current.xml');args=ap.parse_args()
 command=[args.adb,'-s',args.serial]
 remote='/sdcard/wordbloom-ui-'+str(time.time_ns())+'.xml'
 for _ in range(4):
  result=subprocess.run(command+['shell','uiautomator','dump',remote],capture_output=True,text=True,timeout=12)
  if 'UI hierchary dumped to:' in result.stdout:
   data=subprocess.check_output(command+['exec-out','cat',remote],timeout=5)
   tree=ET.fromstring(data);destination=Path(args.out);destination.parent.mkdir(parents=True,exist_ok=True);destination.write_bytes(data)
   subprocess.run(command+['shell','rm',remote],capture_output=True,timeout=5,check=True)
   sys.stdout.reconfigure(encoding='utf-8')
   for node in tree.iter('node'):
    a=node.attrib
    if a.get('text') or a.get('content-desc'):print(repr((a.get('text'),a.get('content-desc'),a.get('bounds'),a.get('checked'))))
   return
  time.sleep(.5)
 raise SystemExit('No fresh accessibility root; inspect an actual screenshot instead. Old dump was not reused.')

if __name__=='__main__':main()
