#!/data/data/com.termux/files/usr/bin/bash
set -e

pkg install openjdk-17 aapt dx zipalign apksigner android-tools

mkdir -p app/build/classes
# Compile with minimal Android stubs (works without full SDK)
javac -source 1.8 -target 1.8 -d app/build/classes \
  app/src/main/java/com/nfaralli/particleflow/*.java

dx --dex --output=app/build/classes.dex app/build/classes

aapt package -f -M app/src/main/AndroidManifest.xml \
  -S app/src/main/res \
  -I $PREFIX/share/android-sdk/platforms/android-34/android.jar \
  -F app/build/ParticleFlowGL-unsigned.apk app/build/classes.dex

zipalign -f 4 app/build/ParticleFlowGL-unsigned.apk app/build/ParticleFlowGL.apk
apksigner sign --ks ~/.android/debug.keystore --ks-pass pass:android app/build/ParticleFlowGL.apk

echo "✅ APK built! Install with: adb install -r app/build/ParticleFlowGL.apk"
