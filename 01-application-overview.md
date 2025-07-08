# CEO Ticket Issuance Android Application - Overview

## Project Summary
Modern Android application for CEO (Civil Enforcement Officer) ticket issuance based on TMA 2004 (Traffic Management Act 2004) requirements. The application operates primarily offline with sync capabilities for future API integration.

## Core Requirements
- **Offline-first architecture** - Full functionality without internet connection
- **Local data storage** - SQLite database for all application data
- **ANPR integration** - Automatic Number Plate Recognition via camera
- **Time-based observations** - Countdown system for different contravention types
- **Sync capabilities** - Queue system for future server integration
- **TMA 2004 compliance** - Adherence to Traffic Management Act 2004 standards

## Technology Stack
- **Language**: Kotlin
- **Platform**: Android (minimum SDK to be determined)
- **Database**: SQLite with Room persistence library
- **Camera**: CameraX with ML Kit for text recognition (ANPR)
- **Architecture**: MVVM with LiveData/StateFlow
- **Dependencies**: 
  - Room (local database)
  - CameraX (camera functionality)
  - ML Kit (text recognition for ANPR)
  - Jetpack Compose (modern UI)
  - Coroutines (asynchronous operations)
  - Hilt (dependency injection)

## Key Features

### Authentication
- Simple login with hardcoded credentials (Test/Test)
- Session management for continuous use

### Main Dashboard
- **Observation Button** - Start new parking observation
- **Issuance Button** - Create tickets directly
- **Countdowns Button** - View active observation timers
- **Queue Button** - View pending sync operations
- **Sync Button** - Manual data synchronization

### Observation Flow
- Instant camera activation with ANPR
- Real-time observation timer
- Mandatory fields: Street, Contravention, Make, Model
- Optional valve positions (Front/Rear with 1-12 clock positions)
- Auto-calculation of remaining observation time

### Ticket Issuance
- Pre-populated from observations or manual entry
- TMA 2004 compliant ticket format
- Local storage with sync queue integration

### Data Management
- 10 streets with 10 contraventions each (dummy data)
- 5 vehicle makes and models for testing
- Sync queue for upload/download operations
- Offline operation with connection status awareness

## Application Flow
1. Login with Test/Test credentials
2. Main dashboard with navigation options
3. Observation: Camera → ANPR → Timer → Details → Submit
4. Countdown: Monitor observation timers
5. Issuance: Create tickets when observation time elapsed
6. Queue: View pending operations
7. Sync: Manual data synchronization

## Future Considerations
- External API integration for data updates
- Advanced ANPR with make/model recognition
- Real-time server synchronization
- Enhanced reporting and analytics
- Multi-user support
- Biometric authentication