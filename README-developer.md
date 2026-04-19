# dailyLog Developer Documentation

This document provides instructions for setting up a development environment and building the dailyLog application.

## Prerequisites

- Android Studio (recommended) or command-line tools
- JDK 17 (required for building)
- Kotlin SDK 2.2.10 (as specified in build.gradle)
- Android SDK with API level 36
- Gradle 8.0 or higher

## Setting Up Development Environment Using Android Studio

Download the latest version of Android Studio.

1. **Clone the repository**:
   ```bash
   git clone https://github.com/madCode/dailylog.git
   cd dailylog
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned repository directory

3. **Sync the project**:
   - Android Studio will automatically sync Gradle files
   - If prompted, accept any SDK or build tool updates

4. **Run all tests:**
   - Under `app/src` locate the two test folders: `test` (unit tests) and `androidTest` (instrumentation tests).
   - In Android Studio, right click on each folder and click on "Run 'All Tests'" (Ctrl + Shift + R)
   - Ensure all tests pass
   - Common issues:
     - If it's been a while since the app was updated, the current Gradle version may be incompatible with the latest version of Android Studio.
     - Android Studio won't build the app until this is resolved, this may impact running tests or building the debug version (step 5)
     - To resolve, follow the instructions in Android Studio to upgrade the Gradle and relevant library versions and put up a pull request.

5. **Debug build the project**:
   - In the top right-hand corner in Android studio:
      - select a device
      - select "app"
      - click on the green play button
   - Ensure the app builds with no errors
      - the debug apk is stored in `app/build/outputs/apk/debug/app-debug.apk`
   - In the device, play around with the app

6. **madCode only**: Set up signing key
   - Search for the `dailylog.jks` file.

## Releasing a New App Version

### Part 1: Creating an F-Droid Compatible New Release

To create a new release compatible with F-Droid, follow these steps:

1. **Update version information in `app/build.gradle`**:
   ```gradle
   defaultConfig {
       applicationId "com.app.dailylog"
       minSdkVersion 23
       targetSdkVersion 36
       versionCode 3001  // Increment this number
       versionName "3.0.1"  // Update to new semantic version
   }
   ```

2. **Commit your changes**:
   ```bash
   git add app/build.gradle
   git commit -m "Release version 3.0.1"
   ```

3. **Create a Git tag**:
   ```bash
   git tag -a v3.0.1 -m "Release version 3.0.1"
   git push origin v3.0.1
   ```

4. **Build the release APK**:
   - In Android Studio: Build > Generate Signed App Bundle or APK > APK
   - **Important:** F-Droid only uses APKs *not* App Bundles. Always select APK for your release build.

### Part 2: Publish The Release on Github

When creating a new release on GitHub:

1. **Navigate to Releases page**:
   - Go to your GitHub repository's "Releases" tab
   - Click "Create a new release"

2. **Release details**:
   - **Tag version**: Use the same tag you created (e.g., `v3.0.1`)
   - **Release title**: Use the same version name (e.g., `dailyLog 3.0.1`)
   - **Description**: Add release notes and changelog information

3. **Uploading the APK**:
   - **Upload the release APK**: Attach `app/build/outputs/apk/release/app-release.apk` and `output.json`
   - **F-Droid will verify the signed APK** - Since the app is signed, F-Droid will check that it matches the reproducible build, then serve the Github version.

4. **Publish the release**:
   - Click "Publish release" to make it available on GitHub

### Part 3: Ensuring a Successful F-Droid Release

You can read about F-Droid's update process [here](https://gitlab.com/fdroid/wiki/-/wikis/FAQ#finding-updates). This page also links to where you can see the latest builds and releases.

After publishing a new release, wait at least 24 hours, then check the following:
1. The [DailyLog F-Droid page](https://f-droid.org/packages/com.app.dailylog/): see if the version at the bottom matches the new version.
2. If not, check the [metadata file](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.app.dailylog.yml?ref_type=heads): see if F-Droid successfully updated the version number.
3. If not, search [existing dailyLog merge requests](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/?sort=created_date&state=opened&search=dailyLog&first_page_size=20): see if the check-updates bot had issues and if anyone is working on it

## Testing

### Running Unit Tests
```bash
./gradlew test
```

### Running Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

## Code Style and Formatting

### Kotlin Formatting
- Uses Kotlin 2.2.10 (from build.gradle)
- Follows Kotlin code style "official" as specified in gradle.properties

### Android Project Structure
The project follows standard Android conventions:
- `app/src/main/java/com/app/dailylog/` - Main application code
- `app/src/main/res/` - Resources (layouts, drawables, strings)
- `app/src/test/java/` - Unit tests
- `app/src/androidTest/java/` - Instrumentation tests

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes following the code style
4. Write tests for new functionality
5. Run all tests to ensure no regressions
6. Submit a pull request

## Troubleshooting

### Common Issues

1. **Build failures with Java version**:
   - Ensure JDK 17 is set as default
   - Check `JAVA_HOME` environment variable

2. **Permission issues with external storage**:
   - The app requires read/write permissions to external storage
   - These are properly declared in AndroidManifest.xml

3. **F-Droid build errors**:
   - Ensure all dependencies are compatible with F-Droid's build environment
   - Verify the app builds with openjdk-17-jdk-headless