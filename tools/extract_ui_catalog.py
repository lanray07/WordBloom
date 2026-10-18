"""Extract resource and Kotlin UI templates, including nested interpolation.

Review the source locations before using translations. Never translate puzzle
answers through this catalogue; gameplay vocabulary is authored separately.
"""
import hashlib
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SINGLES = {'Home','Settings','All','Undo','Clear','Check','Beginner','Intermediate','Advanced','Expert','bloom','blooms','escape','escapes','empty letter','Listening…','Recognising…'}

def voice_commands():
 source=(ROOT/'engine/src/main/kotlin/com/wordbloom/engine/Puzzle.kt').read_text(encoding='utf-8')
 english=source.split('private val english=mapOf(',1)[1].split('\n private val phrases=',1)[0]
 commands=re.findall(r'"([^"]+)" to Command\.([A-Z]+)',english)
 phrases=source.split('private val phrases=mapOf(',1)[1].split('\n )',1)[0]
 return dict(english=[dict(command=command,phrase=phrase) for phrase,command in commands],drafts={locale:[dict(command=command,phrase=phrase) for (_,command),phrase in zip(commands,text.split('|'))] for locale,text in re.findall(r'"([^"]+)" to "([^"]+)"',phrases)})

def literals(source):
 """Yield (offset, template, expressions) for normal Kotlin strings."""
 out=[]
 def string_at(start):
  i=start+1; parts=[];args=[]
  while i<len(source):
   c=source[i]
   if c=='"':
    out.append((start,''.join(parts),args));return i+1
   if c=='\\' and i+1<len(source):
    parts.append({'n':'\n','t':'\t','r':'\r'}.get(source[i+1],source[i+1]));i+=2;continue
   if c=='$' and i+1<len(source):
    if source[i+1]=='{':
     begin=i+2;i=begin;depth=1
     while depth and i<len(source):
      if source[i]=='"':i=string_at(i);continue
      if source[i]=='{':depth+=1
      elif source[i]=='}':depth-=1
      i+=1
     args.append(source[begin:i-1]);parts.append('{'+str(len(args)-1)+'}');continue
    match=re.match(r'\$([A-Za-z_]\w*)',source[i:])
    if match:
     args.append(match[1]);parts.append('{'+str(len(args)-1)+'}');i+=len(match[0]);continue
   parts.append(c);i+=1
  raise ValueError('Unterminated Kotlin string')
 i=0
 while i<len(source):
  if source.startswith('//',i):
   end=source.find('\n',i);i=end if end>=0 else len(source)
  elif source.startswith('/*',i):
   end=source.find('*/',i+2);i=end+2 if end>=0 else len(source)
  elif source[i]=='"':i=string_at(i)
  elif source[i]=="'":
   i+=1
   while i<len(source):
    if source[i]=='\\':i+=2
    elif source[i]=="'":i+=1;break
    else:i+=1
  else:i+=1
 return out

def main():
 entries={}
 def add(text,location,expressions=None):
  ident='msg_'+hashlib.sha256(text.encode()).hexdigest()[:12]
  item=entries.setdefault(ident,dict(source=text,locations=[],parameters=expressions or []))
  item['locations'].append(location)
 for n in ET.parse(ROOT/'app/src/main/res/values/strings.xml').getroot():
  # AAPT collapses unescaped resource whitespace; explicit \n stays a newline.
  add(re.sub(r'\s+',' ',(n.text or '').strip()).replace('\\n','\n'),'resource:'+n.attrib['name'])
 paths=[ROOT/'app/src/main/java/com/wordbloom/garden'/name for name in ['MainActivity.kt','GameViewModel.kt','Billing.kt','Voice.kt']]
 for path in paths:
  source=path.read_text(encoding='utf-8')
  for offset,text,args in literals(source):
   visible=re.sub(r'\{\d+\}','',text)
   if not (text in SINGLES or (re.search('[A-Za-z]',visible) and (' ' in visible or '…' in visible)) or (args and visible.strip())):continue
   if text.startswith('SELECT ') or text in {'tile colour'}:continue
   add(text,path.relative_to(ROOT).as_posix()+':'+str(source.count('\n',0,offset)+1),args)
 # World and difficulty labels are data, but appear in the interface.
 for text in ['Sunrise Garden','Enchanted Forest','Blossom Valley','Crystal Lake','Golden Meadow','Moonlight Garden','Beginner','Intermediate','Advanced','Expert']:
  add(text,'engine:display-label')
 for text in ['successful','failed','already purchased','invalid sku','not supported','pending']:
  add(text,'sdk:purchase-status-display')
 output=dict(status='source extraction for translation and review; not approved language activation',entries=entries)
 (ROOT/'localization/ui-source.json').write_text(json.dumps(output,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 (ROOT/'localization/voice-command-drafts.json').write_text(json.dumps(voice_commands(),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print('Extracted',len(entries),'distinct UI resource/message templates with source locations.')

if __name__=='__main__':main()
