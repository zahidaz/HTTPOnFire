<div align="center">
  <img src="docs/app_icon.png" alt="HTTP on Fire" width="120">

# HTTP on Fire

**Turn your Android phone into a powerful web server & remote control panel.**

[![Platform](https://img.shields.io/badge/Android-0D0D0F?style=for-the-badge&logo=android&logoColor=00D4AA)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-0D0D0F?style=for-the-badge&logo=kotlin&logoColor=A78BFA)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-0D0D0F?style=for-the-badge&logo=jetpackcompose&logoColor=00D4AA)](https://developer.android.com/jetpack/compose)
[![Ktor](https://img.shields.io/badge/Ktor-0D0D0F?style=for-the-badge&logo=ktor&logoColor=A78BFA)](https://ktor.io)
[![License](https://img.shields.io/badge/Apache_2.0-0D0D0F?style=for-the-badge)](LICENSE)

<br>

<img src="docs/hof.gif" alt="HTTP on Fire Demo" width="700">

</div>

<br>

## What is this?

HTTP on Fire runs a **real HTTP server** on your Android device. Open a browser on any device in your network, and you get a full **remote control dashboard** — battery stats, flashlight, volume, camera, microphone, GPS, contacts, app management, and more. No root. No cloud. Everything stays on your local WiFi.

You can also create custom endpoints, share files, host static websites, and build APIs — all from your phone.

<br>

## Screenshots

<div align="center">
<table>
<tr>
<td align="center"><b>Home</b></td>
<td align="center"><b>Built-in APIs</b></td>
<td align="center"><b>Settings</b></td>
<td align="center"><b>Activity Log</b></td>
</tr>
<tr>
<td><img src="docs/screenshot_home.png" width="200"></td>
<td><img src="docs/screenshot_builtin.png" width="200"></td>
<td><img src="docs/screenshot_settings.png" width="200"></td>
<td><img src="docs/screenshot_logs.png" width="200"></td>
</tr>
</table>

<table>
<tr>
<td align="center"><b>Web Dashboard — Status</b></td>
<td align="center"><b>Web Dashboard — Controls</b></td>
</tr>
<tr>
<td><img src="docs/screenshot_dashboard.png" width="300"></td>
<td><img src="docs/screenshot_dashboard2.png" width="300"></td>
</tr>
</table>
</div>

<br>

## Features

### Remote Device Control

| Feature | Endpoint | Description |
|---------|----------|-------------|
| **Dashboard** | `GET /api/dashboard` | Full web control panel — manage everything from a browser |
| **Flashlight** | `POST /api/flashlight` | Toggle the torch on/off |
| **Volume** | `GET/POST /api/volume` | Read or set media, ring, alarm, notification volume |
| **Vibrate** | `POST /api/vibrate` | Vibrate the device for a custom duration |
| **Find My Phone** | `POST /api/ring` | Play an alarm sound to locate the device |
| **Text to Speech** | `POST /api/speak` | Speak text aloud through the device speakers |
| **Clipboard** | `GET/POST /api/clipboard` | Read or write the device clipboard |
| **Camera** | `POST /api/camera` | Capture a photo from front or back camera |
| **Microphone** | `GET /api/mic/stream` | Live audio stream from the device mic |
| **Notifications** | `POST /api/notify` | Push custom notifications to the device |
| **App Launch** | `POST /api/apps/launch` | Launch any installed app by package name |
| **App Stop** | `POST /api/apps/stop` | Stop background processes of an app |

### Device Information

| Feature | Endpoint | Description |
|---------|----------|-------------|
| **Battery** | `GET /api/battery` | Level, charging status, health, temperature |
| **WiFi** | `GET /api/wifi` | SSID, IP, signal strength, frequency |
| **Device Info** | `GET /api/device` | Model, OS, memory, storage |
| **Location** | `GET /api/location` | GPS coordinates, altitude, accuracy |
| **Contacts** | `GET /api/contacts` | Read device contacts |
| **Installed Apps** | `GET /api/apps` | List all installed applications |

### Server & Tools

| Feature | Endpoint | Description |
|---------|----------|-------------|
| **Server Status** | `GET /api/status` | Health check and uptime |
| **QR Code** | `GET /api/qr` | Generate QR codes from any text |
| **Echo** | `GET /api/echo` | Mirror back request details |
| **Proxy** | `GET /api/proxy` | Forward requests to external URLs |
| **Swagger Docs** | `GET /api/swagger` | Interactive API documentation |
| **OpenAPI Spec** | `GET /api/json` | Machine-readable API schema |

### Content Hosting

- **Static file serving** — share any file from your phone
- **Directory browsing** — share entire folders with file explorer UI & upload support
- **Custom endpoints** — create GET/POST/PUT/DELETE/PATCH routes with custom responses
- **Redirect routes** — create short URLs that redirect to other destinations
- **Route persistence** — all routes are saved in a local database across restarts

<br>

## Installation

### Download APK

Grab the latest build from [**GitHub Releases**](https://github.com/zahidaz/HTTPOnFire/releases).

### Build from Source

```bash
git clone https://github.com/zahidaz/HTTPOnFire.git
cd HTTPOnFire
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/`

<br>

## Quick Start

```bash
# Check server status
curl http://<phone-ip>:43567/api/status

# Open the dashboard in your browser
open http://<phone-ip>:43567/api/dashboard

# Toggle flashlight
curl -X POST "http://<phone-ip>:43567/api/flashlight?enable=true"

# Capture a photo
curl -X POST "http://<phone-ip>:43567/api/camera?facing=back" -o photo.jpg

# Send a notification
curl -X POST http://<phone-ip>:43567/api/notify \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello","body":"From my laptop!"}'

# Launch an app
curl -X POST "http://<phone-ip>:43567/api/apps/launch?package=com.android.chrome"

# Listen to live microphone (open in browser)
open "http://<phone-ip>:43567/api/mic/stream"
```

> **Emulator?** Run `adb forward tcp:43567 tcp:43567` then use `localhost` instead of the phone IP.

<br>

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Platform | Android (SDK 24–36) |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + StateFlow |
| Server | Ktor CIO |
| API Docs | OpenAPI + Swagger UI |
| Database | Room |
| Build | Gradle + Version Catalogs |

<br>

## Contributing

Contributions welcome — bug fixes, new endpoints, UI improvements, docs. Open an issue or send a PR.

<br>

## License

```
Copyright 2025 zahidaz

Licensed under the Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0
```
