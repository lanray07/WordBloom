"""Development-only corpus evidence; never a dictionary/suitability certificate."""
import argparse
import importlib.metadata
import json
import re
import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--runtime',type=Path,required=True);args=ap.parse_args()
 sys.path.insert(0,str(args.runtime.resolve()))
 from wordfreq import zipf_frequency
 packs=json.loads((ROOT/'localization/gameplay-drafts.json').read_text(encoding='utf-8'))['packs']
 # Read the actual authored English targets and bonus allowlists as well.
 english=(ROOT/'engine/src/main/kotlin/com/wordbloom/engine/Puzzle.kt').read_text(encoding='utf-8').split('val seeds=listOf(',1)[1].split('\n val puzzles:',1)[0]
 pairs=re.findall(r'Seed\("[^\"]+","([^\"]+)","([^\"]*)"',english)
 assert len(pairs)==24,'English seed extraction incomplete'
 packs['en']=[{'targets':targets.split()+bonus.split()} for targets,bonus in pairs]
 result={'provider':'wordfreq','version':importlib.metadata.version('wordfreq'),'source':'https://github.com/rspeer/wordfreq','data_notice':'https://github.com/rspeer/wordfreq/blob/master/NOTICE.md','attribution':'Robyn Speer; SUBTLEX authors Marc Brysbaert et al.; Leeds Internet Corpus; Wikipedia; ParaCrawl; OPUS OpenSubtitles 2018 / OpenSubtitles; Google Books Ngrams','report_data_license':'CC BY-SA 4.0, https://creativecommons.org/licenses/by-sa/4.0/','modification':'Lookups and review flags for WordBloom authored vocabulary; full corpus is not redistributed or packaged in the Android app.','limits':'Corpus observations through about 2021, not a normative dictionary, regional certification, audience filter or native linguistic approval. Portuguese frequency data is language-wide, not separate Brazil/Portugal data.','review_required':True,'languages':{}}
 for locale,seeds in packs.items():
  words=sorted({word for seed in seeds for word in seed['targets']})
  records=[dict(word=word,zipf=zipf_frequency(word.lower(),locale),review_reason='unobserved in this corpus' if zipf_frequency(word.lower(),locale)==0 else 'less than one occurrence per million; review rarity' if zipf_frequency(word.lower(),locale)<3 else None) for word in words]
  result['languages'][locale]={'word_count':len(words),'flagged':[r['word'] for r in records if r['review_reason']],'words':records}
  print(locale+': '+str(len(words))+' words; flagged '+str(len(result['languages'][locale]['flagged'])),flush=True)
 (ROOT/'reports/word-frequency-review.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__':main()
