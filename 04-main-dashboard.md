# Main Dashboard Design

## Overview
The main dashboard serves as the central navigation hub after successful login. It provides quick access to all primary application functions with a clean, intuitive interface optimized for field use.

## Dashboard Layout

### Screen Design
```
┌─────────────────────────────────────┐
│  ☰  CEO TICKETS        [Sync] 🔄   │
│                                     │
│     Welcome, Test Officer           │
│     Today: 08/07/2025 14:32         │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │                                 │ │
│  │        OBSERVATION              │ │
│  │      Start New Watch            │ │
│  │           📷                    │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │                                 │ │
│  │        ISSUANCE                 │ │
│  │      Create New Ticket          │ │
│  │           🎫                    │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
│                                     │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ │
│  │COUNTDOWN│ │  QUEUE  │ │  SYNC   │ │
│  │   (2)   │ │   (5)   │ │   📶    │ │
│  │   ⏱️    │ │   📋    │ │   🔄    │ │
│  └─────────┘ └─────────┘ └─────────┘ │
│                                     │
│            [ LOGOUT ]               │
└─────────────────────────────────────┘
```

## Navigation Elements

### 1. Header Section
- **App Title**: "CEO TICKETS"
- **Sync Button**: Real-time sync status and manual sync trigger
- **Menu Icon**: Future expansion for settings/preferences
- **User Greeting**: "Welcome, [Username]"
- **Date/Time**: Current date and time display

### 2. Primary Action Buttons

#### Observation Button
- **Purpose**: Start new parking observation
- **Visual**: Large button with camera icon
- **Label**: "OBSERVATION - Start New Watch"
- **Action**: Navigate to camera/ANPR screen
- **Priority**: Primary action (largest button)

#### Issuance Button
- **Purpose**: Create ticket directly (without observation)
- **Visual**: Large button with ticket icon
- **Label**: "ISSUANCE - Create New Ticket"
- **Action**: Navigate to ticket creation screen
- **Priority**: Primary action (same size as Observation)

### 3. Secondary Action Buttons

#### Countdown Button
- **Purpose**: View active observation timers
- **Visual**: Smaller button with timer icon
- **Label**: "COUNTDOWN"
- **Badge**: Number of active countdowns
- **Action**: Navigate to countdown management screen

#### Queue Button
- **Purpose**: View pending sync operations
- **Visual**: Smaller button with list icon
- **Label**: "QUEUE"
- **Badge**: Number of pending operations
- **Action**: Navigate to sync queue screen

#### Sync Button
- **Purpose**: Manual data synchronization
- **Visual**: Smaller button with sync icon
- **Label**: "SYNC"
- **Status**: Connection status indicator
- **Action**: Trigger sync operations

### 4. Footer Section
- **Logout Button**: Secure logout functionality
- **Version Info**: App version and build number (subtle)

## Button States and Indicators

### Observation Button
- **Default**: Blue background, white text
- **Disabled**: Gray background (if camera unavailable)
- **Active**: Pulsing animation if observation in progress

### Issuance Button
- **Default**: Green background, white text
- **Disabled**: Gray background (if required data missing)

### Countdown Button
- **Default**: Orange background, white text
- **Badge**: Red circle with count of active countdowns
- **Urgent**: Red background if any countdown expired

### Queue Button
- **Default**: Purple background, white text
- **Badge**: Blue circle with count of pending items
- **Error**: Red background if sync failures exist

### Sync Button
- **Connected**: Green background with checkmark
- **Disconnected**: Red background with X
- **Syncing**: Blue background with spinning icon
- **Error**: Orange background with warning icon

## Screen Behavior

### On Load
1. Display welcome message with current user
2. Update date/time display
3. Check sync status and update indicators
4. Load badge counts for Countdown and Queue buttons
5. Verify camera permissions for Observation button

### Refresh Logic
- **Auto-refresh**: Update badges and status every 30 seconds
- **Manual refresh**: Pull-to-refresh gesture
- **Background refresh**: Update when returning from other screens

### Navigation Flow
```
Dashboard → Observation → Camera/ANPR → Observation Details → Back to Dashboard
Dashboard → Issuance → Ticket Form → Back to Dashboard
Dashboard → Countdown → Active Timers → Back to Dashboard
Dashboard → Queue → Sync Operations → Back to Dashboard
Dashboard → Sync → Sync Status → Back to Dashboard
```

## Responsive Design

### Portrait Mode (Primary)
- Vertical layout as shown above
- Large buttons for easy touch access
- Optimal for single-handed use

### Landscape Mode
- Horizontal layout with buttons arranged in grid
- Maintain relative sizes and importance
- Ensure all elements remain accessible

## Data Display

### Real-time Updates
- **Active Observations**: Show count and status
- **Pending Tickets**: Show count in queue
- **Sync Status**: Real-time connection status
- **Time Display**: Update every minute

### Status Indicators
- **Connectivity**: WiFi/Mobile data status
- **Battery**: Low battery warning
- **Storage**: Low storage warning
- **Permissions**: Camera/location permission status

## Error Handling

### Common Error States
- **No Camera Permission**: Disable Observation button with message
- **No Network**: Show offline mode indicator
- **Database Error**: Show error message with retry option
- **Low Storage**: Warning message with storage management options

### Error Display
- **Toast Messages**: For temporary notifications
- **Inline Messages**: For persistent issues
- **Error Dialogs**: For critical errors requiring user action

## Accessibility Features

### Visual Accessibility
- High contrast mode support
- Large text support
- Color blind friendly color scheme
- Clear visual hierarchy

### Interaction Accessibility
- Touch target minimum 44dp
- Voice control support
- Screen reader compatibility
- Keyboard navigation support

## Performance Considerations

### Optimization
- Lazy loading of badge counts
- Efficient database queries
- Minimal network calls
- Smooth animations and transitions

### Memory Management
- Proper lifecycle management
- Release resources when not needed
- Background task optimization

## Jetpack Compose Implementation Structure

### DashboardScreen Composable
```kotlin
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToObservation: () -> Unit,
    onNavigateToIssuance: () -> Unit,
    onNavigateToCountdown: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToSync: () -> Unit,
    onLogout: () -> Unit
)
```

### DashboardViewModel
```kotlin
class DashboardViewModel : ViewModel() {
    val countdownCount = MutableStateFlow(0)
    val queueCount = MutableStateFlow(0)
    val syncStatus = MutableStateFlow(SyncStatus.Disconnected)
    val currentTime = MutableStateFlow(System.currentTimeMillis())
}
```

### State Management
- Use StateFlow for reactive updates
- Proper error handling with sealed classes
- Efficient state composition for UI updates