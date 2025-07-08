# Quick Start Guide - CEO Ticket Issuance App

## Immediate Testing (5 Minutes)

### 1. Build the App
```bash
# Navigate to project
cd /home/simonbradley/claude-dashboard/ticket-issuance

# Run deployment script
./deploy.sh

# Select option 1 (Debug Build)
```

### 2. Install on Device
```bash
# Connect Android device via USB
# Enable USB debugging in Developer Options

# Install APK
adb install android-project/app/build/outputs/apk/debug/app-debug.apk

# Or use deployment script option 4
```

### 3. Test Complete Flow (2 Minutes)

#### Login
- **Username**: `Test`
- **Password**: `Test`

#### Complete Ticket Flow
1. **Dashboard** → Tap "New Observation"
2. **Camera** → Point at any text OR tap "Manual Entry"
3. **Form** → Enter plate "AB12 CDE", select street/contravention
4. **Timer** → Wait for countdown OR tap "Stop" → "Proceed to Ticket"
5. **Ticket** → Tap "Capture Signature" → Draw signature → "Generate Ticket"
6. **Preview** → View ticket → Tap "Print" (opens system print dialog)
7. **History** → Back to dashboard → Tap "History" → View all tickets

### 4. Key Features to Test

✅ **Authentication**: Test/Test login
✅ **ANPR**: Camera text recognition
✅ **Forms**: Vehicle data entry with validation
✅ **Timer**: Background countdown with notifications
✅ **Signatures**: Touch-based signature capture
✅ **Tickets**: TMA 2004 compliant format
✅ **Printing**: System print integration
✅ **History**: Search and filter tickets

## Alternative Setup (If Build Issues)

### Option 1: Android Studio
```bash
# Open in Android Studio
open android-project/

# Build → Generate Signed Bundle/APK
# Run → Run 'app'
```

### Option 2: Manual Gradle
```bash
cd android-project/

# Generate wrapper
gradle wrapper

# Build
./gradlew assembleDebug

# Install
./gradlew installDebug
```

## Test Data Available

### Pre-populated Database
- **User**: Test/Test credentials
- **Streets**: 10 locations (High Street, Church Lane, etc.)
- **Contraventions**: 100 codes with penalties
- **Vehicles**: 25 models across 5 makes

### Sample Test Scenarios
1. **Quick Test**: AB12 CDE → High Street → Code 01 (Parking meter)
2. **Timer Test**: Start observation → Wait 5 minutes → Issue ticket
3. **Signature Test**: Generate ticket → Draw signature → Print
4. **History Test**: Generate multiple tickets → Search by VRM

## Expected Results

### Working Features
- ✅ End-to-end ticket generation
- ✅ Professional UI with Material Design 3
- ✅ Camera integration with ANPR
- ✅ Background timer service
- ✅ Digital signature capture
- ✅ Print integration
- ✅ Ticket history and search

### Performance Targets
- **App startup**: < 3 seconds
- **Camera launch**: < 2 seconds
- **Form validation**: Instant
- **Ticket generation**: < 1 second
- **Database queries**: < 500ms

## Troubleshooting

### Common Issues
1. **Build fails**: Check Java/Android SDK versions
2. **Camera not working**: Test on physical device
3. **Login fails**: Use exact "Test"/"Test" (case-sensitive)
4. **Timer not working**: Check notification permissions

### Debug Tools
- **Logcat**: Monitor in Android Studio
- **Database Inspector**: View Room database
- **Layout Inspector**: Check UI issues

## What's Next

After testing, you can:
1. **Report issues**: Any bugs or UI improvements needed
2. **Request features**: Additional functionality for Phase 7
3. **Deployment**: Prepare for production release
4. **Integration**: Connect with backend systems

---

**Ready to Test**: The app is fully functional with all Phase 6 features implemented. Start with the 5-minute test flow above!