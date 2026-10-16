"""Marketing concepts from production Compose renders, explicitly pending device QA.

These are separate from the device-capture pipeline and are not submission assets.
Run the optional Paparazzi suite first. Requires Pillow and licensed system fonts.
"""
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / 'store/screenshot-manifest.json'
SOURCE = ROOT / 'app/src/test/snapshots/images'
SCREENS = ['game', 'crossword', 'daily', 'garden', 'worlds', 'voice', 'home', 'english']


def lines(text, font, available):
    result, current = [], ''
    for word in text.split():
        candidate = (current + ' ' + word).strip()
        if current and font.getlength(candidate) > available:
            result.append(current)
            current = word
        else:
            current = candidate
    return result + ([current] if current else [])


def main():
    manifest = json.loads(MANIFEST.read_text(encoding='utf-8'))
    exports = []
    for variant, size in manifest['variants'].items():
        width, height = size
        for shot, screen in zip(manifest['captures'], SCREENS):
            filename = f'com.wordbloom.garden_ScreenRenderTest_screens[{variant}]_{variant}-{screen}.png'
            source = SOURCE / filename
            if not source.exists():
                raise SystemExit('Production render missing: ' + str(source))
            canvas = Image.new('RGB', (width, height), '#faf6ed')
            draw = ImageDraw.Draw(canvas)
            margin = 36 if variant == 'phone' else 48
            heading = ImageFont.truetype('C:/Windows/Fonts/georgia.ttf', 48)
            body = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 22)
            label = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 13)
            y = 24
            draw.text((margin, y), 'WORDBLOOM', font=label, fill='#244e43')
            y += 28
            for line in lines(shot['headline'], heading, width - margin * 2):
                draw.text((margin, y), line, font=heading, fill='#244e43')
                y += 58
            for line in lines(shot['supporting'], body, width - margin * 2):
                draw.text((margin, y), line, font=body, fill='#244e43')
                y += 30
            y += 16
            screen_image = Image.open(source).convert('RGB')
            screen_image.thumbnail((width - 32, height - y - 36), Image.Resampling.LANCZOS)
            canvas.paste(screen_image, ((width - screen_image.width) // 2, y))
            draw.text((margin, height - 24), 'COMPOSE SCREEN RENDER · DEVICE QA PENDING', font=label, fill='#244e43')
            for locale in manifest['locales']:
                output = ROOT / 'store/concepts' / locale / variant / (shot['id'] + '.jpg')
                output.parent.mkdir(parents=True, exist_ok=True)
                canvas.save(output, quality=95)
                exports.append({'path': str(output.relative_to(ROOT)), 'dimensions': size,
                                'source': str(source.relative_to(ROOT)), 'not_for_submission': True})
    manifest['render_concepts'] = {
        'status': 'production Compose screen renders; real device captures still required',
        'renderer': 'Paparazzi API-34 layoutlib; production application targets API 36',
        'exports': exports,
    }
    MANIFEST.write_text(json.dumps(manifest, indent=2), encoding='utf-8')
    print('Created', len(exports), 'clearly labelled render concepts; device captures remain pending.')


if __name__ == '__main__':
    main()
