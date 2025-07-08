# Ticket Issuance Flow

## Overview
The ticket issuance flow handles the creation of parking tickets either from completed observations or as direct manual entries. It ensures TMA 2004 compliance and generates proper ticket documentation with all required fields.

## Entry Points

### 1. From Countdown Screen
- **Trigger**: User taps "ISSUE TICKET" on eligible countdown
- **Pre-population**: All observation data automatically filled
- **Validation**: Observation time requirements already met
- **Flow**: Skip to ticket details confirmation

### 2. From Dashboard (Direct Issuance)
- **Trigger**: User taps "ISSUANCE" button from main dashboard
- **Pre-population**: Empty form requiring manual entry
- **Validation**: All fields must be manually validated
- **Flow**: Complete form entry process

### 3. From Observation Flow
- **Trigger**: Observation countdown reaches zero
- **Pre-population**: Observation data pre-filled
- **Validation**: Time requirements automatically validated
- **Flow**: Quick ticket generation

## Screen Layouts

### Ticket Form Screen
```
┌─────────────────────────────────────┐
│  [←] ISSUE TICKET          [SAVE]   │
│                                     │
│  VRM: [AB12 CDE            ]        │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │                                 │ │
│  │        📷 Vehicle Photo         │ │
│  │      [CAMERA] [GALLERY]        │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
│                                     │
│  Make: [Ford            ▼]          │
│  Model: [Focus          ▼]          │
│  Color: [Blue           ▼]          │
│  Street: [High Street   ▼]          │
│  Contravention: [01 - Restricted ▼] │
│                                     │
│  ── Valve Positions (Optional) ──   │
│  Front: [12 ▼]    Rear: [6 ▼]      │
│                                     │
│  ── Observation Details ──          │
│  Start Time: 14:30:00               │
│  Duration: 05:30                    │
│  Issue Time: 14:35:30               │
│                                     │
│  ── Penalty Information ──          │
│  Amount: £70.00                     │
│  Discount: £35.00 (if paid early)   │
│                                     │
│  Notes: [Optional notes...    ]     │
│                                     │
│        [PREVIEW] [ISSUE TICKET]     │
└─────────────────────────────────────┘
```

### Ticket Preview Screen
```
┌─────────────────────────────────────┐
│  [←] PREVIEW TICKET                 │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │        PENALTY NOTICE           │ │
│  │                                 │ │
│  │  Ticket No: TMA20250708001      │ │
│  │  Date: 08/07/2025 14:35        │ │
│  │                                 │ │
│  │  VRM: AB12 CDE                  │ │
│  │  Make: Ford    Model: Focus     │ │
│  │  Color: Blue                    │ │
│  │                                 │ │
│  │  Location: High Street          │ │
│  │                                 │ │
│  │  Contravention:                 │ │
│  │  01 - Parked in restricted      │ │
│  │  street during prescribed hours │ │
│  │                                 │ │
│  │  Penalty: £70.00                │ │
│  │  Early Payment: £35.00          │ │
│  │                                 │ │
│  │  Officer: Test Officer          │ │
│  │  Badge: CEO001                  │ │
│  │                                 │ │
│  │  [Vehicle Photo]                │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
│                                     │
│         [EDIT] [CONFIRM ISSUE]      │
└─────────────────────────────────────┘
```

## Ticket Number Generation

### Format Structure
- **Prefix**: TMA (Traffic Management Act)
- **Date**: YYYYMMDD format
- **Sequence**: 001-999 daily sequence
- **Example**: TMA20250708001

### Generation Logic
```kotlin
fun generateTicketNumber(): String {
    val today = LocalDate.now()
    val dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val sequence = getNextSequenceNumber(today)
    return "TMA${dateStr}${sequence.toString().padStart(3, '0')}"
}

fun getNextSequenceNumber(date: LocalDate): Int {
    val todayTickets = ticketRepository.getTicketsByDate(date)
    return todayTickets.size + 1
}
```

## Form Validation

### Mandatory Fields
- **VRM**: UK registration format validation
- **Make**: Must select from dropdown
- **Model**: Must select from dropdown (filtered by make)
- **Color**: Must select from dropdown
- **Street**: Must select from dropdown
- **Contravention**: Must select from dropdown
- **Photo**: Vehicle photo required
- **Issue Time**: Auto-generated but editable

### Optional Fields
- **Valve Positions**: Front and rear positions
- **Notes**: Additional officer notes
- **Observation Details**: Auto-populated from observation

### Validation Rules
```kotlin
data class TicketValidation(
    val vrm: ValidationResult,
    val make: ValidationResult,
    val model: ValidationResult,
    val color: ValidationResult,
    val street: ValidationResult,
    val contravention: ValidationResult,
    val photo: ValidationResult
)

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}
```

## Data Pre-population

### From Observation
- **VRM**: Observation VRM
- **Make/Model**: Observation make/model
- **Street**: Observation street
- **Contravention**: Observation contravention
- **Valve Positions**: Observation valve positions
- **Photo**: Observation photo
- **Times**: Observation start/end times

### From Manual Entry
- **All Fields**: Empty, require manual input
- **Issue Time**: Current timestamp
- **Officer**: Current logged-in user
- **Date**: Current date

## Photo Management

### Photo Sources
- **Camera**: Take new photo of vehicle
- **Gallery**: Select existing photo
- **Observation**: Use photo from observation
- **Multiple**: Support multiple evidence photos

### Photo Requirements
- **Resolution**: Minimum 1920x1080
- **Format**: JPEG with compression
- **Metadata**: Timestamp, location, officer
- **Storage**: Local storage with backup to sync queue

### Photo Validation
- **Size**: Maximum 5MB per photo
- **Format**: JPEG, PNG supported
- **Content**: Validate photo shows vehicle
- **Quantity**: At least 1 photo required

## Penalty Calculation

### Base Penalty
- **Source**: Contravention table penalty_amount
- **Standard**: £70.00 for most contraventions
- **Variable**: Some contraventions may have different amounts

### Early Payment Discount
- **Calculation**: 50% of base penalty
- **Time Limit**: 14 days from issue
- **Display**: Show both full and discounted amounts

### Penalty Display
```kotlin
data class PenaltyInfo(
    val fullAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val discountDays: Int = 14
)

fun calculatePenalty(contravention: Contravention): PenaltyInfo {
    val fullAmount = contravention.penaltyAmount
    val discountAmount = fullAmount.multiply(BigDecimal("0.5"))
    return PenaltyInfo(fullAmount, discountAmount)
}
```

## Ticket Generation

### Ticket Data Structure
```kotlin
data class Ticket(
    val id: Long = 0,
    val ticketNumber: String,
    val vrm: String,
    val make: String,
    val model: String,
    val color: String,
    val street: String,
    val contravention: String,
    val contraventionCode: String,
    val penaltyAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val issueTime: LocalDateTime,
    val observationStartTime: LocalDateTime?,
    val observationDuration: Duration?,
    val valvePositionFront: Int?,
    val valvePositionRear: Int?,
    val photos: List<String>,
    val notes: String?,
    val officerName: String,
    val officerBadge: String,
    val status: TicketStatus = TicketStatus.ISSUED
)
```

### Issue Process
1. **Validation**: Validate all form fields
2. **Photo Processing**: Compress and store photos
3. **Ticket Number**: Generate unique ticket number
4. **Database Save**: Save ticket to local database
5. **Sync Queue**: Add to sync queue for upload
6. **Cleanup**: Remove from countdown if applicable
7. **Confirmation**: Show success message

## Print/Export Options

### Digital Ticket
- **PDF Generation**: Generate PDF version
- **Email**: Send ticket via email
- **Share**: Share ticket via other apps
- **Storage**: Save to device storage

### Print Options
- **Bluetooth Printer**: Connect to portable printer
- **Network Printer**: Print via WiFi
- **Print Preview**: Show print preview
- **Print Settings**: Configure print options

## Error Handling

### Validation Errors
- **Field Errors**: Show specific field errors
- **Form Errors**: Show general form errors
- **Real-time**: Validate as user types/selects
- **Summary**: Show all errors at once

### System Errors
- **Database**: Handle database save errors
- **Photo**: Handle photo capture/processing errors
- **Storage**: Handle storage full errors
- **Network**: Handle sync queue errors

### User Feedback
- **Success**: Show success confirmation
- **Error**: Show error messages with retry options
- **Loading**: Show progress during processing
- **Validation**: Real-time validation feedback

## Integration with Other Features

### Countdown System
- **Cleanup**: Remove countdown after ticket issued
- **Validation**: Ensure observation time met
- **Data Flow**: Seamless data transfer

### Sync System
- **Queue Addition**: Add ticket to sync queue
- **Status Tracking**: Track sync status
- **Error Handling**: Handle sync errors

### Dashboard
- **Badge Updates**: Update dashboard badges
- **Navigation**: Return to dashboard after issue
- **Status Display**: Show recent ticket status

## Accessibility Features

### Visual Accessibility
- **High Contrast**: Support high contrast mode
- **Large Text**: Support large text sizes
- **Color Blind**: Use text and patterns
- **Screen Reader**: Proper content descriptions

### Interaction Accessibility
- **Voice Input**: Support voice input for notes
- **Touch Targets**: Minimum 44dp touch targets
- **Keyboard Navigation**: Support external keyboards
- **Switch Control**: Support switch control

## Performance Considerations

### Form Performance
- **Lazy Loading**: Load dropdown data efficiently
- **Caching**: Cache frequently used data
- **Validation**: Efficient validation logic
- **Memory**: Proper memory management

### Photo Performance
- **Compression**: Efficient photo compression
- **Caching**: Cache compressed photos
- **Background**: Process photos in background
- **Memory**: Proper bitmap management

## Testing Scenarios

### Happy Path
1. **From Observation**: Complete ticket from observation
2. **Manual Entry**: Create ticket manually
3. **Photo Capture**: Take and attach photos
4. **Validation**: All fields validate correctly
5. **Generation**: Ticket generates successfully

### Error Scenarios
1. **Missing Fields**: Handle missing mandatory fields
2. **Invalid Data**: Handle invalid field data
3. **Photo Errors**: Handle photo capture errors
4. **Database Errors**: Handle save failures
5. **Sync Errors**: Handle sync queue errors

### Edge Cases
1. **Duplicate Tickets**: Prevent duplicate ticket numbers
2. **Concurrent Issues**: Handle multiple simultaneous tickets
3. **System Time**: Handle time zone issues
4. **Storage Full**: Handle storage limitations
5. **Battery Low**: Handle low battery during issue