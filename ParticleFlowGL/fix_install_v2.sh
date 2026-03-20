#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "[+] ParticleFlowGL v2 - Fixing Gradle wrapper..."

cd ~/storage/shared/ParticleFlowGL

# Generate gradlew wrapper
gradle wrapper --gradle-version 8.5 || echo "[!] Gradle wrapper failed, manual install..."

# If still missing, download manually
if [ ! -f "gradlew" ]; then
    echo "[+] Downloading gradle wrapper manually..."
    wget -O gradle/wrapper/gradle-wrapper.jar "https://github.com/termux/termux-packages/raw/master/packages/gradle/gradle-wrapper.jar"
    chmod +x gradle/wrapper/gradle-wrapper.jar
    echo '#!/data/data/com.termux/files/usr/bin/bash
exec java -jar gradle/wrapper/gradle-wrapper.jar "$@"' > gradlew
    chmod +x gradlew
fi

echo "[+] Building APK..."
./gradlew assembleDebug

echo "[+] Installing..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

echo "✅ DONE! ParticleFlowGL installed."

