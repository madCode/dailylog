# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

DailyLog is a native Android app (Kotlin) for distraction-free journaling with customizable text shortcuts. Users select a markdown/text file, define shortcut buttons with templated text (including datetime patterns), and insert them at cursor position with one tap. Shortcuts are stored in a Room database; the log file and cursor position are persisted via SharedPreferences and the Android file system.

## Privacy

This repository is public, and this app holds a person's journal. Never commit anyone's own
data: no journal text, no file names, no `content://` URIs, no accounts or emails from
anyone's own setup. Tests, screenshots and sample data use made-up material only.

## Posting as madCode

Claude Code posts from madCode's account, so anything it writes says so **at the top**, where
it's read, not only in a footer a reader scrolls past. The first line of a pull request
description, an issue or PR comment, or a review:

    🤖 **Claude · <tag>**

Tags: `pull request`, `proposal`, `fix ready`, `needs info`, `update`, `review notes`,
`question for madCode`. `update` when none of the others fit. The first comment in an issue
thread also opens by saying who is writing and what to expect, which the `handle-issues` skill
spells out.

Nothing from the Claude GitHub App needs this: its author already says so.

This rule, and the Privacy, Tests, Documentation and Pull requests sections below, are shared
with the other projects in
[claude-playground](https://github.com/madCode/claude-playground/blob/main/CLAUDE.md), whose
root CLAUDE.md has the same rules for any project. A change to one belongs in the other too.

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
./gradlew koverXmlReport    # XML:  app/build/reports/kover/report.xml
./gradlew koverVerify       # Fails if line coverage < 85%
```

Unit tests include Robolectric tests that launch the whole app on the JVM (`app/src/test/.../testutil/AppRobolectricTest.kt` is the base class). They use the real file-backed Room database, so they cover the cold-start path; prefer them over instrumentation tests for UI behavior. CI also runs the instrumentation tests on emulators and uploads a debug APK (`com.app.dailylog.debug`, installs beside the release app) as a build artifact.

**Requirements**: JDK 21 (OpenJDK 21), Android SDK (API 37 target, 23 min), Kotlin 2.2.10.

## Architecture

Single-Activity MVVM, three fragments, a `Repository` composing a file interface and a shortcut
interface over Room. [ARCHITECTURE.md](ARCHITECTURE.md) has the shape of it and what each file
is for; read it before changing behaviour.

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

## Code Comments

Keep comments to what the code can't say on its own:

- **Explain why, not what.** Comment a non-obvious reason: a workaround, a platform or library quirk, an ordering constraint, a magic number. Link the upstream issue when there is one.
- **Skip the obvious.** Don't restate the code, describe a well-named function, or explain common Android knowledge (e.g. why fragments need a `FragmentFactory`, what `applicationIdSuffix` does).
  The exception is a summary of a large or non-obvious stretch of code, such as a run of unclear math (e.g. "Calculates the keyboard's height from X and Y because Z").
- **Reviewer context goes in the PR, not the code.** What changed, what was tried, and why this approach won belong in the commit message or PR description.
- **One comment per block.** When several lines share a reason (e.g. a group of CI settings), put one comment above the block saying why it exists, not a comment on each line.
- **Keep them short.** Usually one line, two at most.

## Code Review

Review changed code for these, beyond the usual correctness pass:

- File I/O and error handling for markdown or plaintext files.
- Memory management in `MainActivity` and UI components.
- Kotlin best practices and idiomatic usage.
- Android lifecycle management and resource cleanup.
- UI responsiveness and performance in the logging interface.
- Handling of user-defined shortcuts and text insertion.
- Data persistence and repository implementation.
- UX and UI consistency: matches existing screens' styling and spacing, keeps the
  journaling flow distraction-free, and works with the on-screen keyboard, small
  screens and dark theme.

Give specific, actionable feedback.

## Tests

A test should be able to catch a plausible regression. Test behaviour, not structure, end to end
where you can. Don't feed code inputs it can never receive, or write a test only to lift
coverage. A test that waits for the screen or the database waits for the condition, not a fixed
time. When a change has no behaviour to test, say so in the PR instead of inventing a test.

## Documentation

Docs are for people: keep them readable, current and short.

- A change that alters behaviour updates the doc that describes it, in the same PR:
  ARCHITECTURE.md for how the code fits together, README.md for what the app does,
  README-developer.md for building and releasing.
- Every few cycles, a documentation pass: check the docs against the code, fix what's stale, cut
  what's grown long or become history.
- Plain words over jargon, short sections, one idea per bullet.

## Pull requests

Several small PRs beat one large one. The description says what changed, how it was tested --
the commands run and what they showed, rather than "tests pass" -- and what the review found.
Merge only with CI green.

Before opening a PR that changes behaviour, have a fresh-eyes subagent review the diff against
the `## Code Review` list above. Point it at the risky parts:

- The log file: a save cut short or run twice, the smart-save hash, the cursor index, and what
  happens when the file or its `content://` permission has gone.
- Shortcut import and export as untrusted input: a huge or malformed JSON or CSV, duplicate
  labels, a cursor index out of range, datetime patterns that don't parse.
- Rotation, and the app being killed and restored: what is on screen, open dialogs, and unsaved
  text.
- The on-screen keyboard, small screens, large font scale and dark theme.
- Accessibility: TalkBack labels, touch targets.

Ask for concrete findings only: file:line and a failure scenario, most severe first, no edits.
Ask for bugs and gaps against what the change is for, not style or what might be nice. Verify
each finding before acting on it, and say in the PR what the review found and what was fixed or
deliberately left. Docs-, comment- and config-only changes can skip this.

## Library Choices

When adding dependencies, prefer Kotlin-native libraries (no Java plugin requirement). Current key deps: Room (database), Gson (JSON), OpenCSV (legacy CSV import), Kover (coverage), Mockito (test mocks), Robolectric (JVM UI tests), Espresso (UI tests).
