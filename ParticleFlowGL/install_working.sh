#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "[+] Downloading Gradle wrapper files..."

# Download ACTUAL gradle wrapper files
wget -O gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/master/lib/gradle-launcher-8.5.jar
wget -O gradle/wrapper/gradle-wrapper.jar https://services.gradle.org/distributions/gradle-8.5-bin.zip -O /tmp/gradle-8.5.zip && unzip /tmp/gradle-8.5.zip -d /tmp/gradle && cp /tmp/gradle/gradle-8.5/lib/gradle-launcher-8.5.jar gradle/wrapper/gradle-wrapper.jar

# Create working gradlew
cat > gradlew << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash
export JAVA_HOME=/data/data/com.termux/files/usr
exec java -jar gradle/wrapper/gradle-wrapper.jar "$@"
EOF
chmod +x gradlew

# Build with retries
echo "[+] Building APK..."
for i in {1..3}; do
    if ./gradlew assembleDebug; then
        break
    else
        echo "[+] Retry $i/3..."
        sleep 3
    fi
done

echo "[+] Installing APK..."
adb install -r app/build/outputs/apk/debug/app-debug.apk || adb install app/build/outputs/apk/debug/app-debug.apk

echo "✅ ParticleFlowGL INSTALLED! Launch from drawer."

