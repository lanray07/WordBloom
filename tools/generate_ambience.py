"""Recreate the original 16-second ambient loop. Standard library only."""
from pathlib import Path
import math, struct, wave
path=Path(__file__).resolve().parents[1]/'app/src/main/res/raw/garden_ambience.wav'
path.parent.mkdir(parents=True,exist_ok=True)
rate=16000;duration=16
with wave.open(str(path),'wb') as output:
 output.setparams((1,2,rate,0,'NONE','not compressed'))
 frames=bytearray()
 for i in range(rate*duration):
  t=i/rate;envelope=math.sin(math.pi*t/duration)**2
  value=sum(math.sin(2*math.pi*f*t)*.025/(j+1) for j,f in enumerate([130.8125,164.8125,196,261.625]))*envelope
  frames.extend(struct.pack('<h',int(value*32767)))
 output.writeframes(frames)
print(path)
