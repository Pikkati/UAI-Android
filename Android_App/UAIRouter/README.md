# UAI Router Mobile Dashboard - User Guide

## Overview
The UAI Router Mobile Dashboard is a comprehensive enterprise-grade mobile application designed for IT administrators, AI engineers, and DevOps professionals to monitor and manage UAI (Universal AI) Router systems in real-time.

## Features

### 🔐 Authentication
- **Secure Login**: Access the dashboard with enterprise credentials
- **Demo Credentials (example)**: Username: `demo@example.com`, Password: `example-password` (do not use in production)
- **Session Management**: Automatic logout on app close for security

### 📊 Dashboard Tab
- **Real-time Metrics**: System performance indicators
- **Status Overview**: Current system health and connectivity
- **Activity Feed**: Recent system activities and events
- **Quick Actions**: Direct access to critical functions

### 🤖 Agents Tab
- **AI Agent Management**: Monitor and control AI agents
- **Status Monitoring**: Real-time agent health and performance
- **Search & Filter**: Find specific agents quickly
- **Agent Details**: Comprehensive agent information and metrics

### 📈 Analytics Tab
- **Performance Charts**: Visual data representation
- **System Metrics**: CPU, memory, and network usage
- **Trend Analysis**: Historical performance data
- **Custom Dashboards**: Personalized metric views

### 📋 Logs Tab
- **Comprehensive Logging**: All system events and activities
- **Advanced Filtering**: Filter by level, type, and time
- **Real-time Updates**: Live log streaming
- **Search Functionality**: Quick log lookup

### 🔔 Notifications Tab
- **Push Notifications**: Real-time alerts via Firebase
- **Notification Center**: Centralized alert management
- **Unread Badges**: Visual indicators for new alerts
- **Categorization**: Organize notifications by priority

### ⚙️ Settings Tab
- **App Configuration**: Customize app behavior
- **Notification Preferences**: Control alert settings
- **Data Management**: Cache and offline settings
- **About & Support**: App information and help

## Offline Mode
- **Automatic Detection**: Seamless offline/online switching
- **Data Caching**: Access cached data when offline
- **Sync on Reconnect**: Automatic data synchronization
- **Offline Indicator**: Clear visual status indication

## Technical Specifications

### Platform Support
- **Android**: Primary platform (SDK 28+)
- **iOS**: Compatible with React Native framework

### Dependencies
- React Native 0.73.0
- Firebase Cloud Messaging
- AsyncStorage for offline caching
- react-native-chart-kit for analytics
- react-native-vector-icons for UI

### Performance
- Fast startup (< 15 seconds build time)
- Smooth navigation and animations
- Efficient memory usage
- Battery-optimized background processes

## Getting Started

### Prerequisites
1. Android Studio with SDK 28+
2. Node.js and npm
3. React Native CLI
4. Android emulator or physical device

### Installation
```bash
# Navigate to project directory
cd Android_App/UAIRouter

# Install dependencies
npm install

# Start Metro server
npx react-native start --port 8082

# Run on Android
npx react-native run-android --port 8082
```

### First Time Setup
1. Launch the app on your Android device/emulator
2. Enter demo credentials: `admin` / `password`
3. Grant notification permissions when prompted
4. The dashboard will load with sample data

## Usage Guide

### Navigation
- **Bottom Tabs**: Switch between main sections
- **Header Icons**: Quick access to notifications and settings
- **Search Bars**: Available in Agents, Logs, and Notifications tabs

### Search & Filtering
- **Real-time Search**: Type to instantly filter results
- **Advanced Filters**: Use filter buttons for specific criteria
- **Multiple Filters**: Combine search and filters for precise results

### Notifications
- **Firebase Integration**: Receive push notifications
- **Badge Indicators**: Visual unread notification counts
- **Mark as Read**: Tap notifications to mark them read
- **Clear All**: Remove all notifications at once

### Offline Operation
- **Automatic Mode**: App detects connectivity changes
- **Cached Data**: Access previously loaded data offline
- **Sync Indicator**: Shows when data is being synchronized
- **Limited Features**: Some real-time features unavailable offline

## Troubleshooting

### Emulator/ADB Unauthorized Device Issues
If you see errors like `device unauthorized` or the build script hangs waiting for a device:

- Make sure your Android emulator is running and authorized.
- If you have a physical device connected that shows as `unauthorized` in `adb devices`, disconnect it or revoke USB debugging authorizations on the device.
- The build script now prefers emulator devices (emulator-*) to avoid unauthorized physical devices interfering.
- You can run `adb devices -l` to check device status. Only one emulator should show as `device`.
- If you need to reset authorizations, on your phone go to Developer Options > Revoke USB debugging authorizations, then reconnect and accept the prompt.


### Common Issues

#### App Won't Start
- Ensure Metro server is running on port 8082
- Check Android emulator is running
- Verify all dependencies are installed

#### Login Issues
- Use demo credentials: admin/password
- Check internet connection for authentication
- Clear app data if persistent issues

#### Notifications Not Working
- Grant notification permissions in Android settings
- Check Firebase configuration
- Verify internet connection

#### Performance Issues
- Close other apps on emulator
- Clear app cache in Settings
- Restart Metro server

### Error Codes
- **Build Errors**: Check React Native and Android SDK versions
- **Network Errors**: Verify internet connectivity
- **Permission Errors**: Grant required Android permissions

## Development

### Project Structure
```
UAIRouter/
├── android/           # Android native code
├── ios/              # iOS native code (future)
├── src/              # Source code (future organization)
├── App.js            # Main application component
├── package.json      # Dependencies and scripts
└── README.md         # This documentation
```

### Key Components
- **Authentication**: Login screen and session management
- **Dashboard**: Main interface with tab navigation
- **Data Management**: Offline caching and sync logic
- **Notifications**: Firebase integration and alert system
- **Analytics**: Chart rendering and data visualization

### Customization
- **Themes**: Modify colors in StyleSheet objects
- **Features**: Add new tabs or functionality
- **Data Sources**: Integrate with real UAI Router APIs
- **Branding**: Update logos and app metadata

## Support & Maintenance

### Regular Maintenance
- Update dependencies quarterly
- Test on latest Android versions
- Monitor Firebase usage and costs
- Backup user data and configurations

### Future Enhancements
- iOS platform support
- Advanced analytics and reporting
- Multi-user collaboration features
- Integration with additional monitoring tools

## Security Considerations
- Secure credential storage
- Encrypted data transmission
- Regular security updates
- Compliance with enterprise security policies

---

**Version**: 1.0.0
**Last Updated**: October 13, 2025
**Platform**: Android
**Framework**: React Native 0.73.0</content>
<parameter name="filePath">c:\UAI_Copilot_Automation_Tool\Android_App\UAIRouter\README.md