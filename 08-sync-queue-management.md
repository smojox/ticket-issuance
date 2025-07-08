# Sync and Queue Management System

## Overview
The sync and queue management system handles offline operation by storing all upload/download tasks in a local queue. This ensures the application can work completely offline while maintaining data integrity for future synchronization with central servers.

## Queue Architecture

### Queue Types
- **Upload Queue**: Data to be sent to server
- **Download Queue**: Data to be retrieved from server
- **Retry Queue**: Failed operations requiring retry
- **Priority Queue**: High-priority operations

### Operation Types
- **upload_ticket**: Upload issued tickets
- **upload_observation**: Upload observation data
- **upload_photo**: Upload photo evidence
- **download_streets**: Download street data
- **download_contraventions**: Download contravention data
- **download_vehicle_data**: Download make/model data
- **download_updates**: Download general updates

## Screen Layouts

### Queue Management Screen
```
┌─────────────────────────────────────┐
│  [←] SYNC QUEUE        [SYNC ALL]   │
│                                     │
│  Connection: ⚫ Offline              │
│  Last Sync: 2 hours ago             │
│  Pending: 7 items                   │
│                                     │
│  ── UPLOAD QUEUE ──                 │
│  ┌─────────────────────────────────┐ │
│  │ 📤 Ticket TMA20250708001        │ │
│  │    Status: Pending              │ │
│  │    Size: 2.3 MB                 │ │
│  │    [RETRY] [DELETE] [VIEW]      │ │
│  └─────────────────────────────────┘ │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │ 📤 Observation AB12CDE          │ │
│  │    Status: Failed (3 retries)   │ │
│  │    Error: Connection timeout    │ │
│  │    [RETRY] [DELETE] [VIEW]      │ │
│  └─────────────────────────────────┘ │
│                                     │
│  ── DOWNLOAD QUEUE ──               │
│  ┌─────────────────────────────────┐ │
│  │ 📥 Street Updates               │ │
│  │    Status: Pending              │ │
│  │    Priority: Low                │ │
│  │    [RETRY] [DELETE] [VIEW]      │ │
│  └─────────────────────────────────┘ │
│                                     │
│           [CLEAR ALL] [SETTINGS]    │
└─────────────────────────────────────┘
```

### Sync Status Screen
```
┌─────────────────────────────────────┐
│  [←] SYNC STATUS                    │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │         SYNC PROGRESS           │ │
│  │                                 │ │
│  │  Uploading: 3 of 5 items        │ │
│  │  ▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░  │ │
│  │  60% Complete                   │ │
│  │                                 │ │
│  │  Current: Ticket TMA20250708001 │ │
│  │  Status: Uploading photos...    │ │
│  │                                 │ │
│  │  Estimated: 2 minutes remaining │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
│                                     │
│  ── RECENT ACTIVITY ──              │
│  ✅ Ticket TMA20250708002 - Success │
│  ✅ Observation XY98ZAB - Success   │
│  ❌ Photo upload - Failed           │
│  ⏳ Ticket TMA20250708001 - Pending │
│                                     │
│          [PAUSE] [CANCEL]           │
└─────────────────────────────────────┘
```

## Data Structures

### Sync Queue Entry
```kotlin
data class SyncQueueEntry(
    val id: Long = 0,
    val operationType: OperationType,
    val tableName: String,
    val recordId: Long?,
    val dataJson: String,
    val status: SyncStatus,
    val priority: Priority,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val lastAttemptTime: LocalDateTime?,
    val errorMessage: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class OperationType {
    UPLOAD_TICKET,
    UPLOAD_OBSERVATION,
    UPLOAD_PHOTO,
    DOWNLOAD_STREETS,
    DOWNLOAD_CONTRAVENTIONS,
    DOWNLOAD_VEHICLE_DATA,
    DOWNLOAD_UPDATES
}

enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}
```

### Queue Manager
```kotlin
class SyncQueueManager {
    fun addToQueue(
        operationType: OperationType,
        tableName: String,
        recordId: Long?,
        data: Any,
        priority: Priority = Priority.MEDIUM
    ): Long
    
    fun processQueue(): Boolean
    fun retryFailedItems(): Int
    fun clearQueue(): Boolean
    fun getQueueStatus(): QueueStatus
}
```

## Queue Operations

### Adding Items to Queue
```kotlin
// When ticket is issued
fun onTicketIssued(ticket: Ticket) {
    val ticketJson = jsonSerializer.toJson(ticket)
    queueManager.addToQueue(
        operationType = OperationType.UPLOAD_TICKET,
        tableName = "tickets",
        recordId = ticket.id,
        data = ticketJson,
        priority = Priority.HIGH
    )
    
    // Add photos separately
    ticket.photos.forEach { photoPath ->
        queueManager.addToQueue(
            operationType = OperationType.UPLOAD_PHOTO,
            tableName = "photos",
            recordId = null,
            data = photoPath,
            priority = Priority.MEDIUM
        )
    }
}
```

### Processing Queue
```kotlin
class QueueProcessor {
    suspend fun processQueue() {
        val pendingItems = queueRepository.getPendingItems()
        
        pendingItems.sortedBy { it.priority }.forEach { item ->
            try {
                updateStatus(item.id, SyncStatus.IN_PROGRESS)
                
                when (item.operationType) {
                    OperationType.UPLOAD_TICKET -> uploadTicket(item)
                    OperationType.UPLOAD_OBSERVATION -> uploadObservation(item)
                    OperationType.UPLOAD_PHOTO -> uploadPhoto(item)
                    OperationType.DOWNLOAD_STREETS -> downloadStreets(item)
                    // ... other operations
                }
                
                updateStatus(item.id, SyncStatus.COMPLETED)
            } catch (e: Exception) {
                handleError(item, e)
            }
        }
    }
}
```

## Retry Logic

### Retry Strategy
- **Exponential Backoff**: Increasing delay between retries
- **Max Retries**: 3 attempts for most operations
- **Retry Conditions**: Network errors, timeouts, server errors
- **No Retry**: Authentication errors, invalid data

### Retry Implementation
```kotlin
class RetryManager {
    fun shouldRetry(error: Exception, retryCount: Int): Boolean {
        return when {
            retryCount >= MAX_RETRIES -> false
            error is AuthenticationException -> false
            error is ValidationException -> false
            error is NetworkException -> true
            error is TimeoutException -> true
            else -> true
        }
    }
    
    fun getRetryDelay(retryCount: Int): Long {
        return (1000 * (2.0.pow(retryCount))).toLong() // Exponential backoff
    }
}
```

## Network Management

### Connection Monitoring
```kotlin
class NetworkManager {
    private val connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    
    fun startMonitoring() {
        // Monitor network connectivity
        // Update connectionState accordingly
    }
    
    fun isConnected(): Boolean {
        return connectionState.value == ConnectionState.CONNECTED
    }
    
    fun getConnectionType(): ConnectionType {
        // Return WiFi, Mobile, or None
    }
}
```

### Sync Conditions
- **WiFi Only**: Option to sync only on WiFi
- **Battery Level**: Minimum battery level for sync
- **Data Limits**: Respect data usage limits
- **Background Sync**: Automatic background sync when conditions met

## Manual Sync Operations

### Sync All
- **Trigger**: User taps "SYNC ALL" button
- **Process**: Process all pending queue items
- **Feedback**: Show progress and status
- **Cancellation**: Allow user to cancel sync

### Individual Sync
- **Trigger**: User taps "RETRY" on specific item
- **Process**: Retry single queue item
- **Feedback**: Show item-specific progress
- **Error Handling**: Display specific error messages

### Selective Sync
- **Uploads First**: Prioritize uploads over downloads
- **Critical Data**: Sync tickets before observations
- **Size Limits**: Sync small items before large photos

## Error Handling

### Error Types
- **Network Errors**: Connection failures, timeouts
- **Server Errors**: 5xx HTTP errors
- **Authentication**: Invalid credentials, expired tokens
- **Data Errors**: Invalid data format, missing fields
- **Storage Errors**: Disk full, permission denied

### Error Display
```kotlin
sealed class SyncError {
    data class NetworkError(val message: String) : SyncError()
    data class ServerError(val code: Int, val message: String) : SyncError()
    data class AuthenticationError(val message: String) : SyncError()
    data class DataError(val field: String, val message: String) : SyncError()
    data class StorageError(val message: String) : SyncError()
}
```

### Error Recovery
- **Automatic Retry**: For temporary network issues
- **Manual Retry**: For persistent issues
- **Data Correction**: For data validation errors
- **Skip and Continue**: For non-critical errors

## Background Sync

### Sync Service
```kotlin
class SyncService : JobIntentService() {
    override fun onHandleWork(intent: Intent) {
        if (canSync()) {
            syncQueueManager.processQueue()
        }
    }
    
    private fun canSync(): Boolean {
        return networkManager.isConnected() &&
               batteryManager.getBatteryLevel() > 20 &&
               !isDataLimited()
    }
}
```

### Sync Scheduling
- **Periodic**: Every 30 minutes when conditions allow
- **Immediate**: When network becomes available
- **User Triggered**: Manual sync requests
- **Constraint Based**: Only when WiFi available (optional)

## Data Compression

### Compression Strategy
- **JSON**: Gzip compression for JSON data
- **Photos**: JPEG compression with quality settings
- **Batch**: Group small items together
- **Incremental**: Only sync changed data

### Compression Implementation
```kotlin
class DataCompressor {
    fun compressJson(data: String): ByteArray {
        return GZIPOutputStream(ByteArrayOutputStream()).use { gzip ->
            gzip.write(data.toByteArray())
            gzip.finish()
            gzip.toByteArray()
        }
    }
    
    fun compressPhoto(photo: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { stream ->
            photo.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.toByteArray()
        }
    }
}
```

## Storage Management

### Queue Storage
- **Database**: SQLite table for queue entries
- **File System**: Temporary files for large data
- **Cleanup**: Remove completed items after retention period
- **Size Limits**: Maximum queue size and item size

### Storage Optimization
```kotlin
class StorageManager {
    fun cleanupCompletedItems() {
        val cutoff = LocalDateTime.now().minusDays(7)
        queueRepository.deleteCompletedBefore(cutoff)
    }
    
    fun getStorageUsage(): StorageInfo {
        return StorageInfo(
            totalSize = calculateTotalSize(),
            queueSize = calculateQueueSize(),
            photoSize = calculatePhotoSize(),
            availableSpace = getAvailableSpace()
        )
    }
}
```

## Performance Optimization

### Batch Operations
- **Group Similar**: Batch similar operations together
- **Size Limits**: Limit batch size for memory efficiency
- **Parallel**: Process independent items in parallel
- **Streaming**: Stream large data instead of loading in memory

### Memory Management
- **Lazy Loading**: Load queue items on demand
- **Pagination**: Paginate large queue lists
- **Cleanup**: Clean up resources after operations
- **Monitoring**: Monitor memory usage during sync

## Testing Scenarios

### Queue Testing
1. **Add Items**: Verify items added to queue correctly
2. **Process Items**: Test queue processing logic
3. **Retry Logic**: Test retry mechanisms
4. **Error Handling**: Test various error scenarios
5. **Storage**: Test queue storage and retrieval

### Sync Testing
1. **Network Conditions**: Test various network states
2. **Interruption**: Test sync interruption and resume
3. **Large Data**: Test sync with large photos/data
4. **Concurrent**: Test multiple sync operations
5. **Background**: Test background sync behavior

### Error Testing
1. **Network Errors**: Test network failure scenarios
2. **Server Errors**: Test server error responses
3. **Data Errors**: Test invalid data handling
4. **Storage Errors**: Test storage full scenarios
5. **Authentication**: Test authentication failures

## Security Considerations

### Data Security
- **Encryption**: Encrypt sensitive data in queue
- **Authentication**: Secure API authentication
- **Transmission**: Use HTTPS for all communications
- **Storage**: Secure local storage of queue data

### Privacy Protection
- **Data Minimization**: Only sync necessary data
- **Retention**: Limit data retention periods
- **Anonymization**: Anonymize sensitive data where possible
- **Consent**: Respect user privacy settings