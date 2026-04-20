# Repository Overview

## Project Description
- **What this project does**: dailyLog is an Android application that enables rapid logging with customizable keyboard shortcuts. It allows users to quickly add information to text files (particularly markdown files) using predefined shortcuts for fast journaling, habit tracking, or note-taking.

- **Main purpose and goals**: The main goal is to provide a distraction-free typing environment with customizable shortcuts for quick text insertion. It's designed for journaling, habit tracking, and fast note-taking into text-based systems like Obsidian vaults.

- **Key technologies used**: 
  - Android (Native)
  - Kotlin
  - Gradle build system
  - Java-based Android development

- When recommending tools and libraries, pick those optimized for Kotlin-native apps that don't use the java plugin.

## Architecture Overview
- **High-level architecture**: This is a standard Android mobile application with a single activity (MainActivity) that manages the logging interface and shortcut functionality.

- **Main components and their relationships**: 
  - MainActivity: Entry point that handles the main logging screen and navigation
  - UI components: Display the logging interface, shortcut tray, and settings screens
  - Repository pattern: Manages file operations and data persistence
  - Utils: Helper functions for various operations like file handling

- **Data flow and system interactions**: 
  - User creates or selects a markdown file for logging
  - Users define shortcuts with custom text templates and cursor positions
  - When shortcuts are triggered, the text is inserted at current cursor position in the log file
  - The app automatically saves changes when navigating away from the log screen

## Directory Structure
- **Important directories and their purposes**:
  - `/app/src/main/java/com/app/dailylog`: Main application code with MainActivity and core logic
  - `/app/src/main/res`: Android resources (layouts, drawables, strings)
  - `/app/src/main/AndroidManifest.xml`: App configuration and permissions

- **Key files and configuration**:
  - `build.gradle` (root): Gradle build configuration with Kotlin and Android plugins
  - `settings.gradle`: Project settings including the app module
  - `AndroidManifest.xml`: App permissions and activity configuration

- **Entry points and main modules**:
  - `MainActivity.kt`: Main entry point for the logging interface
  - `com.app.dailylog`: Package containing all core application logic

## Development Workflow
- **How to build/run the project**: 
  - Requires Android Studio or Gradle build system
  - Build using `./gradlew build` command from root directory
  - Run with `./gradlew installDebug` to install on device/emulator

- **Testing approach**: 
  - Unit tests exist in `app/src/test/java` for utility classes like ShortcutUtils
  - Instrumentation tests exist in `app/src/androidTest/java` for UI and integration testing
  - Uses Mockito for mocking dependencies in tests
  - Tests cover shortcut date formatting and datetime parsing functionality

- **Development environment setup**: 
  - Android Studio with Gradle
  - Kotlin SDK 2.2.10 (from build.gradle)
  - Android API level supporting the required storage permissions

- **Lint and format commands**: 
  - Gradle-based build system handles linting
  - Kotlin formatting handled through Gradle/Kotlin plugin configurations