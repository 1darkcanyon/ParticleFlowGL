#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "🔥 Building ParticleFlowGL APK (RenderScript → OpenGL)..."

# Compile Java
mkdir -p app/build/classes
javac -source 1.8 -target 1.8 \
  -cp $PREFIX/share/android-sdk/platforms/android-35/android.jar \
  -d app/build/classes \
  app/src/main/java/com/nfaralli/particleflow/*.java

# Create DEX
mkdir -p app/build/dex
dx --dex --output=app/build/dex/classes.dex app/build/classes/com/nfaralli/particleflow/*.class

# Package APK (needs AndroidManifest.xml in app/src/main/)
aapt package -f -M app/src/main/AndroidManifest.xml \
  -I $PREFIX/share/android-sdk/platforms/android-35/android.jar \
  -S app/src/main/res -F app/build/unsigned.apk \
  app/build/dex

# Align + Sign
zipalign -f 4 app/build/unsigned.apk app/build/ParticleFlowGL.apk
apksigner sign --ks ~/.android/debug.keystore --ks-pass pass:android \
  app/build/ParticleFlowGL.apk

# Install
adb install -r app/build/ParticleFlowGL.apk

echo "✅ INSTALLED! Launch from app drawer. Touch = 16k particles swarm!"
