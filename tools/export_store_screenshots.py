"""Compose three listing images from unaltered production Compose render outputs.

These are screenshots of the native screen tree rendered by layoutlib, not device
captures. Keep that provenance visible and separate from runtime certification.
"""
import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SOURCES = ROOT / 'app/src/test/snapshots/images'
DEST = ROOT / 'store/screenshots/en-US/tablet'
SPECS = [
    ('01-connect', 'connect', 'WORD CONNECT',
     'Connect letters. Find your flow.',
     'Swipe, tap or type to solve curated word puzzles.'),
    ('02-journey', 'journey', 'SIX GARDEN CHAPTERS',
     'A world of words awaits.',
     'Start free. Unlock the full collection with one premium purchase.'),
    ('03-garden', 'garden', 'SAVED PROGRESS',
     'Little puzzles. Lovely progress.',
     'Watch your illustrated garden bloom with every first completion.'),
]

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    DEST.mkdir(parents=True, exist_ok=True)
    heading = ImageFont.truetype('C:/Windows/Fonts/georgia.ttf', 43)
    body = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 21)
    eyebrow = ImageFont.truetype('C:/Windows/Fonts/seguisb.ttf', 12)
    source_label = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 10)
    exports = []
    for ident, screen, tag, title, subtitle in SPECS:
        matches = list(SOURCES.glob(f'com.wordbloom.garden_StoreScreenRenderTest_*_store-{screen}.png'))
        assert len(matches) == 1, f'Expected one fresh production render: {screen}'
        source = matches[0]
        with Image.open(source) as original:
            assert original.size == (1280, 800)
            shot = original.convert('RGB').resize((944, 590), Image.Resampling.LANCZOS)
        canvas = Image.new('RGB', (1280, 800), '#F6F0E5')
        draw = ImageDraw.Draw(canvas)
        draw.text((56, 26), 'WORDBLOOM  /  ' + tag, font=eyebrow, fill='#47675A')
        assert draw.textlength(title, font=heading) <= 1168
        assert draw.textlength(subtitle, font=body) <= 1168
        draw.text((54, 50), title, font=heading, fill='#244E43')
        draw.text((56, 113), subtitle, font=body, fill='#47675A')
        # Preserve every pixel of the original screen's content and aspect ratio.
        x, y = 168, 178
        shadow = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
        ImageDraw.Draw(shadow).rounded_rectangle((x-2,y+4,x+946,y+598),radius=9,fill=(29,58,46,34))
        shadow = shadow.filter(ImageFilter.GaussianBlur(9))
        canvas = Image.alpha_composite(canvas.convert('RGBA'), shadow)
        canvas.paste(shot, (x, y))
        draw = ImageDraw.Draw(canvas)
        draw.rectangle((x-1,y-1,x+944,y+590),outline='#D7DBCE',width=1)
        draw.text((56, 782), 'APPLICATION SCREEN RENDER',font=source_label,fill='#5E7164')
        out = DEST / (ident + '.png')
        canvas.convert('RGB').save(out, optimize=True)
        exports.append(dict(id=ident,path=out.relative_to(ROOT).as_posix(),
            dimensions=[1280,800],format='PNG',sha256=digest(out),
            source=source.relative_to(ROOT).as_posix(),source_sha256=digest(source),
            title=title,subtitle=subtitle,
            provenance='Production Compose tree, Paparazzi/layoutlib, independent screen fixture; not a physical-device capture',
            content_edits='None: uniform downscale, frame, shadow and external caption only',
            device_qa='Normal debug KFTRWI checks passed; Amazon-delivered release test remains pending',console_status='prepared'))
    manifest = dict(status='prepared; pending visual and console validation',
        source_kind='production Compose screen renders',device_capture=False,
        locale='en-US',fallback='English example screens shared across localized listings; the candidate supports all ten gameplay locale editions',
        prepared_date='2026-10-18',exports=exports)
    (ROOT/'store/premium-screenshots.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
    # This preview is for convenient review only, never a store upload.
    contact = Image.new('RGB', (960, 1800), '#E5E9DF')
    for index, item in enumerate(exports):
        with Image.open(ROOT/item['path']) as image:
            preview = image.resize((960,600),Image.Resampling.LANCZOS)
            contact.paste(preview,(0,index*600))
    contact.save(ROOT/'reports/premium-screenshots-preview.jpg',quality=94)
    print('Prepared three 1280x800 production-screen listing images with provenance.')

if __name__ == '__main__':
    main()
