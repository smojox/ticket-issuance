#!/bin/bash

# CEO Ticket Issuance - Build Health Check
# This script checks for common build issues after Kotlin/KSP updates

echo "🔍 CEO Ticket Issuance - Build Health Check"
echo "============================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    if [[ $1 == "OK" ]]; then
        echo -e "${GREEN}✅ $2${NC}"
    elif [[ $1 == "WARNING" ]]; then
        echo -e "${YELLOW}⚠️  $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
    fi
}

# Check if we're in the right directory
if [[ ! -f "build.gradle.kts" ]]; then
    print_status "ERROR" "Not in android-project directory. Please run from android-project/"
    exit 1
fi

echo "Checking build configuration..."
echo ""

# Check 1: Kotlin and KSP versions
echo "1. Checking Kotlin and KSP versions..."
kotlin_version=$(grep 'kotlin.android' ../build.gradle.kts | sed 's/.*version "\(.*\)".*/\1/')
ksp_version=$(grep 'devtools.ksp' ../build.gradle.kts | sed 's/.*version "\(.*\)".*/\1/')

if [[ "$kotlin_version" == "1.9.23" && "$ksp_version" == "1.9.24-1.0.20" ]]; then
    print_status "OK" "Kotlin $kotlin_version and KSP $ksp_version versions are compatible"
else
    print_status "WARNING" "Kotlin: $kotlin_version, KSP: $ksp_version - verify compatibility"
fi

# Check 2: Compose compiler version
echo "2. Checking Compose compiler version..."
compose_version=$(grep 'kotlinCompilerExtensionVersion' app/build.gradle.kts | sed 's/.*= "\(.*\)".*/\1/')

if [[ "$compose_version" == "1.5.11" ]]; then
    print_status "OK" "Compose compiler version $compose_version is compatible with Kotlin 1.9.23"
else
    print_status "WARNING" "Compose compiler version: $compose_version - verify Kotlin compatibility"
fi

# Check 3: Java target version
echo "3. Checking Java target version..."
java_version=$(grep 'jvmTarget' app/build.gradle.kts | sed 's/.*jvmTarget = "\(.*\)".*/\1/')

if [[ "$java_version" == "17" ]]; then
    print_status "OK" "Java target version $java_version is correctly set"
else
    print_status "WARNING" "Java target version: $java_version - should be 17"
fi

# Check 4: Room and Hilt versions
echo "4. Checking Room and Hilt versions..."
room_version=$(grep 'androidx.room:room-runtime' app/build.gradle.kts | sed 's/.*:\(.*\)".*/\1/')
hilt_version=$(grep 'com.google.dagger:hilt-android' app/build.gradle.kts | sed 's/.*:\(.*\)".*/\1/')

if [[ "$room_version" == "2.6.1" && "$hilt_version" == "2.48" ]]; then
    print_status "OK" "Room $room_version and Hilt $hilt_version versions are compatible with KSP"
else
    print_status "WARNING" "Room: $room_version, Hilt: $hilt_version - verify KSP compatibility"
fi

# Check 5: KSP configuration
echo "5. Checking KSP configuration..."
if grep -q "ksp.incremental=true" gradle.properties; then
    print_status "OK" "KSP incremental compilation is enabled"
else
    print_status "WARNING" "KSP incremental compilation is not configured"
fi

# Check 6: Gradle wrapper
echo "6. Checking Gradle wrapper..."
if [[ -f "gradlew" ]]; then
    print_status "OK" "Gradle wrapper is present"
else
    print_status "WARNING" "Gradle wrapper not found - run './gradlew wrapper' to generate"
fi

# Check 7: Schema export directory
echo "7. Checking Room schema export..."
if [[ -d "schemas" ]]; then
    print_status "OK" "Room schema export directory exists"
else
    print_status "WARNING" "Room schema export directory not found - will be created on first build"
fi

# Check 8: Build directory permissions
echo "8. Checking build directory permissions..."
if [[ -w "." ]]; then
    print_status "OK" "Build directory is writable"
else
    print_status "ERROR" "Build directory is not writable"
fi

echo ""
echo "Build health check complete!"
echo ""

# Quick build test
echo "Running quick build test..."
if [[ -f "gradlew" ]]; then
    if ./gradlew tasks --quiet > /dev/null 2>&1; then
        print_status "OK" "Gradle can execute tasks successfully"
    else
        print_status "ERROR" "Gradle task execution failed - check environment setup"
    fi
else
    print_status "WARNING" "Cannot run build test - Gradle wrapper not found"
fi

echo ""
echo "💡 To run a full build:"
echo "   ./gradlew clean assembleDebug"
echo ""
echo "💡 To run tests:"
echo "   ./gradlew test"
echo ""
echo "💡 To check for dependency updates:"
echo "   ./gradlew dependencyUpdates"