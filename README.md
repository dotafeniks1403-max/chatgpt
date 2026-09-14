# Učimo kroz igru – Kamioni

Interaktivna Android knjiga za decu uzrasta približno 2–5 godina.

## Šta aplikacija sadrži

- 10 interaktivnih zadataka o kamionima
- glasovna pitanja na srpskom jeziku
- zvučnu i vizuelnu povratnu informaciju
- zvezdice i završnu nagradu
- rad bez interneta, reklama i prijavljivanja

## Instalacija gotovog APK-a

Preuzmite `UcimoKrozIgru-Kamioni.apk`, otvorite ga na Android telefonu i dozvolite
instalaciju iz tog izvora kada telefon to zatraži.

## Izrada iz izvornog koda

Projekat je namerno napravljen bez spoljnih biblioteka. Potrebni su Java 8 ili
novija i Android SDK sa platformom i build-tools paketom. Pokrenite:

```bash
chmod +x build.sh
./build.sh
```

Gotov paket biće u `build/UcimoKrozIgru-Kamioni.apk`.

Napomena: APK se trenutno potpisuje razvojnim ključem za direktnu instalaciju.
Za objavljivanje u Google Play prodavnici treba napraviti trajni produkcioni ključ.
