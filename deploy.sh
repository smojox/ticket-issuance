#!/bin/bash

# CEO Ticket Issuance - Deployment Script
# Phase 6 - Interim Build

echo "🚀 CEO Ticket Issuance - Deployment Script"
echo "==========================================="
echo "Phase: 6 (Ticket Issuance System)"
echo "Build Date: $(date)"
echo ""

# Navigate to project directory
cd "$(dirname "$0")/android-project"

# Check if we're in the right directory
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Error: Not in Android project directory"
    echo "Please run this script from the project root"
    exit 1
fi

echo "📁 Current directory: $(pwd)"
echo ""

# Function to check if command exists
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "❌ $1 is not installed or not in PATH"
        return 1
    else
        echo "✅ $1 found"
        return 0
    fi
}

# Check prerequisites
echo "🔍 Checking prerequisites..."
echo "----------------------------"

# Check for Java
if check_command "java"; then
    java_version=$(java -version 2>&1 | head -n 1)
    echo "   Java: $java_version"
fi

# Check for Gradle
gradle_available=false
if check_command "gradle"; then
    gradle_version=$(gradle --version | grep "Gradle" | head -n 1)
    echo "   Gradle: $gradle_version"
    gradle_available=true
fi

# Check for gradlew
if [ -f "./gradlew" ]; then
    echo "✅ gradlew found"
    gradlew_available=true
else
    echo "⚠️  gradlew not found - will try to generate"
    gradlew_available=false
fi

# Check for Android SDK
if [ -n "$ANDROID_HOME" ]; then
    echo "✅ ANDROID_HOME set: $ANDROID_HOME"
else
    echo "⚠️  ANDROID_HOME not set - may cause issues"
fi

echo ""

# Generate gradle wrapper if needed
if [ "$gradlew_available" = false ] && [ "$gradle_available" = true ]; then
    echo "🔧 Generating Gradle wrapper..."
    gradle wrapper
    if [ $? -eq 0 ]; then
        echo "✅ Gradle wrapper generated successfully"
        gradlew_available=true
    else
        echo "❌ Failed to generate Gradle wrapper"
    fi
fi

# Make gradlew executable
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    echo "✅ Made gradlew executable"
fi

echo ""

# Build function
build_project() {
    local build_type=$1
    local gradle_task=$2
    local description=$3
    
    echo "🏗️  Building $description..."
    echo "Task: $gradle_task"
    echo "----------------------------"
    
    if [ "$gradlew_available" = true ]; then
        echo "Using ./gradlew..."
        ./gradlew $gradle_task
    elif [ "$gradle_available" = true ]; then
        echo "Using gradle..."
        gradle $gradle_task
    else
        echo "❌ Neither gradlew nor gradle available"
        return 1
    fi
    
    local exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        echo "✅ $description build completed successfully"
        
        # Find and display APK location
        find app/build/outputs/apk -name "*.apk" -type f | while read apk; do
            echo "📦 APK: $apk"
            echo "   Size: $(du -h "$apk" | cut -f1)"
        done
        
        return 0
    else
        echo "❌ $description build failed (exit code: $exit_code)"
        return 1
    fi
}

# Main build menu
echo "🎯 Build Options:"
echo "1. Debug Build (recommended for testing)"
echo "2. Release Build (unsigned)"
echo "3. Clean Build"
echo "4. Install Debug APK"
echo "5. Check Project Status"
echo ""

read -p "Select option (1-5): " choice

case $choice in
    1)
        build_project "debug" "assembleDebug" "Debug APK"
        ;;
    2)
        build_project "release" "assembleRelease" "Release APK"
        ;;
    3)
        echo "🧹 Cleaning project..."
        if [ "$gradlew_available" = true ]; then
            ./gradlew clean
        elif [ "$gradle_available" = true ]; then
            gradle clean
        fi
        echo "✅ Clean completed"
        ;;
    4)
        echo "📱 Installing debug APK..."
        if [ "$gradlew_available" = true ]; then
            ./gradlew installDebug
        elif [ "$gradle_available" = true ]; then
            gradle installDebug
        fi
        ;;
    5)
        echo "📊 Project Status:"
        echo "----------------------------"
        echo "Project: CEO Ticket Issuance"
        echo "Package: com.ceo.ticketissuance"
        echo "Phase: 6 (Ticket Issuance System)"
        echo ""
        echo "Features implemented:"
        echo "✅ Authentication (Test/Test)"
        echo "✅ Camera & ANPR"
        echo "✅ Observation Forms"
        echo "✅ Timer System"
        echo "✅ Ticket Generation"
        echo "✅ Digital Signatures"
        echo "✅ Ticket Printing"
        echo "✅ Ticket History"
        echo ""
        echo "Database tables:"
        echo "- users (1 test user)"
        echo "- streets (10 locations)"
        echo "- contraventions (100 codes)"
        echo "- vehicle_makes (5 makes)"
        echo "- vehicle_models (25 models)"
        echo "- observations (dynamic)"
        echo "- tickets (dynamic)"
        echo "- sync_queue (dynamic)"
        ;;
    *)
        echo "❌ Invalid option"
        exit 1
        ;;
esac

echo ""
echo "🎉 Deployment script completed!"
echo ""
echo "📋 Next Steps:"
echo "1. Install APK on Android device (API 24+)"
echo "2. Login with Test/Test credentials"
echo "3. Test complete flow: Dashboard → Camera → Form → Timer → Ticket"
echo "4. Check ticket history and printing functionality"
echo ""
echo "🐛 Troubleshooting:"
echo "- Check Android Studio Logcat for errors"
echo "- Ensure camera permissions are granted"
echo "- Use physical device for camera testing"
echo "- Clear app data if database issues occur"
echo ""
echo "📚 Documentation: See INTERIM_BUILD_PHASE_6.md for details"