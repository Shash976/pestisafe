# PestiSafe 🌿

An Android IoT application for real-time pesticide residue detection on agricultural produce. PestiSafe connects to an electrochemical sensor over a local WiFi network, converts raw voltage readings into pesticide concentration values (ppm) via a calibration curve, and compares results against FAO Codex Maximum Residue Limits (MRLs) to give a clear **safe / unsafe** verdict on sampled produce.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Screen Flow](#screen-flow)
- [Database Schema](#database-schema)
- [Setup & Requirements](#setup--requirements)
- [Dependencies](#dependencies)
- [How It Works](#how-it-works)
- [Project Structure](#project-structure)
- [Known Limitations](#known-limitations)

---

## Overview

PestiSafe is designed for field use by agricultural inspectors, food safety auditors, or researchers who need a fast, portable way to test produce for pesticide contamination. The workflow is:

1. Connect your Android device to the same WiFi network as the sensor.
2. Fetch the latest pesticide & MRL database from the FAO Codex API.
3. Select the pesticide you are testing for.
4. Scan the local network to find the sensor device.
5. Calibrate the sensor using known reference concentrations.
6. Point the sensor at a food sample — view live concentration readings and an instant safety verdict.
7. Export results as CSV, JSON, or Excel for reporting.

---

## Features

| Feature | Detail |
|---|---|
| **Live sensor readings** | Polls the IoT sensor over HTTP every 3 seconds and displays real-time voltage and concentration |
| **Calibration engine** | Collects voltage readings at 8 known concentrations, fits a least-squares linear regression, and validates with R² ≥ 0.9 |
| **FAO MRL database** | Fetches and caches the complete FAO Codex pesticide + Maximum Residue Limit dataset on-device |
| **Safety verdict** | Compares live concentration against the FAO MRL for the selected pesticide-commodity pair; displays a clear SAFE ✅ or UNSAFE ⚠️ result |
| **IP Scanner** | Scans the local /24 subnet to discover sensor devices automatically |
| **Data export** | Export all captured readings as CSV, JSON, or Excel (.xlsx) and share via Android's standard file share sheet |
| **User accounts** | Local account system with BCrypt-hashed passwords and a DOB-verified password reset flow |
| **Secure networking** | Cleartext HTTP restricted to LAN IP ranges only (FAO API uses HTTPS) |

---

## Architecture

PestiSafe follows **Modern Android Architecture** with a clean separation of concerns:

```
UI Layer          →   Jetpack Compose + Material Design 3
State Management  →   ViewModel + MutableStateFlow / LiveData
Data Layer        →   Room Database + Repository pattern
Async             →   Kotlin Coroutines + SupervisorJob
Navigation        →   Jetpack Navigation Compose
```

### Key design decisions

- **Single ViewModel** (`MainViewModel`) acts as the central state owner for sensor data, calibration coefficients, and navigation state.
- **Repository pattern** wraps all DAO access so the ViewModel never talks to Room directly.
- **Reactive UI** — all DAO queries return `Flow` or `LiveData`, so the chart and readings update automatically when new data is captured.
- **Side-effect safety** — all navigation calls and state mutations are performed inside `LaunchedEffect` blocks, never directly during Compose's composition phase.

---

## Screen Flow

```
Login / Sign-Up
      │
      ▼
Main (Connect Screen)
  • Check WiFi connection
  • Fetch FAO pesticide + MRL dataset
      │
      ▼
Pesticide Selection
  • Choose target pesticide from FAO list
      │
      ▼
IP Scanner
  • Scan local /24 subnet (254 hosts concurrently)
  • Select sensor device IP
      │
      ▼
Calibration
  • Record voltage at 8 known concentrations
  • Fit linear regression (requires R² ≥ 0.9)
      │
      ▼
Home (Live Monitoring)
  • Real-time voltage + concentration display
  • Line chart of all captured readings
  • Select pesticide + commodity → view FAO MRL
  • SAFE / UNSAFE verdict
  • "Capture Reading" button → saves to database
  • Export data (CSV / JSON / Excel)
```

A dropdown menu in the top app bar allows jumping between any screen at any time.

---

## Database Schema

Five Room entities:

### `User`
| Column | Type | Notes |
|---|---|---|
| id | Int (PK, autoGen) | |
| name | String | Display name |
| username | String | Unique — enforced by index |
| email | String | |
| password | String | BCrypt hashed |
| dob | String | Used for password reset verification |

### `Pesticide`
| Column | Type | Notes |
|---|---|---|
| id | Int (PK) | FAO Codex pesticide ID |
| name | String | English name from FAO API |

### `Commodity`
| Column | Type | Notes |
|---|---|---|
| id | Int (PK, autoGen) | |
| name | String | Crop / food product name |

### `MRL`
| Column | Type | Notes |
|---|---|---|
| mrlID | Int (PK, autoGen) | |
| pesticideID | Int (FK → Pesticide) | CASCADE delete |
| commodityID | Int (FK → Commodity) | CASCADE delete |
| mrl | Double | Maximum Residue Limit in ppm |

### `DataValue`
| Column | Type | Notes |
|---|---|---|
| id | Int (PK, autoGen) | |
| voltage | Double | Raw sensor reading (V) |
| concentration | Double | Calculated concentration (ppm) |

---

## Setup & Requirements

### Prerequisites

- Android Studio Hedgehog or newer
- Android device or emulator running **Android 7.0+ (API 24+)**
- A compatible electrochemical sensor device on the same WiFi network

### Build

1. Clone the repository:
   ```bash
   git clone https://github.com/Shash976/wifi-data-app.git
   cd wifi-data-app
   ```
2. Open in Android Studio.
3. Let Gradle sync and download dependencies.
4. Run on a device or emulator (`Run > Run 'app'`).

### Permissions

The app requests the following at runtime:

| Permission | Reason |
|---|---|
| `INTERNET` | FAO API calls + sensor HTTP polling |
| `ACCESS_WIFI_STATE` | Check WiFi connectivity before connecting |
| `ACCESS_NETWORK_STATE` | Network availability checks |
| `READ/WRITE_EXTERNAL_STORAGE` | File export on Android 9 and below (not needed on API 29+) |

Cleartext HTTP is allowed **only** for local LAN IP ranges (192.168.x.x, 10.x.x.x, 172.16.x.x) via a Network Security Config. All external traffic (including FAO API) uses HTTPS.

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2025.06.00 | Declarative UI framework |
| Material Design 3 | — | UI components and theming |
| Room | 2.7.1 | Local SQLite ORM |
| Navigation Compose | 2.9.0 | Screen routing |
| Lifecycle ViewModel | 2.9.1 | State management |
| Kotlin Coroutines | 1.8.0 | Async operations |
| Vico Charts | 2.0.0-alpha.19 | Line chart rendering |
| Gson | 2.10.1 | FAO API JSON deserialization |
| BCrypt (`jbcrypt`) | 0.9.0 | Password hashing |
| Apache POI OOXML | 5.2.3 | Excel (.xlsx) export |
| WorkManager | 2.10.1 | Background task scheduling (future use) |

**SDK versions:** `minSdk = 24`, `targetSdk = 35`

---

## How It Works

### Calibration

The sensor produces a voltage proportional to pesticide concentration. Calibration establishes the mapping:

```
Concentration (ppm) = (Voltage - Intercept) / Slope
```

The app steps through 8 reference concentrations `[1.0, 10.0, 5.0, 7.5, 6.0, 2.5, 4.0, 1.25]` ppm. At each step, the user places the sensor in a reference solution and captures the voltage. Once ≥ 3 points are collected, least-squares linear regression is fitted. Calibration is accepted only when R² ≥ 0.9.

### FAO MRL Lookup

On the Connect screen, the app fetches from two FAO Codex endpoints:

- **Pesticide list:** `https://www.fao.org/jsoncodexpest/jsonrequest/pesticides/index.html`
- **MRL details per pesticide:** `https://www.fao.org/jsoncodexpest/jsonrequest/pesticides/details.html?id={id}`

Results are cached in the local Room database and deduped on subsequent fetches.

### Safety Verdict

Once a pesticide and commodity are selected on the Home screen:

1. The MRL for that pair is retrieved from the local database.
2. The current concentration (converted from live sensor voltage using the calibration coefficients) is compared to the MRL.
3. If `concentration > MRL` → **UNSAFE** (red warning).
4. If `concentration ≤ MRL` → **SAFE** (green confirmation).

### Data Capture & Export

Live readings update the display continuously but are **not** auto-saved. Tap **"Capture Reading"** to intentionally commit a reading to the database. Captured readings appear in the chart and can be exported:

- **CSV** — comma-separated with headers
- **JSON** — Gson-serialized array
- **Excel** — `.xlsx` workbook with column headers and auto-sized columns (via Apache POI)

Files are named `{username}_{pesticide}_{commodity}_data.{ext}` and shared via Android's standard share sheet.

---

## Project Structure

```
app/src/main/java/com/example/pestisafe/
├── MainActivity.kt              # Entry point, Navigation host, Scaffold
├── MainViewModel.kt             # Central state, sensor polling, FAO API, calibration math
├── Database.kt                  # Room entities, DAOs, Repository
├── Routes.kt                    # Navigation route enum
│
├── LoginSignUp.kt               # Auth screen (login, sign-up, password reset)
├── ConnectScreen.kt             # WiFi check + FAO data fetch
├── PesticideSelectionScreen.kt  # Pesticide picker dropdown
├── ScanScreen.kt                # LAN IP scanner
├── CalibrationScreen.kt         # Sensor calibration wizard
├── HomeScreen.kt                # Live monitoring, MRL verdict, export
│
├── MathFuncs.kt                 # Linear regression, R² calculation
├── Chart.kt                     # Vico chart configuration
├── Deserializers.kt             # Gson deserializers for FAO API JSON
├── DownloadFuncs.kt             # CSV / JSON / Excel export + file sharing
├── Formats.kt                   # Export format enum
│
└── ui/theme/
    ├── Theme.kt                 # Material3 theme setup
    ├── Color.kt                 # Color palette
    └── Type.kt                  # Typography
```

---

## Known Limitations

- **Offline mode** — The app requires WiFi throughout: for the FAO API fetch on first use and for sensor communication. No offline fallback exists once data is cached.
- **Single sensor** — The app connects to one sensor device at a time.
- **HTTP sensor protocol** — Sensor devices communicate over plain HTTP (acceptable for LAN-only use but not suitable for untrusted networks).
- **Legacy v1 code** — The `v1/` package contains an older Activity-based implementation. It is not used by the current Compose app and will be removed in a future cleanup.

---

## License

This project is developed as part of an academic research project. All rights reserved.
