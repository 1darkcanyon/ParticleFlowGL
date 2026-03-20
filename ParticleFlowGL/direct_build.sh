#!/data/data/com.termux/files/usr/bin/bash
echo "[+] Direct APK build - No Gradle needed"

pkg install aapt dx zipalign apksigner openjdk-17 android-tools -y

# Compile Java
mkdir -p app/build/classes
javac -d app/build/classes \
  -cp $(pkg-config --cflags --libs android-sdk) \
  -source 1.8 -target 1.8 \
  app/src/main/java/com/nfaralli/particleflow/*.java

# Create DEX
dx --dex --output=app/build/classes.dex app/build/classes

# Package APK
aapt package -f -m -J app/src/main/gen \
  -M app/src/main/AndroidManifest.xml \
  -S app/src/main/res \
  -I $(dirname $(pkg-config --variable=android_jar android-sdk))/android-35/android.jar \
  -F app/build/unsigned.apk

# Align & sign
zipalign -f 4 app/build/unsigned.apk app/build/app-debug.apk
apksigner sign --ks ~/.android/debug.keystore --ks-pass pass:android app/build/app-debug.apk

echo "[+] Installing APK..."
adb install -r app/build/app-debug.apk

echo "✅ ParticleFlowGL INSTALLED! Touch screen for particle flow."

