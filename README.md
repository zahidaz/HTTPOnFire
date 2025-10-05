<div align="center">
  <img src="docs/app_icon.png" alt="HTTP on Fire Logo" width="120">

# HTTP on Fire

[![Platform](https://img.shields.io/badge/Platform-Android-FF8F00?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-E65100?style=for-the-badge)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-FF7043?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-8D6E63?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Ktor](https://img.shields.io/badge/Ktor-5D4037?style=for-the-badge&logo=ktor&logoColor=white)](https://ktor.io)
</div>

<div align="center">
No-code Android HTTP server and device management.
</div>

## Overview

A professional Android HTTP server application that transforms your mobile device into a
full-featured web server. Works on your local WiFi network - you can share content and let other devices access your phone via its IP address.

<div align="center">
  <img src="docs/hof.gif" alt="HTTP on Fire Demo">
</div>

## Installation

### Download APK

Download the latest debug build from GitHub Releases:

**[Download HTTP on Fire APK](https://github.com/zahidaz/HTTPOnFire/releases)**

1. Go to the [Releases page](https://github.com/zahidaz/HTTPOnFire/releases)
2. Download the latest APK file
3. Enable "Install unknown apps" for your browser/file manager in Android settings
4. Install the APK file

### Build from Source

```bash
git clone https://github.com/zahidaz/HTTPOnFire.git
cd HTTPOnFire
./gradlew assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/`

## Features

### Current Features

- [x] **HTTP server hosting** - Turn your phone into a website that you can share with others
- [x] **Visual API route builder** - Build web pages using simple forms without writing any code
- [x] **Static file serving** - Share any file from your phone directly through the web
- [x] **Directory browsing and static website hosting** - Share entire folders for easy browsing and downloading
- [x] **Custom HTTP endpoints (GET, POST, PUT, DELETE, PATCH)** - Create custom responses with text, files, or redirects
- [x] **OpenAPI documentation with Swagger UI** - Get professional API documentation generated automatically *(Android 12+ required)*
- [x] **CORS configuration** - Control which websites and devices can access your server
- [x] **Route persistence with Room database** - Keep all your custom pages saved between app restarts
- [x] **Request monitoring and logs** - See who visits your server with detailed activity logs
- [x] **Device notification API endpoint** - Receive notifications on your phone from other devices via web requests
- [x] **HTTP proxy endpoint** - Your phone acts as a proxy server that fetches any URL and returns the content to the client *(WebView rendering coming in Phase 2)*

### Planned Features

- [ ] **File upload handling** - Accept file uploads to your phone through the browser
- [ ] **Device trigger endpoints (flashlight, vibration, volume control)** - Remotely control your phone's hardware features
- [ ] **SQLite database, query builder and query routes** - Build and query databases on your phone through web interface
- [ ] **App management endpoints (install, uninstall, launch apps)** - Remotely manage your phone's apps via web commands
- [ ] **Device sensors data exposure (accelerometer, gyroscope, GPS)** - Expose your phone's sensor data through web endpoints
- [ ] **Device information API endpoints (battery, storage, network)** - Share your phone's system information via web requests
- [ ] **Authentication system with pre-shipped themed login pages** - Protect your server with ready-made login screens
- [ ] **HTTP proxy WebView rendering (Phase 2)** - Option to render pages through WebView before returning content to bypass anti-bot measures and execute JavaScript
- [ ] **Redirect routes** - Create custom URLs that redirect visitors to other websites
- [ ] **Third-party content provider integration (Contacts, SMS, Calendar, Photos)** - Expose your phone's personal data through web endpoints
- [ ] **Device AI model exposure** - Run and access AI models on your phone via web requests
- [ ] **IFTTT-style logic composer with input/output chains** - Build automated workflows triggered by web requests
- [ ] **Camera and microphone access endpoints** - Remotely access your phone's camera and microphone via web

## Usage Examples

The server runs on port `43567` by default. Replace `192.168.1.100` with your phone's actual IP address.

### Running on Android Emulator

When using Android Emulator, you'll need to set up port forwarding since the emulator runs in an isolated network:

```bash
# Set up port forwarding (run this in your terminal)
adb forward tcp:43567 tcp:43567

# Then use localhost instead of phone IP
curl http://localhost:43567/api/status
```

### Built-in API Endpoints

```bash
# Check server status
curl http://192.168.1.100:43567/api/status

# Send notification to phone
curl -X POST http://192.168.1.100:43567/api/notify \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Alert",
    "body": "Server backup completed",
    "priority": "HIGH"
  }'

# Get API documentation (Android 12+)
curl http://192.168.1.100:43567/api/json

# Browse API docs in browser
# Visit: http://192.168.1.100:43567/api/swagger

# Proxy external URLs through your phone
curl http://192.168.1.100:43567/api/proxy/https://api.github.com/users/github

# Proxy with custom headers
curl http://192.168.1.100:43567/api/proxy/https://httpbin.org/headers \
  -H "Authorization: Bearer your-token" \
  -H "X-Custom-Header: value"

# Proxy POST requests
curl -X POST http://192.168.1.100:43567/api/proxy/https://jsonplaceholder.typicode.com/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"test","body":"content","userId":1}'

# Set custom timeout (milliseconds)
curl http://192.168.1.100:43567/api/proxy/https://slow-api.example.com \
  -H "X-Proxy-Timeout: 60000"
```

### Custom Endpoints (created via Visual Builder)

```bash
# Custom API endpoint
curl http://192.168.1.100:43567/api/hello

# File download
curl http://192.168.1.100:43567/files/document.pdf -o document.pdf

# Directory browsing
# Visit: http://192.168.1.100:43567/shared/
```

## Contributing

Contributions are welcome! Whether you're fixing bugs, adding features, or improving documentation,
your help is appreciated.

## License

Copyright 2025 zahidaz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
