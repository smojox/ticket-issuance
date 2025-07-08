# Countdown System for Observations

## Overview
The countdown system manages time-based observations, calculating when tickets can be issued based on contravention requirements. It tracks elapsed observation time and provides real-time updates on remaining time before issuance eligibility.

## Countdown Logic

### Time Calculation
- **Observation Start**: When observation is submitted
- **Elapsed Time**: Time already spent during observation entry
- **Required Time**: Based on contravention type (from database)
- **Remaining Time**: Required time minus elapsed time

### Example Calculation
```
Contravention 01: Requires 5 minutes observation
Observation started: 14:30:00
Observation submitted: 14:31:00 (1 minute elapsed)
Remaining time: 5 minutes - 1 minute = 4 minutes
Ticket eligible at: 14:35:00
```

## Screen Layout

### Countdown List View
```
┌─────────────────────────────────────┐
│  [←] COUNTDOWNS           [🔄]      │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │ AB12 CDE     High Street        │ │
│  │ 01 - Restricted Hours           │ │
│  │                                 │ │
│  │        03:42                    │ │
│  │     REMAINING                   │ │
│  │                                 │ │
│  │ Started: 14:30  [VIEW] [ISSUE]  │ │
│  └─────────────────────────────────┘ │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │ XY98 ZAB     Market Square      │ │
│  │ 30 - Overstay Time Limit        │ │
│  │                                 │ │
│  │        READY                    │ │
│  │      TO ISSUE                   │ │
│  │                                 │ │
│  │ Started: 14:15  [VIEW] [ISSUE]  │ │
│  └─────────────────────────────────┘ │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │ CD34 EFG     Church Lane        │ │
│  │ 25 - Loading Bay Misuse         │ │
│  │                                 │ │
│  │        01:15                    │ │
│  │     REMAINING                   │ │
│  │                                 │ │
│  │ Started: 14:32  [VIEW] [ISSUE]  │ │
│  └─────────────────────────────────┘ │
│                                     │
│              No more items          │
└─────────────────────────────────────┘
```

### Individual Countdown Card
```
┌─────────────────────────────────────┐
│  VRM: AB12 CDE                      │
│  Street: High Street                │
│  Contravention: 01 - Restricted     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│  Required: 5 min    Elapsed: 1 min  │
│  Started: 14:30:00                  │
│  Eligible: 14:35:00                 │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│                                     │
│            03:42                    │
│          REMAINING                  │
│                                     │
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░   │
│  Progress: 78%                      │
│                                     │
│    [VIEW DETAILS]  [ISSUE TICKET]   │
└─────────────────────────────────────┘
```

## Countdown States

### 1. Active Countdown
- **Status**: Time remaining before issuance eligibility
- **Display**: MM:SS format (e.g., 03:42)
- **Color**: Orange/Yellow background
- **Progress**: Visual progress bar showing elapsed time
- **Actions**: View details, cannot issue ticket yet

### 2. Ready to Issue
- **Status**: Required observation time has elapsed
- **Display**: "READY TO ISSUE" message
- **Color**: Green background
- **Notification**: Alert/notification when first eligible
- **Actions**: View details, issue ticket

### 3. Expired/Overdue
- **Status**: Significant time passed since eligibility
- **Display**: "OVERDUE" or time since eligible
- **Color**: Red background
- **Actions**: View details, issue ticket with note

## Time Management

### Contravention Time Requirements
```kotlin
// Example time requirements by contravention code
val contraventionTimes = mapOf(
    "01" to 5,   // 5 minutes - Restricted street
    "02" to 5,   // 5 minutes - Loading restriction
    "06" to 0,   // 0 minutes - No valid ticket
    "07" to 0,   // 0 minutes - Overstay
    "12" to 0,   // 0 minutes - No permit
    "16" to 0,   // 0 minutes - Invalid permit
    "19" to 0,   // 0 minutes - Invalid permit display
    "23" to 0,   // 0 minutes - Wrong vehicle class
    "25" to 5,   // 5 minutes - Loading bay misuse
    "30" to 10   // 10 minutes - Overstay time limit
)
```

### Timer Updates
- **Frequency**: Update every second
- **Accuracy**: Millisecond precision for calculations
- **Display**: Show only minutes and seconds
- **Persistence**: Continue running in background

## Real-time Updates

### Background Processing
- **Service**: Foreground service for timer updates
- **Notifications**: Show progress in notification bar
- **Wake Lock**: Prevent device sleep affecting timers
- **Battery**: Optimize for minimal battery usage

### UI Updates
- **Live Updates**: Real-time countdown display
- **State Changes**: Immediate visual feedback
- **Badge Updates**: Update dashboard badge count
- **Auto-refresh**: Refresh on screen focus

## Notifications

### Eligibility Notifications
- **Trigger**: When countdown reaches zero
- **Content**: "VRM [plate] ready for ticket issuance"
- **Action**: Tap to open countdown screen
- **Sound**: Subtle notification sound
- **Vibration**: Brief vibration pattern

### Reminder Notifications
- **Trigger**: 30 minutes after eligibility
- **Content**: "Overdue: VRM [plate] - issue ticket"
- **Action**: Tap to issue ticket directly
- **Priority**: Higher priority for overdue items

## Data Management

### Countdown Creation
```kotlin
// When observation is submitted
fun createCountdown(observation: Observation) {
    val contravention = getContravention(observation.contraventionId)
    val requiredMinutes = contravention.observationTimeMinutes
    val elapsedMinutes = observation.getElapsedMinutes()
    val remainingMinutes = maxOf(0, requiredMinutes - elapsedMinutes)
    
    val countdown = Countdown(
        observationId = observation.id,
        vrm = observation.vrm,
        startTime = observation.observationStartTime,
        requiredMinutes = requiredMinutes,
        elapsedMinutes = elapsedMinutes,
        remainingMinutes = remainingMinutes,
        eligibleTime = observation.observationStartTime.plus(requiredMinutes.minutes),
        status = if (remainingMinutes > 0) CountdownStatus.ACTIVE else CountdownStatus.READY
    )
    
    saveCountdown(countdown)
}
```

### Countdown Updates
- **Real-time**: Update remaining time every second
- **State Changes**: Update status when eligible
- **Persistence**: Save state to database
- **Cleanup**: Remove after ticket issued

## User Interactions

### View Details
- **Action**: Show full observation details
- **Content**: VRM, street, contravention, photos, times
- **Navigation**: Modal or detail screen
- **Return**: Back to countdown list

### Issue Ticket
- **Enabled**: Only when countdown eligible
- **Action**: Navigate to ticket issuance screen
- **Pre-populate**: Fill form with observation data
- **Cleanup**: Remove from countdown list after issuance

### Refresh
- **Manual**: Pull-to-refresh gesture
- **Auto**: Refresh on screen focus
- **Background**: Update timers continuously

## Error Handling

### Timer Errors
- **System Clock**: Handle system time changes
- **Background Limits**: Handle Android background restrictions
- **Memory**: Handle low memory situations
- **Database**: Handle database errors gracefully

### Display Errors
- **Invalid Data**: Show error message for corrupt data
- **Missing Data**: Handle missing observations
- **Sync Errors**: Show sync status in UI

## Performance Optimization

### Efficient Updates
- **Batch Updates**: Group database updates
- **Selective Updates**: Only update changed items
- **View Recycling**: Efficient RecyclerView usage
- **Memory Management**: Proper cleanup of resources

### Background Efficiency
- **Doze Mode**: Handle Android Doze mode
- **Battery Optimization**: Minimize battery usage
- **CPU Usage**: Efficient timer calculations
- **Network**: Minimal network usage

## Integration with Other Features

### Dashboard Integration
- **Badge Count**: Show active countdown count
- **Status Updates**: Real-time badge updates
- **Navigation**: Direct access from dashboard

### Ticket Issuance
- **Pre-population**: Auto-fill ticket forms
- **Data Validation**: Ensure data consistency
- **Cleanup**: Remove countdown after ticket issued

### Sync System
- **Queue Updates**: Update sync queue on changes
- **Status Tracking**: Track sync status
- **Error Handling**: Handle sync errors

## Accessibility Features

### Visual Accessibility
- **High Contrast**: Support high contrast mode
- **Large Text**: Support large text sizes
- **Color Blind**: Use text and patterns, not just colors
- **Screen Reader**: Proper content descriptions

### Audio Accessibility
- **Voice Announcements**: Announce time updates
- **Sound Alerts**: Audio notifications for eligibility
- **Vibration**: Tactile feedback for alerts

## Testing Scenarios

### Timer Testing
1. **Normal Countdown**: Verify timer counts down correctly
2. **Immediate Eligibility**: Test contraventions with 0 minutes
3. **Background Continuation**: Verify timer continues in background
4. **System Time Changes**: Handle clock adjustments
5. **App Restart**: Verify timer persistence

### State Testing
1. **Active to Ready**: Verify state transition
2. **Overdue Handling**: Test overdue notifications
3. **Multiple Countdowns**: Handle multiple simultaneous timers
4. **Cleanup**: Verify cleanup after ticket issuance

### Error Testing
1. **Missing Data**: Handle corrupt observations
2. **Database Errors**: Handle database failures
3. **Background Restrictions**: Handle Android limitations
4. **Memory Issues**: Handle low memory situations