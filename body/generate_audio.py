"""Generate bundled English recordings at the voice's normal speaking rate."""
import asyncio, json
from pathlib import Path
import edge_tts
ROOT = Path(__file__).parent

def key(text):
    h = 5381
    for c in text: h = ((h * 33) ^ ord(c)) & 0xffffffff
    return 'v' + str(h)

async def main():
    dest = ROOT / 'assets' / 'audio'
    dest.mkdir(parents=True, exist_ok=True)
    texts = json.loads((ROOT / 'speech.json').read_text())
    for i, text in enumerate(texts):
        path = dest / (key(text) + '.mp3')
        if path.exists() and path.stat().st_size > 1000: continue
        for attempt in range(3):
            try:
                await edge_tts.Communicate(text, 'en-GB-SoniaNeural', rate='+0%', pitch='+0Hz').save(str(path))
                assert path.stat().st_size > 1000
                print(f'Voice {i+1}/{len(texts)} ready', flush=True)
                break
            except Exception:
                if attempt == 2: raise
                await asyncio.sleep(3)
    assert len(list(dest.glob('*.mp3'))) == len(texts)
asyncio.run(main())
