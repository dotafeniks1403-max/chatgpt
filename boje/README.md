# Učimo boje
Zasebna Android aplikacija za malu decu: deset boja, slučajan redosled pitanja i četiri velika kruga, šest primera za svaku boju, ohrabrenje pri grešci, ponavljanje glasa i deset zvezdica na kraju. Nema reklama, naloga ni mrežne dozvole. Srpski ženski glas sr-RS-SophieNeural snima se pri izradi normalnom brzinom (+0%) i ugrađuje u APK. Glas radi bez interneta.

Izrada: `pip install edge-tts`, `python generate_audio.py`, pa `bash build.sh` uz Android SDK i JDK 17. Rezultat je build/Ucimo-Boje.apk. GitHub Actions obavlja iste korake.
