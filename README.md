# Movement Report — Android App

A complete offline Android application for railway employees to maintain and export monthly movement reports as professional PDFs.

---

## Features

| Feature | Description |
|---|---|
| First-time Setup | Collects Name, Designation, PF No, Pay, Level — saved locally |
| Add Duty | Date (default today), Train No, From/To Station → Remarks = "Duty" |
| Add Rest | Date + Rest Type (Rest / Casual Rest) → Remarks = "Rest" / "C-Rest" |
| Edit / Delete | Swipe or tap to edit or delete any existing entry |
| Search by Date | Filter records by a specific date |
| Monthly View | Browse all entries for any selected month |
| Export PDF | Generates a professional railway movement report PDF with header, table, summary, signature |
| Settings | Edit your profile details at any time |
| Dark Mode | Full Material Design 3 with automatic dark mode support |
| 100% Offline | Room (SQLite) database — no internet required, data persists across restarts |

---

## Project Structure

```
MovementReport/
├── app/
│   ├── build.gradle                          # App-level build config + dependencies
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/railway/movementreport/
│       │   ├── MovementReportApp.kt          # Application class (DB + Repo init)
│       │   ├── data/
│       │   │   ├── entity/MovementEntry.kt   # Room entity (table schema)
│       │   │   ├── dao/MovementEntryDao.kt   # All SQL queries
│       │   │   ├── database/MovementDatabase.kt
│       │   │   └── repository/MovementRepository.kt
│       │   ├── ui/
│       │   │   ├── MovementViewModel.kt      # Shared ViewModel + Factory
│       │   │   ├── setup/SetupActivity.kt    # First-launch profile setup
│       │   │   ├── home/HomeActivity.kt      # Main screen with 4 action cards
│       │   │   ├── duty/AddDutyActivity.kt   # Add / Edit duty entries
│       │   │   ├── rest/AddRestActivity.kt   # Add / Edit rest entries
│       │   │   ├── records/
│       │   │   │   ├── RecordsActivity.kt    # View, search, edit, delete
│       │   │   │   └── MovementEntryAdapter.kt
│       │   │   ├── export/ExportActivity.kt  # PDF generation & sharing
│       │   │   └── settings/SettingsActivity.kt
│       │   └── utils/
│       │       ├── DateUtils.kt              # Date format helpers
│       │       ├── UserPreferences.kt        # SharedPreferences wrapper
│       │       └── PdfGenerator.kt           # Android Canvas PDF renderer
│       └── res/
│           ├── layout/                       # 9 XML layouts
│           ├── drawable/                     # 18 vector icons + shapes
│           ├── values/                       # colors, strings, themes (light)
│           ├── values-night/                 # themes (dark mode)
│           ├── mipmap-*/                     # Adaptive launcher icons
│           └── xml/file_paths.xml            # FileProvider paths
├── build.gradle                              # Root build config
├── settings.gradle
└── gradle.properties
```

---

## Database Schema

**Table: `movement_entries`**

| Column | Type | Description |
|---|---|---|
| `id` | INTEGER (PK, autoincrement) | Unique row ID |
| `date` | TEXT | Format: `yyyy-MM-dd` |
| `trainNumber` | TEXT | Blank for rest entries |
| `stationFrom` | TEXT | Blank for rest entries |
| `stationTo` | TEXT | Blank for rest entries |
| `remarks` | TEXT | `"Duty"`, `"Rest"`, or `"C-Rest"` |
| `entryType` | TEXT | `"DUTY"` or `"REST"` |

---

## PDF Report Format

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
         MOVEMENT REPORT
           JUNE 2025
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Name: John Doe          Designation: Loco Pilot
PF No: CR123456         Pay: ₹ 58000
Level: Level 6
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Date      | Train No | From   | To     | Remarks
01/06/25  | 12345    | CSTM   | PUNE   | Duty
02/06/25  |          |        |        | Rest
03/06/25  |          |        |        | C-Rest
...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
SUMMARY: Duty Days: 18 | Rest Days: 8 | C-Rest Days: 4
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Place: Mumbai        Date: 30-06-2025
                     ________________________
                     (John Doe)
                     Loco Pilot
```

---

## Build & Run Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with API 34
- Minimum device: Android 8.0 (API 26)

### Steps

1. **Open the project**
   ```
   File → Open → select the MovementReport folder
   ```

2. **Sync Gradle**
   Android Studio will auto-sync. If not: `File → Sync Project with Gradle Files`

3. **Run on device or emulator**
   ```
   Run → Run 'app'   (or Shift+F10)
   ```

4. **Build APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

### Key Dependencies
| Library | Purpose |
|---|---|
| Room 2.6.1 | SQLite ORM for local storage |
| KSP 1.9.10 | Kotlin Symbol Processing for Room |
| Material 3 1.11.0 | UI components + dark mode |
| ViewModel / LiveData 2.7.0 | MVVM architecture |
| Coroutines 1.7.3 | Async database operations |
| AndroidX FileProvider | Secure PDF file sharing |
| Android PDF Canvas API | Built-in PDF generation (no external lib needed) |

> **Note:** PDF generation uses Android's built-in `android.graphics.pdf.PdfDocument` — no third-party PDF library required. The iText dependency in build.gradle can be removed if unused.

---

## App Flow

```
Launch
  └─ SetupActivity (first launch only)
       └─ HomeActivity
            ├─ Add Duty → AddDutyActivity
            ├─ Add Rest → AddRestActivity
            ├─ View Records → RecordsActivity
            │    ├─ Edit → AddDutyActivity / AddRestActivity
            │    └─ Delete (with confirmation dialog)
            ├─ Export Report → ExportActivity
            │    └─ Generates PDF → Share/Save via system sheet
            └─ Settings FAB → SettingsActivity
```

---

## Customisation Notes

- **Add more columns**: Extend `MovementEntry.kt`, update DAO queries, add a Room migration
- **Change PDF styling**: All rendering code is in `PdfGenerator.kt` — colors, fonts, layout are all editable
- **Add more rest types**: Update `AddRestActivity.kt` RadioGroup and the remarks mapping logic
- **Change date formats**: All formats are constants in `DateUtils.kt`
