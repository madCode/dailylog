# Architecture

How the code fits together. `CLAUDE.md` holds the rules; this holds the shape, so it is read
when you need it rather than loaded into every session.

Single-Activity MVVM. `MainActivity` creates the `Repository`, then swaps between three fragments based on app state:

- **WelcomeFragment** — shown on first launch when no file is selected. Lets user create or pick a file, then calls back into MainActivity to open the log.
- **LogFragment** — main screen. Displays the log file in a text editor, a shortcut tray (horizontal staggered RecyclerView), and save/settings buttons. Saves on pause (smart save: skips if content hash unchanged) and on explicit button press (force save).
- **SettingsFragment** — manage shortcuts (add, edit, delete, reorder via drag-and-drop), change the active file, and import/export shortcuts.

Each fragment takes a pre-constructed ViewModel (not a factory lookup) passed from MainActivity, except `SettingsViewModel` which uses `ViewModelProvider` for lifecycle scoping.

## Repository Layer

`Repository` implements `RepositoryInterface`, which composes:

- **`FileRepositoryInterface`** — reads/writes the user's log file via Android `ContentResolver` (URI-based access). Uses MD5 hashing to implement smart save (skip if unchanged). File URI and cursor index are stored in `SharedPreferences`.
- **`ShortcutRepositoryInterface`** — CRUD operations against `ShortcutDao` (Room). Shortcuts have a `position` field for ordering; drag-to-reorder updates all positions. Bulk add validates each row (unique label, non-empty text, valid cursor index, valid type).

## Shortcut System

`Shortcut` is the Room entity (`@Entity`). Fields: `label` (PK), `value`, `cursorIndex`, `type`, `position`.

Two shortcut types (`ShortcutType`):
- `TEXT` — inserts `value` verbatim.
- `DATETIME` — value contains `{DATETIME: <pattern>}` tokens replaced at insert time using `java.time.DateTimeFormatter` patterns (Android O+). `ShortcutUtils` handles replacement and adjusts `cursorIndex` to account for the expanded datetime string length.

JSON export format (v3.1.0+): `{ "schemaVersion": N, "shortcuts": [...] }`. Legacy CSV import remains available but is being phased out.

## Key Files

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
