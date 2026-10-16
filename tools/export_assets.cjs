const path = require('path');
const fs = require('fs');
const sharp = require(process.env.SHARP_MODULE || 'sharp');
const root = path.resolve(__dirname, '..');
async function main() {
 const out=path.join(root,'store','artwork');fs.mkdirSync(out,{recursive:true});
 for(const size of [114,512,1024]) await sharp(path.join(root,'assets','icon-master.svg'),{density:300}).resize(size,size).ensureAlpha().png().toFile(path.join(out,`wordbloom-icon-${size}.png`));
 await sharp(path.join(root,'assets','promo-master.svg')).png().toFile(path.join(out,'wordbloom-promo-1024x500.png'));
 for(let world=1;world<=6;world++) await sharp(path.join(root,'assets',`world-${world}.svg`)).png().toFile(path.join(root,'assets',`world-${world}.png`));
 console.log('Exported original SVG artwork: icons, six worlds and promotional image.');
}
main().catch(error=>{console.error(error.message);process.exit(1)});
