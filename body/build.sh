#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "$0")" && pwd)"
android_sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/lib/android-sdk}}"

android_jar="$(find "$android_sdk/platforms" -name android.jar -print | sort -V | tail -1)"
aapt_bin="$(find "$android_sdk/build-tools" -type f -name aapt -print | sort -V | tail -1)"
d8_bin="$(find "$android_sdk/build-tools" -type f -name d8 -print | sort -V | tail -1)"
zipalign_bin="$(find "$android_sdk/build-tools" -type f -name zipalign -print | sort -V | tail -1)"
apksigner_bin="$(find "$android_sdk/build-tools" -type f -name apksigner -print | sort -V | tail -1)"

if [[ -z "$android_jar" || -z "$aapt_bin" || -z "$d8_bin" || -z "$zipalign_bin" || -z "$apksigner_bin" ]]; then
  echo "Android SDK nije kompletan. Potrebni su platforma i build-tools." >&2
  exit 1
fi

build_dir="$project_dir/build"
rm -rf "$build_dir"
mkdir -p "$build_dir/generated" "$build_dir/classes" "$build_dir/dex"

"$aapt_bin" package -f -m \
  -J "$build_dir/generated" \
  -M "$project_dir/AndroidManifest.xml" \
  -S "$project_dir/res" \
  -A "$project_dir/assets" \
  -I "$android_jar"

find "$project_dir/src/main/java" "$build_dir/generated" -name '*.java' -print0 \
  | xargs -0 javac -encoding UTF-8 -source 8 -target 8 \
      -bootclasspath "$android_jar" -d "$build_dir/classes"

find "$build_dir/classes" -name '*.class' -print0 \
  | xargs -0 "$d8_bin" --lib "$android_jar" --min-api 23 --output "$build_dir/dex"

"$aapt_bin" package -f \
  -M "$project_dir/AndroidManifest.xml" \
  -S "$project_dir/res" \
  -A "$project_dir/assets" \
  -I "$android_jar" \
  -F "$build_dir/app-unsigned.apk"

(cd "$build_dir/dex" && "$aapt_bin" add "$build_dir/app-unsigned.apk" classes.dex >/dev/null)
"$zipalign_bin" -f 4 "$build_dir/app-unsigned.apk" "$build_dir/app-aligned.apk"

keystore="$build_dir/debug.keystore"
keytool -genkeypair -noprompt -v \
  -keystore "$keystore" -storepass android -keypass android \
  -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Ucimo kroz igru, O=PG Jovic, C=RS" >/dev/null 2>&1

output="$build_dir/My-Body-English.apk"
"$apksigner_bin" sign \
  --ks "$keystore" --ks-pass pass:android --key-pass pass:android \
  --out "$output" "$build_dir/app-aligned.apk"
"$apksigner_bin" verify --verbose "$output"
echo "APK napravljen: $output"
