# Učimo kroz igru – Kamioni

Kompletna Android edukativna igra za malu decu. Aplikacija radi bez interneta i sadrži deset ilustrovanih zadataka o kamionima.

## Verzija 2.0

- 10 novih ilustrovanih scena
- ugrađen prirodan srpski glas za pitanja, odgovore i pomoć
- klikabilni delovi slike i veliki tasteri prilagođeni maloj deci
- animacije, konfete, zvuk tačnog i pogrešnog odgovora
- ponavljanje pitanja pritiskom na zvučnik
- glas objašnjava gde treba pritisnuti za sledeći zadatak
- automatski glasovni podsetnik ako dete zastane
- brojanje svakog pokušaja i završna ocena
- posebna pohvala kada je svih 10 zadataka rešeno iz prve
- radi potpuno bez interneta

## APK preko GitHub Actions

Svaka promena na grani `main` automatski pokreće pravljenje APK fajla. Otvori karticu **Actions**, izaberi poslednji uspešan proces i preuzmi artefakt **UcimoKrozIgru-Kamioni-APK**.

## Lokalna izrada

Potreban je Android SDK sa platformom i build-tools paketom, zatim:

```bash
chmod +x build.sh
./build.sh
```

APK će biti napravljen kao `build/UcimoKrozIgru-Kamioni.apk`.
