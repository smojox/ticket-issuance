#!/bin/bash

# CEO Ticket Issuance - Build Environment Setup
# This script sets up the Android development environment

echo "🚀 CEO Ticket Issuance - Build Environment Setup"
echo "=================================================="
echo ""

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   echo "❌ This script should not be run as root"
   echo "Please run as regular user (it will ask for sudo when needed)"
   exit 1
fi

# Detect OS
OS="unknown"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    OS="windows"
fi

echo "Detected OS: $OS"
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Install Java 11
install_java() {
    echo "🔧 Installing Java 11..."
    
    if command_exists java; then
        java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        echo "Java already installed: $java_version"
        
        # Check if it's Java 11 or higher
        major_version=$(echo "$java_version" | cut -d'.' -f1)
        if [[ "$major_version" -ge 11 ]]; then
            echo "✅ Java version is compatible"
            return 0
        else
            echo "⚠️  Java version is too old, installing Java 11..."
        fi
    fi
    
    case $OS in
        linux)
            sudo apt update
            sudo apt install -y openjdk-11-jdk
            ;;
        macos)
            if command_exists brew; then
                brew install openjdk@11
            else
                echo "❌ Homebrew not found. Please install Homebrew first."
                echo "Visit: https://brew.sh/"
                return 1
            fi
            ;;
        windows)
            echo "❌ Windows detected. Please install Java 11 manually:"
            echo "1. Download from: https://adoptium.net/"
            echo "2. Or use chocolatey: choco install openjdk11"
            return 1
            ;;
        *)
            echo "❌ Unsupported OS. Please install Java 11 manually."
            return 1
            ;;
    esac
    
    if command_exists java; then
        echo "✅ Java installed successfully"
        java -version
    else
        echo "❌ Java installation failed"
        return 1
    fi
}

# Install Android SDK
install_android_sdk() {
    echo "🔧 Installing Android SDK..."
    
    ANDROID_HOME="$HOME/android-sdk"
    
    if [[ -d "$ANDROID_HOME" ]]; then
        echo "Android SDK already exists at $ANDROID_HOME"
    else
        echo "Downloading Android SDK..."
        
        cd "$HOME"
        wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
        
        if [[ $? -eq 0 ]]; then
            echo "✅ Download completed"
            unzip -q commandlinetools-linux-9477386_latest.zip
            mkdir -p "$ANDROID_HOME/cmdline-tools/latest"
            mv cmdline-tools/* "$ANDROID_HOME/cmdline-tools/latest/"
            rm -rf cmdline-tools commandlinetools-linux-9477386_latest.zip
            echo "✅ Android SDK extracted"
        else
            echo "❌ Failed to download Android SDK"
            return 1
        fi
    fi
    
    # Set environment variables
    export ANDROID_HOME="$ANDROID_HOME"
    export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"
    export PATH="$PATH:$ANDROID_HOME/platform-tools"
    
    # Add to shell profile
    SHELL_PROFILE=""
    if [[ -f "$HOME/.bashrc" ]]; then
        SHELL_PROFILE="$HOME/.bashrc"
    elif [[ -f "$HOME/.zshrc" ]]; then
        SHELL_PROFILE="$HOME/.zshrc"
    fi
    
    if [[ -n "$SHELL_PROFILE" ]]; then
        echo "Adding environment variables to $SHELL_PROFILE"
        echo "" >> "$SHELL_PROFILE"
        echo "# Android SDK" >> "$SHELL_PROFILE"
        echo "export ANDROID_HOME=\$HOME/android-sdk" >> "$SHELL_PROFILE"
        echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin" >> "$SHELL_PROFILE"
        echo "export PATH=\$PATH:\$ANDROID_HOME/platform-tools" >> "$SHELL_PROFILE"
        echo "✅ Environment variables added to $SHELL_PROFILE"
    fi
    
    # Install required packages
    echo "Installing Android SDK packages..."
    yes | sdkmanager --licenses > /dev/null 2>&1
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" > /dev/null 2>&1
    
    if [[ $? -eq 0 ]]; then
        echo "✅ Android SDK packages installed"
    else
        echo "❌ Failed to install Android SDK packages"
        return 1
    fi
}

# Install Gradle
install_gradle() {
    echo "🔧 Installing Gradle..."
    
    if command_exists gradle; then
        gradle_version=$(gradle --version | grep "Gradle" | head -n 1)
        echo "Gradle already installed: $gradle_version"
        echo "✅ Gradle is available"
        return 0
    fi
    
    case $OS in
        linux)
            sudo apt update
            sudo apt install -y gradle
            ;;
        macos)
            if command_exists brew; then
                brew install gradle
            else
                echo "❌ Homebrew not found. Please install Homebrew first."
                return 1
            fi
            ;;
        windows)
            echo "❌ Windows detected. Please install Gradle manually:"
            echo "1. Download from: https://gradle.org/install/"
            echo "2. Or use chocolatey: choco install gradle"
            return 1
            ;;
        *)
            echo "❌ Unsupported OS. Please install Gradle manually."
            return 1
            ;;
    esac
    
    if command_exists gradle; then
        echo "✅ Gradle installed successfully"
        gradle --version | head -n 1
    else
        echo "❌ Gradle installation failed"
        return 1
    fi
}

# Test build
test_build() {
    echo "🧪 Testing build environment..."
    
    cd "$(dirname "$0")/android-project"
    
    if [[ ! -f "build.gradle.kts" ]]; then
        echo "❌ Android project not found"
        return 1
    fi
    
    echo "Generating Gradle wrapper..."
    gradle wrapper
    
    if [[ $? -eq 0 ]]; then
        echo "✅ Gradle wrapper generated"
        chmod +x ./gradlew
        
        echo "Testing build..."
        ./gradlew tasks --quiet
        
        if [[ $? -eq 0 ]]; then
            echo "✅ Build environment is working!"
            echo ""
            echo "🎉 Setup complete! You can now build the app:"
            echo "cd android-project/"
            echo "./gradlew assembleDebug"
            echo ""
            echo "APK will be generated at:"
            echo "app/build/outputs/apk/debug/app-debug.apk"
        else
            echo "❌ Build test failed"
            return 1
        fi
    else
        echo "❌ Failed to generate Gradle wrapper"
        return 1
    fi
}

# Main execution
main() {
    echo "This script will install:"
    echo "1. Java 11 (OpenJDK)"
    echo "2. Android SDK"
    echo "3. Gradle"
    echo ""
    
    read -p "Continue? (y/n): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Installation cancelled."
        exit 1
    fi
    
    echo ""
    echo "Starting installation..."
    echo ""
    
    install_java || exit 1
    echo ""
    
    install_android_sdk || exit 1
    echo ""
    
    install_gradle || exit 1
    echo ""
    
    test_build || exit 1
    
    echo ""
    echo "🎉 Build environment setup complete!"
    echo ""
    echo "Next steps:"
    echo "1. Restart your terminal (or run: source ~/.bashrc)"
    echo "2. cd android-project/"
    echo "3. ./gradlew assembleDebug"
    echo "4. Install APK: adb install app/build/outputs/apk/debug/app-debug.apk"
}

# Check for required tools
if ! command_exists wget; then
    echo "❌ wget is required but not installed"
    echo "Please install wget first:"
    echo "  Ubuntu/Debian: sudo apt install wget"
    echo "  macOS: brew install wget"
    exit 1
fi

if ! command_exists unzip; then
    echo "❌ unzip is required but not installed"
    echo "Please install unzip first:"
    echo "  Ubuntu/Debian: sudo apt install unzip"
    echo "  macOS: brew install unzip"
    exit 1
fi

# Run main function
main