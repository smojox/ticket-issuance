# CEO Ticket Issuance Android App

![Build Status](https://github.com/your-username/ceo-ticket-issuance/workflows/Build%20CEO%20Ticket%20Issuance%20APK/badge.svg)

A comprehensive Android application for Civil Enforcement Officers (CEO) to issue parking tickets with TMA 2004 compliance.

## 🚀 Features

### ✅ Phase 6 Complete - Ticket Issuance System
- **TMA 2004 Compliance**: Legal ticket format with proper numbering
- **Digital Signature Capture**: Touch-based signature pad for officers
- **Professional Ticket Preview**: Print-ready ticket layout
- **Printing Integration**: System print manager with PDF generation
- **Ticket History Management**: Complete ticket tracking with search
- **Dashboard Integration**: Easy access to all features

### ✅ Previously Completed (Phases 1-5)
- **Authentication System**: Secure login with session management
- **Camera & ANPR**: ML Kit integration for automatic number plate recognition
- **Observation Forms**: Professional data entry with validation
- **Timer Management**: Background countdown with notifications
- **Offline-First Database**: Room database with sync capabilities

## 📱 Quick Start

### Option 1: Download Pre-built APK
1. Go to [Releases](../../releases)
2. Download `app-debug.apk`
3. Install on Android device (API 24+)
4. Login with credentials: `Test` / `Test`

### Option 2: Build from Source

#### Prerequisites
- Android Studio Hedgehog or later
- JDK 11+
- Android SDK API 34

#### Build Steps
```bash
# Clone repository
git clone https://github.com/your-username/ceo-ticket-issuance.git
cd ceo-ticket-issuance

# Open in Android Studio
# File → Open → Select android-project folder

# Or build from command line
cd android-project
./gradlew assembleDebug
```

#### Setup Build Environment (Linux/macOS)
```bash
# Run automated setup script
./setup-build-env.sh

# Or manually install dependencies
# See BUILD_ALTERNATIVES.md for detailed instructions
```

## 🧪 Testing Guide

### Complete Testing Flow (2 minutes)
1. **Login**: Use `Test` / `Test`
2. **Dashboard** → Tap "New Observation"
3. **Camera** → Point at any text or use manual entry
4. **Form** → Enter plate "AB12 CDE", select street/contravention
5. **Timer** → Wait for countdown or stop early → "Proceed to Ticket"
6. **Ticket** → Capture signature → "Generate Ticket"
7. **Preview** → View TMA 2004 compliant ticket → "Print"
8. **History** → Return to dashboard → "History" → View all tickets

### Key Features to Test
- ✅ **ANPR Recognition**: Camera text detection for UK plates
- ✅ **Form Validation**: Required field checking
- ✅ **Timer Persistence**: Background timers survive app restart
- ✅ **Signature Capture**: Touch-based drawing
- ✅ **Ticket Generation**: TMA 2004 format with proper numbering
- ✅ **Print Integration**: System print dialog
- ✅ **History Search**: Search by VRM or ticket number

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Camera**: CameraX
- **ML**: ML Kit Text Recognition
- **Threading**: Coroutines & Flow

### Project Structure
```
android-project/
├── app/src/main/java/com/ceo/ticketissuance/
│   ├── core/           # Core utilities (camera, printing, timers)
│   ├── data/           # Database layer (entities, DAOs, repositories)
│   ├── domain/         # Business logic (models, use cases)
│   └── presentation/   # UI layer (screens, viewmodels, navigation)
```

## 📊 Database Schema

### Pre-populated Test Data
- **User**: Test officer with credentials
- **Streets**: 10 sample locations
- **Contraventions**: 100 parking violation codes
- **Vehicles**: 25 vehicle models across 5 makes

### Core Tables
- `users` - Officer authentication
- `streets` - Street/location data
- `contraventions` - Violation codes with penalties
- `observations` - Enforcement observations
- `tickets` - Generated tickets
- `sync_queue` - Offline sync management

## 🔧 Development

### Build Variants
- **Debug**: Development build with debugging enabled
- **Release**: Production build (unsigned)

### Dependencies
- Room 2.5.0 (Database)
- Compose BOM 2023.10.01 (UI)
- CameraX 1.3.0 (Camera)
- ML Kit 16.0.0 (Text Recognition)
- Hilt 2.48 (Dependency Injection)

### CI/CD
- **GitHub Actions**: Automated builds on push
- **Artifacts**: APK files uploaded to releases
- **Testing**: Automated build verification

## 📋 Roadmap

### Phase 7 (Planned)
- **Sync Integration**: Backend synchronization
- **Offline Queue**: Enhanced offline capability
- **Report Generation**: Daily/weekly reports
- **Multi-user Support**: Multiple officer accounts

### Phase 8 (Planned)
- **Bluetooth Printing**: Mobile thermal printer support
- **Advanced ANPR**: Enhanced plate recognition
- **Geolocation**: GPS-based location tracking
- **Voice Notes**: Audio note recording

## 🐛 Known Issues

### Current Limitations
- **Bluetooth Printing**: Framework ready but not fully implemented
- **Network Sync**: Placeholder implementation
- **Multi-language**: English only

### Requirements
- **Android**: API 24+ (Android 7.0+)
- **Camera**: Physical device required for ANPR
- **Storage**: ~50MB app size
- **RAM**: 2GB minimum

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

For issues and questions:
- Open an [Issue](../../issues)
- Check [Documentation](../../wiki)
- Review [Build Alternatives](BUILD_ALTERNATIVES.md)

## 🎯 Build Status

| Branch | Status | APK |
|--------|---------|-----|
| main | ![Build](https://github.com/your-username/ceo-ticket-issuance/workflows/Build%20CEO%20Ticket%20Issuance%20APK/badge.svg) | [Download](../../releases/latest) |

---

**Ready for Testing**: Complete ticket issuance system with TMA 2004 compliance and professional UI.