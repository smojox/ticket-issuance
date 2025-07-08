# Observation Flow with Camera and ANPR

## Overview
The observation flow is the primary feature for parking enforcement officers to start monitoring vehicles. It combines camera functionality with ANPR (Automatic Number Plate Recognition) to capture vehicle details and begin the observation timer.

## Flow Sequence

### 1. Camera Activation
- **Trigger**: User taps "OBSERVATION" button from dashboard
- **Action**: Immediately opens camera in full-screen mode
- **Camera Setup**: Rear camera with auto-focus and optimal settings for plate recognition

### 2. ANPR Processing
- **Real-time Recognition**: Continuous scanning for number plates
- **Visual Feedback**: Overlay showing detected text regions
- **Confirmation**: Haptic feedback when plate successfully captured

### 3. Observation Details Entry
- **Auto-populate**: VRM field filled from ANPR
- **Timer Start**: Observation timer begins immediately
- **Form Completion**: User fills mandatory and optional fields

## Screen Layouts

### Camera Screen
```
┌─────────────────────────────────────┐
│  [X]                    [⚡] [🔄]   │
│                                     │
│                                     │
│         CAMERA VIEWFINDER           │
│                                     │
│     ┌─────────────────────────┐     │
│     │  Detected: AB12 CDE     │     │
│     │                         │     │
│     │    Recognition Box      │     │
│     │                         │     │
│     └─────────────────────────┘     │
│                                     │
│                                     │
│    "Point camera at number plate"   │
│                                     │
│           [CAPTURE]                 │
└─────────────────────────────────────┘
```

### Observation Details Screen
```
┌─────────────────────────────────────┐
│  [←] OBSERVATION      Timer: 00:45   │
│                                     │
│  VRM: AB12 CDE                      │
│  ┌─────────────────────────────────┐ │
│  │                                 │ │
│  │        📷 Photo                 │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
│                                     │
│  Make: [Ford            ▼]          │
│  Model: [Focus          ▼]          │
│  Street: [High Street   ▼]          │
│  Contravention: [01 - Restricted ▼] │
│                                     │
│  ── Valve Positions (Optional) ──   │
│  Front: [12 ▼]    Rear: [6 ▼]      │
│                                     │
│           [SUBMIT]                  │
└─────────────────────────────────────┘
```

## Camera Implementation

### Camera Configuration
- **Resolution**: 1920x1080 minimum for plate recognition
- **Focus Mode**: Continuous auto-focus
- **Exposure**: Auto-exposure with bias adjustment
- **Stabilization**: Electronic image stabilization
- **Orientation**: Handle device rotation properly

### ANPR Integration
- **ML Kit Text Recognition**: Google ML Kit for on-device text detection
- **Processing Pipeline**:
  1. Capture frame from camera
  2. Apply image preprocessing (contrast, brightness)
  3. Run text recognition
  4. Filter results for UK plate patterns
  5. Validate and confirm detection

### Text Recognition Logic
```kotlin
// Pseudo-code for ANPR processing
fun processFrame(image: InputImage): ANPRResult {
    val textRecognizer = TextRecognition.getClient()
    return textRecognizer.process(image)
        .map { result -> 
            result.textBlocks
                .flatMap { it.lines }
                .map { it.text }
                .filter { isValidUKPlate(it) }
                .firstOrNull()
        }
}

fun isValidUKPlate(text: String): Boolean {
    // UK plate validation regex
    val ukPlatePattern = Regex("^[A-Z]{2}[0-9]{2}\\s?[A-Z]{3}$")
    return ukPlatePattern.matches(text.replace(" ", ""))
}
```

## Observation Timer

### Timer Implementation
- **Start Trigger**: Automatic when camera captures plate
- **Format**: MM:SS (e.g., 05:30)
- **Display**: Prominent position at top of screen
- **Persistence**: Continue running in background
- **Accuracy**: Update every second

### Timer States
- **Running**: Green background, normal display
- **Paused**: Yellow background (if user navigates away)
- **Completed**: Red background (observation time exceeded)

## Form Fields

### Mandatory Fields

#### VRM (Vehicle Registration Mark)
- **Source**: Auto-populated from ANPR
- **Validation**: UK plate format validation
- **Editing**: Allow manual correction if ANPR incorrect
- **Format**: Uppercase, space between groups (AB12 CDE)

#### Make (Vehicle Make)
- **Type**: Dropdown selection
- **Options**: Ford, Volkswagen, BMW, Mercedes, Toyota
- **Validation**: Must select from available options
- **Future**: Integration with DVLA lookup

#### Model (Vehicle Model)
- **Type**: Dropdown selection
- **Options**: Filtered by selected make
- **Validation**: Must select from available options
- **Dependency**: Updates based on make selection

#### Street
- **Type**: Dropdown selection
- **Options**: 10 predefined streets from database
- **Validation**: Must select from available options
- **Search**: Future enhancement for quick search

#### Contravention
- **Type**: Dropdown selection
- **Options**: 10 contraventions per street
- **Validation**: Must select from available options
- **Display**: Show code and description
- **Dependency**: Updates based on street selection

### Optional Fields

#### Valve Positions
- **Purpose**: Record wheel valve positions for verification
- **Layout**: Two dropdowns side by side
- **Labels**: "Front" and "Rear"
- **Options**: 1-12 representing clock positions
- **Default**: No selection (optional)
- **Visual**: Clock face reference diagram

### Clock Position Reference
```
        12
    11      1
10            2
9              3
8              4
    7        5
        6
```

## Validation and Error Handling

### Field Validation
- **Required Field Check**: All mandatory fields must be completed
- **VRM Format**: Validate UK registration format
- **Dropdown Selections**: Ensure valid options selected
- **Real-time Validation**: Show errors as user types/selects

### Error States
- **Missing VRM**: "Vehicle registration is required"
- **Invalid VRM**: "Please enter a valid UK registration"
- **Missing Make**: "Please select vehicle make"
- **Missing Model**: "Please select vehicle model"
- **Missing Street**: "Please select street"
- **Missing Contravention**: "Please select contravention"

### Camera Errors
- **Permission Denied**: Request camera permissions
- **Camera Unavailable**: Show error message and fallback
- **Focus Issues**: Provide manual focus assistance
- **Low Light**: Suggest using flash or better lighting

## Photo Capture and Storage

### Photo Requirements
- **Capture**: Take photo of vehicle and plate
- **Resolution**: High resolution for evidence
- **Metadata**: Include timestamp, location, observation ID
- **Storage**: Local storage with unique filename
- **Compression**: Balance quality and file size

### Photo Management
- **Filename**: `observation_[ID]_[timestamp].jpg`
- **Location**: App private directory
- **Backup**: Include in sync queue for upload
- **Cleanup**: Remove old photos after sync

## Observation Submission

### Submission Process
1. **Validation**: Check all mandatory fields completed
2. **Timer Check**: Record current observation time
3. **Photo Save**: Store captured photo
4. **Database Insert**: Save observation to local database
5. **Queue Addition**: Add to sync queue
6. **Navigation**: Return to dashboard or countdown screen

### Submission Feedback
- **Success**: Show confirmation message
- **Error**: Display specific error and allow retry
- **Loading**: Show progress indicator during save

## Background Behavior

### App Lifecycle
- **Background**: Timer continues running
- **Foreground**: Resume from where left off
- **Interruption**: Handle phone calls, notifications
- **Data Persistence**: Save state to prevent data loss

### Resource Management
- **Camera Release**: Properly release camera when not needed
- **Battery Optimization**: Minimize battery usage
- **Memory Management**: Clean up resources efficiently

## Integration with Other Features

### Countdown System
- **Automatic Entry**: Observation automatically added to countdown
- **Time Calculation**: Calculate remaining time based on contravention
- **Notification**: Alert when observation time complete

### Sync Queue
- **Queue Entry**: Add observation to sync queue
- **Upload Data**: Include photo and observation details
- **Status Tracking**: Track sync status and errors

## Accessibility Features

### Visual Accessibility
- **High Contrast**: Support for high contrast mode
- **Large Text**: Support for large text sizes
- **Color Blind**: Use patterns and text, not just colors
- **Screen Reader**: Proper content descriptions

### Interaction Accessibility
- **Voice Input**: Support for voice input on VRM field
- **Touch Targets**: Minimum 44dp touch targets
- **Keyboard Navigation**: Support for external keyboards
- **Switch Control**: Support for switch control navigation

## Performance Considerations

### Camera Performance
- **Frame Rate**: Optimize for smooth preview
- **Processing**: Efficient image processing
- **Memory**: Proper memory management
- **Battery**: Minimize battery drain

### Database Performance
- **Indexing**: Proper database indexes
- **Transactions**: Efficient database operations
- **Caching**: Cache frequently accessed data

## Testing Scenarios

### Happy Path
1. Open camera → Detect plate → Fill details → Submit
2. Verify timer starts immediately
3. Confirm all fields save correctly
4. Check photo capture and storage

### Error Scenarios
1. Camera permission denied
2. ANPR fails to detect plate
3. Invalid VRM format
4. Missing mandatory fields
5. Database save failure

### Edge Cases
1. Multiple plates in frame
2. Damaged or dirty plates
3. Low light conditions
4. Device rotation during capture
5. App interruption during observation