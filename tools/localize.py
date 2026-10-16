"""Development-only DeepL pipeline. No credentials are packaged in Android."""
import argparse, hashlib, json, os, re, urllib.request, urllib.parse
from html import escape, unescape
from pathlib import Path
import xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
PLACEHOLDERS=re.compile(r"%(?:\d+\$)?[sdf]|\{[^{}]+\}|\\n")
TARGETS={'en-US':'EN-US','es':'ES','fr':'FR','de':'DE','it':'IT','pt-BR':'PT-BR','pt-PT':'PT-PT','nl':'NL','pl':'PL'}
QUALIFIERS={'en-US':'en-rUS','pt-BR':'pt-rBR','pt-PT':'pt-rPT'}
def protect(text):
 values=[]
 def sub(m):
  values.append(m.group());return f'<x id="{len(values)-1}"/>'
 return PLACEHOLDERS.sub(sub,text),values

def restore(text,values):
 for i,value in enumerate(values):
  pattern=rf'<x\s+id=[\"\x27]{i}[\"\x27]\s*/>'
  if len(re.findall(pattern,text))!=1: raise ValueError('Placeholder changed or duplicated')
  text=re.sub(pattern,lambda _: value,text)
 if '<x ' in text: raise ValueError('Unknown placeholder')
 return text

def validate(source,target):
 if sorted(PLACEHOLDERS.findall(source))!=sorted(PLACEHOLDERS.findall(target)):raise ValueError('Placeholder mismatch')
 if not target.strip():raise ValueError('Empty translation')
 return {'length_ratio':round(len(target)/max(len(source),1),2),'review_required':True}

def strings():
 return {n.attrib['name']:n.text or '' for n in ET.parse(ROOT/'app/src/main/res/values/strings.xml').getroot()}

def translate(source,language,key,glossary=None):
 escaped=escape(source,quote=False).replace("&lt;keep&gt;","<keep>").replace("&lt;/keep&gt;","</keep>")
 protected,values=protect(escaped)
 payload={'text':[protected],'target_lang':TARGETS[language],'source_lang':'EN','tag_handling':'xml','ignore_tags':['x','keep'],'preserve_formatting':True}
 if glossary:payload['context']='Word puzzle terminology: '+ '; '.join(k+': '+v for k,v in glossary.get('terms',{}).items())
 endpoint=os.getenv('DEEPL_ENDPOINT','https://api-free.deepl.com/v2/translate')
 if endpoint not in ('https://api-free.deepl.com/v2/translate','https://api.deepl.com/v2/translate'):raise ValueError('Use an approved HTTPS DeepL endpoint')
 request=urllib.request.Request(endpoint,data=json.dumps(payload).encode(),headers={'Authorization':'DeepL-Auth-Key '+key,'Content-Type':'application/json'})
 with urllib.request.urlopen(request,timeout=45) as response: data=json.load(response)
 return unescape(restore(data['translations'][0]['text'],values))

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--translate',action='store_true');args=ap.parse_args()
 ui_source=strings();source=dict(ui_source)
 source['store_listing']=(ROOT/'store/listing-en-GB.md').read_text(encoding='utf-8')
 captions=json.loads((ROOT/'store/screenshot-manifest.json').read_text(encoding='utf-8'))['captures']
 for caption in captions:
  for field in ('headline','supporting'):source['shot_'+caption['id']+'_'+field]=caption[field]
 cache_path=ROOT/'localization/cache.json';cache=json.loads(cache_path.read_text()) if cache_path.exists() else {}
 glossary=json.loads((ROOT/'localization/glossary.json').read_text())
 report={'provider':'DeepL','credential_present':bool(os.getenv('DEEPL_API_KEY')),'languages':{},'rtl_prepared':['ar','hi'],'glossary':glossary,'source_count':len(source),'dynamic_strings':'Some interpolated Kotlin messages remain; review extraction audit before activating new UI locales.'}
 metadata_path=ROOT/'store/amazon-metadata.json'
 if metadata_path.exists():
  metadata=json.loads(metadata_path.read_text(encoding='utf-8'))
  report['store_metadata_localization']={
   'locales':list(metadata['listings']),
   'status':'ten listing drafts filled in Amazon console; native editorial review pending',
   'gameplay_locales':['en-GB','en-US'],
   'boundary':'Store descriptions are localized independently; Android interface/puzzle activation is unchanged.'}
 for language in TARGETS:
  output={};missing=[];checks={}
  for name,text in source.items():
   digest=hashlib.sha256((language+'|'+text+'|'+json.dumps(glossary,sort_keys=True)).encode()).hexdigest()
   if language=='en-US':value=text
   elif digest in cache:value=cache[digest]
   elif args.translate and os.getenv('DEEPL_API_KEY'):
    # Product terminology is protected as XML not sent as translatable prose.
    marked=text
    for term in glossary['preserve']:marked=marked.replace(term,'<keep>'+term+'</keep>')
    value=translate(marked,language,os.environ['DEEPL_API_KEY'],glossary).replace('<keep>','').replace('</keep>','')
    for term in glossary['preserve']:
     if term in text and term not in value:raise ValueError('Glossary term changed: '+term)
    cache[digest]=value
    cache_path.parent.mkdir(exist_ok=True);temporary=cache_path.with_suffix('.tmp');temporary.write_text(json.dumps(cache,ensure_ascii=False,indent=2),encoding='utf-8');temporary.replace(cache_path)
   else:missing.append(name);continue
   checks[name]=validate(text,value);output[name]=value
  if not missing:
   xml=ET.Element('resources')
   for name,value in output.items():
    if name in ui_source:ET.SubElement(xml,'string',{'name':name}).text=value
   ET.indent(xml);destination=ROOT/'localization/generated'/('values-'+QUALIFIERS.get(language,language))/'strings.xml';destination.parent.mkdir(parents=True,exist_ok=True);ET.ElementTree(xml).write(destination,encoding='utf-8',xml_declaration=True)
   marketing=ROOT/'localization/generated/store'/language;marketing.mkdir(parents=True,exist_ok=True)
   (marketing/'listing.md').write_text(output['store_listing'],encoding='utf-8')
   (marketing/'captions.json').write_text(json.dumps({k:v for k,v in output.items() if k.startswith('shot_')},ensure_ascii=False,indent=2),encoding='utf-8')
  report['languages'][language]={'status':'pending_linguistic_review' if not missing else 'pending_translation','missing':missing,'checks':checks}
 # Do not install unreviewed generated locales into app/src/main/res.
 leftovers=[]
 for p in (ROOT/'app/src/main/java').rglob('*.kt'):
  for num,line in enumerate(p.read_text(encoding='utf-8').splitlines(),1):
   if re.search(r'(?:Text|message=|notify\(|status\().*"[A-Za-z]',line):leftovers.append({'file':str(p.relative_to(ROOT)),'line':num})
 report['extraction_audit']=leftovers
 cache_path.parent.mkdir(exist_ok=True);cache_path.write_text(json.dumps(cache,ensure_ascii=False,indent=2),encoding='utf-8')
 (ROOT/'localization/report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
 print(f'Validated {len(source)} source strings. Translation status written to localization/report.json')
if __name__=='__main__':main()
