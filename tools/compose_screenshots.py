"""Compose marketing images from real Android captures; never fabricate gameplay."""
import argparse,json,textwrap
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser();ap.add_argument('--variant',choices=['phone','tablet'],required=True);args=ap.parse_args()
manifest=json.loads((ROOT/'store/screenshot-manifest.json').read_text())
size=manifest['variants'][args.variant];source=ROOT/'store/raw'/args.variant
font_path=Path('C:/Windows/Fonts/georgia.ttf');body_path=Path('C:/Windows/Fonts/segoeui.ttf')
if not font_path.exists():raise SystemExit('Configure licensed system font paths for your OS')
results=[]
for shot in manifest['captures']:
 path=source/(shot['id']+'.png')
 if not path.exists():raise SystemExit('Capture missing; run capture_screenshots.py on Android first: '+str(path))
 img=Image.new('RGB',tuple(size),'#faf6ed');draw=ImageDraw.Draw(img)
 scale=size[0]/800;headline=ImageFont.truetype(str(font_path),int(36*scale));body=ImageFont.truetype(str(body_path),int(18*scale))
 y=int(25*scale)
 for line in textwrap.wrap(shot['headline'],32):draw.text((30*scale,y),line,font=headline,fill='#244e43');y+=int(44*scale)
 for line in textwrap.wrap(shot['supporting'],58):draw.text((30*scale,y),line,font=body,fill='#244e43');y+=int(26*scale)
 screen=Image.open(path).convert('RGB');screen.thumbnail((size[0]-int(40*scale),size[1]-y-int(24*scale)))
 img.paste(screen,((size[0]-screen.width)//2,y+int(12*scale)))
 for locale in manifest['locales']:
  dest=ROOT/'store/screenshots'/locale/args.variant/(shot['id']+'.jpg');dest.parent.mkdir(parents=True,exist_ok=True);img.save(dest,quality=94)
  results.append({'path':str(dest.relative_to(ROOT)),'dimensions':size,'source_capture':str(path.relative_to(ROOT))})
manifest.setdefault('exports',[]).extend(results);manifest['status']='captured_pending_visual_and_console_spec_review';(ROOT/'store/screenshot-manifest.json').write_text(json.dumps(manifest,indent=2))
print('Composed',len(results),'images. Review all exports visually before submission.')
