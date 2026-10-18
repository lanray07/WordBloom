"""Build original, language-specific word-connect seed data. Not translated answers.

Initial vocabulary requires separate linguistic review; structural tests cannot
certify age suitability, idiom or regional preference. Reviewed JSON is canonical.
"""
import argparse
import json
import unicodedata
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORDS = {
 'de': 'HAUS AUS SAU;BAUM BAU RAUM;GARTEN GARN RAT;BLUME BLUMEN LEBEN;SONNE SONNEN NONNE;STERNE STERN ERNST;WASSER WAS WER;WOLKE WOLKEN WELK;BLATT BLÄTTER RAT;REISE REIS EIS;STEIN STEINE EINS;LAMPE PALME AMPEL;FENSTER FERN REST;MALER MALEN ARM;LERNEN LERNE ERLEN;GELB GELBE BELEG;WALD BALD BALL;WIESE WIESEN WEIN;GÄRTNER GÄRTEN GARN;BLUMEN BLUME LEBEN;REGENTAG REGEN TAGE;MORGEN MORGENS SORGEN;FREUNDE FREUND FREUDE;SPIELEN SPIEL LEISE',
 'es': 'CASA ASA SACA;PATO PATA TAPA;GATO GOTA TOGA;MESA MES AMES;LAGO GOLA ALGO;ROSA AROS RASO;CAMINO MANO MINA;PLANTA PLAN ALTA;FLORES FLOR ROLES;CANTO CANTA TANTO;NUBES NUBE SUBEN;TIERRA TIRA ARTE;ARENA RANA NENA;VERANO VER VENA;MOLINO LINO LIMO;AMIGO MIGA GOMA;LIBRO LIBRA BRÍO;MADERA AMAR RAMA;JARDINES JARDÍN NIDOS;ESTRELLA ELLA TALLER;MARIPOSA RAMO PASO;VENTANAS VENTANA VENTAS;PALABRAS PALABRA ALAS;CANTORES CANTOR CANTO',
 'fr': 'CHAT CHATS TAS;LUNE UNE NUE;ROSE OSE OSER;TARTE TARE ART;PORTE PORT TOP;ARBRE BARRE RARE;JARDIN RADIN DRAIN;FLEURS FLEUR LEURS;SOLEIL SOLE SOIE;NUAGE NAGE ANGE;PLANTE PLAN LENT;CHEMIN MINE CHIEN;MAISON MAIS AMIS;RIVAGE RIVE VRAI;TERRE TERRES RESTE;ÉTOILES ÉTOILE TOILE;LUMIÈRE MIEL LIRE;POMMES POMME SOMME;JARDINS JARDIN RADINS;PRAIRIE PAIRE PRIER;MATINÉE MATIN AMIE;CHANTER CHANT ANCRE;COULEURS COULEUR COURS;PAPILLON PAPI PION',
 'it': 'CASA CASO COSA;MARE REMA RAMO;SOLE SOLO LESO;GATTO ATTO ATTI;RANA RANE NERA;PANE PENA APE;FIORE FIORI FORI;PRATO PORTA ORA;ALBERO ALBO ALOE;PIANTA PIANO TANA;VENTO VENTI NOTE;MONTE MENTO NOTE;STELLA STALLE SELLA;GIORNO GIRI GIOCO;SENTIERO SENTO RETI;LIMONE LIMO MENO;NUVOLA UOVA VOLA;SABBIA ABBIA BASI;GIARDINO GRANO GIORNI;FARFALLA FARLA ALLA;PRIMAVERA PRIMA RAMI;COLORARE COLORE ORALE;PAROLE PAROLA PERLA;CANTARE CANTA CARTA',
 'nl': 'KAT TAK KART;BOOM BOON OOM;ROOS ROOK OOR;HUIS HUISJE IJS;TUIN TIN UIT;STER REST STERK;BLOEM BLOEMEN BOEM;WATER WAT WARE;STEEN STENEN EEN;TAFEL TAFELS FEL;LAMP PALM MAP;WOLKEN WOLK WELK;GROEN GROENE OGEN;PADEN PAD PAN;ZOMER ZOMERS ROEM;REGEN REGENT EREN;PLANT PLANTEN PLAN;BLADEREN BLAD ADER;TUINEN TUIN TUNEN;BLOEMEN BLOEM MOLEN;VRIENDEN VRIEND VINDEN;SPELEN SPEL LENS;STERREN STER RESTEN;WANDELEN WANDEL LANDEN',
 'pl': 'KOT KTO TOK;LAS LATA STAL;DOM MODA DAM;MAK MAMA RAMA;SEN SER NOS;ROK KROK KOK;KWIAT WITA WAT;OGRÓD GRÓD RÓG;WODA WADY DWA;LATO LATA LATAĆ;RYBA RYBY BAR;POLE POLA LEPI;KAMIENIE KAMIEŃ MINA;TRAWA TRAWY WARTA;DRZEWO DRZEWA WZÓR;SŁOWO SŁOWA WOSK;MOTYL MOTYLE TYLE;NOCE NOC OCEAN;OGRODY OGRÓD RODY;KWIATY KWIAT WITA;GWIAZDA GWIAZDY WAGA;ZABAWA ZABAW BAZA;WIOSNA WIOSNY WINA;SPACER SPACERY PRACE',
 'pt': 'CASA CASO SACO;GATO GOTA TOGA;PATO TOPA PATA;MESA MESAS ESSA;ROSA AROS RASO;LAGO GALO GOLA;PLANTA PLANO TALA;FLORES FLOR SOFRE;CAMINHO NINHO MINHA;PEDRA PERDA PARE;LIVRO LIVRE LIVRES;AREIA AREAR RAIA;NUVEM NUVENS NEM;VENTO VENTOS NOVO;TERRA RETA ARTE;MADEIRA MADEIRAS MARÉ;AMIGOS AMIGO MAGO;ESTRELA ESTRELAS LESTE;JARDINS JARDIM JARDAS;PALAVRA PALAVRAS LAVRA;CANTAR CANTA CARTA;BORBOLETA BOLA BOLETO;SEMENTES SEMENTE MENTE;PRIMAVERA PRIMA RIMA',
}

def initial_drafts():
 packs = {}
 for locale, data in WORDS.items():
  triples = [x.strip() for x in data.split(';')]
  triples.sort(key=lambda triple:max(map(len,triple.split())))
  seeds = []
  for i, triple in enumerate(triples):
   words = [unicodedata.normalize('NFC', w) for w in triple.split()]
   assert len(set(words)) == 3, (locale, triple)
   counts = Counter()
   for word in words: counts |= Counter(word)
   bag = ''.join(c * n for c, n in counts.items())
   assert len(bag) <= 12, (locale, triple, bag)
   longest=max(map(len,words))
   difficulty='BEGINNER' if longest<=4 else 'INTERMEDIATE' if longest<=6 else 'ADVANCED' if longest<=7 else 'EXPERT'
   seeds.append(dict(letters=bag, targets=words, difficulty=difficulty, world=i//4))
  assert len(seeds) == 24
  packs[locale] = seeds
 return packs

def render_kotlin(packs):
 assert set(packs)==set(WORDS),'Expected seven vocabulary languages'
 lines = ['package com.wordbloom.engine', '', '/** Generated original vocabulary. Activation requires separate hash-bound linguistic review. */', 'object DraftLanguagePacks {', ' val locales = listOf("de", "es", "fr", "it", "nl", "pl", "pt-BR", "pt-PT")', ' private val seeds: Map<String,List<Seed>> = mapOf(']
 for locale, seeds in packs.items():
  assert len(seeds)==24,(locale,'Expected 24 puzzles')
  lines.append('  '+json.dumps(locale)+' to listOf(')
  for index,s in enumerate(seeds):
   assert len(s['letters'])<=12 and s['world']==index//4,(locale,index)
   assert len(set(s['targets']))==3,(locale,index)
   for word in s['targets']:
    assert word==unicodedata.normalize('NFC',word)==word.upper() and word.isalpha() and len(word)>=3,(locale,word)
    assert Counter(word)<=Counter(s['letters']),(locale,word,'Cannot form from bag')
   assert s['difficulty'] in ['BEGINNER','INTERMEDIATE','ADVANCED','EXPERT']
   lines.append('   Seed('+json.dumps(s['letters'],ensure_ascii=False)+', '+json.dumps(' '.join(s['targets']),ensure_ascii=False)+', difficulty=Difficulty.'+s['difficulty']+'),')
  lines.append('  ),')
 lines += [' )', ' fun seeds(language: String): List<Seed> = seeds[if(language.startsWith("pt-")) "pt" else language] ?: error("No draft for $language")', ' fun puzzles(language: String): List<Puzzle> {', '  require(language in locales)', '  return seeds(language).mapIndexed { i,s -> Crossword.generate("$language-${i+1}",s.letters,s.targets.split(\' \'),emptySet(),s.difficulty,i/4,811+i) }', ' }', '}']
 return '\n'.join(lines)+'\n'

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--regenerate-drafts',action='store_true');args=ap.parse_args()
 path=ROOT/'localization/gameplay-drafts.json'
 if args.regenerate_drafts or not path.exists():
  path.write_text(json.dumps(dict(status='draft; separate vocabulary and difficulty review required before activation',packs=initial_drafts()),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 packs=json.loads(path.read_text(encoding='utf-8'))['packs']
 (ROOT/'engine/src/main/kotlin/com/wordbloom/engine/DraftLanguagePacks.kt').write_text(render_kotlin(packs),encoding='utf-8')
 print('Prepared', sum(map(len,packs.values())), 'language-specific puzzle drafts; Portuguese shared regional vocabulary.')

if __name__ == '__main__': main()
