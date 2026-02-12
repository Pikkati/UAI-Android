# Android Client — Build & Packaging

This folder contains the UAI Android mobile client, built with Kivy for cross-platform Python development.

## Features

- **Real-time Data Synchronization**: Automatic sync with UAI platform
- **Offline AI Capabilities**: Local AI processing when disconnected
- **Cross-platform Data Sync**: Seamless data synchronization
- **Mobile-optimized Interface**: Touch-friendly UI for mobile devices
- **Session Management**: User authentication and session handling
- **AI Features**: Predictive analytics and ML training capabilities

## Files

- `mobile_client.py` — Main Kivy application with mobile interface
- `buildozer.spec` — Buildozer configuration for Android packaging
- `build-android.ps1` — PowerShell script to build Android APK
- `assemble-android.ps1` — PowerShell script to package artifacts

## Prerequisites

1. **Python 3.8+** with pip
2. **Kivy** framework: `pip install kivy`
3. **Buildozer** for Android packaging: `pip install buildozer`
4. **Android SDK/NDK** (Buildozer will download automatically)

## Quick Start

### Development (Desktop)

```bash
# Install dependencies
pip install kivy requests

# Run the app
python mobile_client.py
```

### Android Build

```powershell
# From repository root
cd UAI-Android/Android_Client

# Build Android APK
.\build-android.ps1 -Configuration Release

# Package artifacts
.\assemble-android.ps1 -Configuration Release -OutputPath ..\artifacts
```

## Buildozer Configuration

The `buildozer.spec` file contains the build configuration:

- **Title**: UAI Android Client
- **Package**: uai_android_client
- **Requirements**: python3,kivy,requests
- **Permissions**: INTERNET, ACCESS_NETWORK_STATE
- **Orientation**: Portrait
- **API Levels**: Min 21, Target 31

## CI/CD Integration

The build scripts are designed for CI/CD pipelines:

- GitHub Actions workflow included for automated builds
- Supports both debug and release configurations
- Automatic artifact collection and storage

## Architecture

The mobile client provides:

1. **Connection Monitoring**: Real-time server connectivity status
2. **User Sessions**: Mobile-optimized authentication
3. **AI Integration**: Access to platform AI features
4. **Data Sync**: Bidirectional synchronization with cloud
5. **Offline Support**: Queue operations for offline use

## API Integration

The client connects to the UAI platform via REST APIs:

- `/mobile/health` — Connection health check
- `/mobile/session/start` — User session initialization
- `/mobile/ai/feature` — AI feature usage
- `/mobile/sync` — Data synchronization
- `/mobile/offline/queue` — Offline queue management

## Demo Mode

Run with demo mode for testing:

```bash
python mobile_client.py --demo
```

This simulates user interactions without requiring a full server connection.