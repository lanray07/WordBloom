"""Gate for independently sourced language packs. Never translates English answers."""
import argparse, collections, json, unicodedata
from pathlib import Path

def validate(pack):
 for field in ['locale','license','reviewer','words','puzzles']:assert pack.get(field),f'Missing {field}'
 words={unicodedata.normalize('NFC',w['word']).upper() for w in pack['words'] if w.get('approved') and w.get('frequency_band') in ('common','standard','advanced')}
 deny={unicodedata.normalize('NFC',x).upper() for x in pack.get('blocked',[])}
 assert not words & deny,'Blocked word approved'
 ids=set()
 for puzzle in pack['puzzles']:
  assert puzzle['id'] not in ids,'Duplicate ID';ids.add(puzzle['id'])
  letters=collections.Counter(unicodedata.normalize('NFC',puzzle['letters']).upper())
  targets=puzzle['targets'];assert len(targets)==len(set(targets)),'Duplicate targets'
  for word in targets+puzzle.get('bonus',[]):
   word=unicodedata.normalize('NFC',word).upper()
   assert word in words and word not in deny,'Unreviewed vocabulary: '+word
   assert not collections.Counter(word)-letters,'Insufficient letters: '+word
 return {'locale':pack['locale'],'validated_word_bags':len(ids),'status':'Crossword engine validation and native linguistic sign-off still required'}
if __name__=='__main__':
 ap=argparse.ArgumentParser();ap.add_argument('pack',type=Path);args=ap.parse_args();print(json.dumps(validate(json.loads(args.pack.read_text(encoding='utf-8'))),indent=2))
