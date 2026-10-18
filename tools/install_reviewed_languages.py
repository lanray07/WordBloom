"""Install only complete, explicitly reviewed, hash-bound localization bundles."""
import argparse
import hashlib
import json
import re
from pathlib import Path
from localize import validate
from extract_ui_catalog import voice_commands
from prepare_language_packs import render_kotlin

ROOT=Path(__file__).resolve().parents[1]
NAMES={'en-GB':'English UK','en-US':'English US','de':'Deutsch','es':'Español','fr':'Français','it':'Italiano','nl':'Nederlands','pl':'Polski','pt-BR':'Português (Brasil)','pt-PT':'Português (Portugal)'}

def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()

def main(check_only=False):
 approvals=json.loads((ROOT/'localization/approvals.json').read_text(encoding='utf-8'))
 source_path=ROOT/'localization/ui-source.json'
 source=json.loads(source_path.read_text(encoding='utf-8'))['entries']
 gameplay=ROOT/'localization/gameplay-drafts.json'
 expected=render_kotlin(json.loads(gameplay.read_text(encoding='utf-8'))['packs'])
 assert (ROOT/'engine/src/main/kotlin/com/wordbloom/engine/DraftLanguagePacks.kt').read_text(encoding='utf-8')==expected,'Reviewed vocabulary differs from compiled Kotlin; run prepare_language_packs.py and engine tests'
 voice=ROOT/'localization/voice-command-drafts.json'
 assert json.loads(voice.read_text(encoding='utf-8'))==voice_commands(),'Voice command source changed; regenerate and review it'
 installed={}
 # Validate all requested languages before mutating installed assets/registry.
 for locale in approvals['required_for_release']:
  review=approvals['locales'].get(locale,{})
  assert review.get('reviewer','').strip() and re.fullmatch(r'\d{4}-\d{2}-\d{2}',review.get('reviewed_on','')),f'{locale}: native reviewer/date missing'
  assert review.get('ui_approved') is True and review.get('gameplay_approved') is True,f'{locale}: review not approved'
  path=ROOT/f'localization/generated/messages/{locale}.json'
  assert path.exists(),f'{locale}: translation generation incomplete'
  assert review.get('ui_source_sha256')==sha(source_path),f'{locale}: changed UI source needs review'
  assert review.get('messages_sha256')==sha(path),f'{locale}: changed translations need review'
  assert review.get('gameplay_sha256')==sha(gameplay),f'{locale}: changed vocabulary needs review'
  assert review.get('voice_commands_sha256')==sha(voice),f'{locale}: changed spoken commands need review'
  bundle=json.loads(path.read_text(encoding='utf-8'));entries=bundle['entries']
  assert bundle['locale']==locale,f'{locale}: bundle locale mismatch'
  assert set(entries)==set(source),f'{locale}: translation catalogue coverage mismatch'
  for key,item in entries.items():
   assert item['source']==source[key]['source'],f'{locale}: stale source {key}'
   assert not re.search(r'\{\d+}\{\d+}',item['source']),f'{locale}: adjacent parameters need an explicit source delimiter: {key}'
   validate(item['source'],item['translation'])
  installed[locale]=path.read_bytes()
 if check_only:
  print('All required language reviews and compiled vocabulary hashes validated.')
  return
 for locale,contents in installed.items():
  path=ROOT/f'app/src/main/assets/localization/{locale}.json';path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(contents)
 locales=['en-GB','en-US',*installed]
 lines=['package com.wordbloom.engine','/** Generated from hash-bound native approvals. */','object ApprovedLanguages {', ' val locales=listOf('+','.join(json.dumps(x) for x in locales)+')', ' val names=mapOf('+','.join(json.dumps(x)+' to '+json.dumps(NAMES[x],ensure_ascii=False) for x in locales)+')','}']
 (ROOT/'engine/src/main/kotlin/com/wordbloom/engine/ApprovedLanguages.kt').write_text('\n'.join(lines)+'\n',encoding='utf-8')
 print('Installed reviewed interfaces and activated eight language editions. Rebuild and run language/device QA before upload.')

if __name__=='__main__':
 parser=argparse.ArgumentParser();parser.add_argument('--check-only',action='store_true')
 main(parser.parse_args().check_only)
