# flutter_screen
A Flutter plugin to manage the device's screen on Android and iOS, Based on [clovisnicolas
/
flutter_screen](https://github.com/clovisnicolas/flutter_screen)

## Usage
To use this plugin, add screen as a dependency in your pubspec.yaml file.

Make sure you add the following permissions to your Android Manifest

```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## Example
```dart
// Import package
import 'package:screen/screen.dart';

// Get the current brightness:
double brightness = await Screen.brightness;

// Set the brightness:
Screen.setBrightness(0.5);

// Check if the screen is kept on:
bool isKeptOn = await Screen.isKeptOn;

// Prevent screen from going into sleep mode:
Screen.keepOn(true);
```
