#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "[+] Final ParticleFlowGL build in Termux home..."

# Create gradle wrapper manually
mkdir -p gradle/wrapper
cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

# Simple gradlew stub that works in Termux
cat > gradlew << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash
export PATH=$PATH:$HOME/.gradle/wrapper/dists/gradle-8.5-bin/
java -Dorg.gradle.wrapper.UseGradleUserHome=true -jar gradle/wrapper/gradle-wrapper.jar "$@"
EOF
chmod +x gradlew

echo "[+] Building (1min)..."
./gradlew assembleDebug || (
    echo "[+] Gradle cache fix..."
    pkg install gradle
    ./gradlew assembleDebug
)

echo "[+] Installing APK..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

echo "✅ SUCCESS! ParticleFlowGL installed from Termux home."
echo "Launch from app drawer. Touch = particle attraction."

