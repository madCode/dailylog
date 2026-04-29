# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

dailyLog is a native Android app (Kotlin) for distraction-free journaling with customizable text shortcuts. Users select a markdown/text file, define shortcut buttons with templated text (including datetime patterns), and insert them at cursor position with one tap. Shortcuts are stored in a Room database; the log file and cursor position are persisted via SharedPreferences and the Android file system.

## Build & Test Commands

```bash
# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew testDebugUnitTest --tests "com.app.dailylog.utils.ShortcutUtilsTest"

# Run instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Build (debug + release)
./gradlew build

# Install debug APK on connected device
./gradlew installDebug

# Coverage
./gradlew koverHtmlReport   # HTML: app/build/reports/kover/html/index.html
./gradlew koverXmlReport    # XML:  app/build/reports/kover/xml/report.xml
./gradlew koverVerify       # Fails if line coverage < 5%
```

**Requirements**: JDK 21 (OpenJDK 21), Android SDK (API 36 target, 23 min), Kotlin 2.2.10.

## Architecture

Single-Activity MVVM. `MainActivity` creates the `Repository` and `PermissionChecker`, then swaps between three fragments based on app state:

- **WelcomeFragment** — shown on first launch when no file is selected. Lets user create or pick a file, then calls back into MainActivity to open the log.
- **LogFragment** — main screen. Displays the log file in a text editor, a shortcut tray (horizontal staggered RecyclerView), and save/settings buttons. Saves on pause (smart save: skips if content hash unchanged) and on explicit button press (force save).
- **SettingsFragment** — manage shortcuts (add, edit, delete, reorder via drag-and-drop), change the active file, and import/export shortcuts.

Each fragment takes a pre-constructed ViewModel (not a factory lookup) passed from MainActivity, except `SettingsViewModel` which uses `ViewModelProvider` for lifecycle scoping.

### Repository Layer

`Repository` implements `RepositoryInterface`, which composes:

- **`FileRepositoryInterface`** — reads/writes the user's log file via Android `ContentResolver` (URI-based access). Uses MD5 hashing to implement smart save (skip if unchanged). File URI and cursor index are stored in `SharedPreferences`.
- **`ShortcutRepositoryInterface`** — CRUD operations against `ShortcutDao` (Room). Shortcuts have a `position` field for ordering; drag-to-reorder updates all positions. Bulk add validates each row (unique label, non-empty text, valid cursor index, valid type).

### Shortcut System

`Shortcut` is the Room entity (`@Entity`). Fields: `label` (PK), `value`, `cursorIndex`, `type`, `position`.

Two shortcut types (`ShortcutType`):
- `TEXT` — inserts `value` verbatim.
- `DATETIME` — value contains `{DATETIME: <pattern>}` tokens replaced at insert time using `java.time.DateTimeFormatter` patterns (Android O+). `ShortcutUtils` handles replacement and adjusts `cursorIndex` to account for the expanded datetime string length.

JSON export format (v3.1.0+): `{ "schemaVersion": N, "shortcuts": [...] }`. Legacy CSV import remains available but is being phased out.

### Key Files

| File | Purpose |
|---|---|
| `MainActivity.kt` | Fragment orchestration, startup migration warning |
| `repository/Repository.kt` | Concrete repository; wires together file + shortcut interfaces |
| `repository/FileRepositoryInterface.kt` | File read/write, smart save via MD5 hash |
| `repository/ShortcutRepositoryInterface.kt` | Shortcut CRUD, bulk add, validation, export rows |
| `repository/Shortcut.kt` | `Shortcut` entity + `ShortcutDao` + `ShortcutType` |
| `repository/ShortcutDatabase.kt` | Room database definition |
| `utils/ShortcutUtils.kt` | Datetime token replacement at insert time |
| `utils/JsonShortcutUtils.kt` | JSON import/export with Gson |
| `ui/log/LogViewModel.kt` | File load/save, shortcut list exposure |
| `ui/settings/SettingsViewModel.kt` | Shortcut management, file selection, import/export |

## Releasing

1. Bump `versionCode` and `versionName` in `app/build.gradle`.
2. Commit, tag `v<version>`, push the tag.
3. The `release` GitHub Actions workflow triggers automatically, builds a signed APK, and attaches it to the GitHub Release page.
4. F-Droid picks up new tags automatically; verify within 24h on the [F-Droid page](https://f-droid.org/packages/com.app.dailylog/).

### First-time setup (required for signing)

The release workflow requires four repository secrets to sign the APK. Set these once in **GitHub → Settings → Secrets and variables → Actions**:

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | Base64-encoded `.jks` file: `base64 -i your-keystore.jks \| pbcopy` |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

## Library Choices

When adding dependencies, prefer Kotlin-native libraries (no Java plugin requirement). Current key deps: Room (database), Gson (JSON), OpenCSV (legacy CSV import), Kover (coverage), Mockito (test mocks), Espresso (UI tests).
