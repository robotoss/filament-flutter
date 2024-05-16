# Flutter Filament

## Enhanced 3D Graphics with Flutter & Filament

Flutter Filament demonstrates the integration of the Filament rendering engine with Flutter to enable real-time, physically-based 3D rendering. This project is an excellent resource for developers looking to leverage advanced graphics in their Flutter applications.

## Features

- **Real-time 3D Rendering:** Utilize Filament for stunning visual effects.
- **Image-Based Lighting (IBL):** Render 3D models with realistic lighting and shadows.

Special thanks to the Filament team. For more details, visit the [Filament Repository](https://github.com/google/filament).

## Preview

<p float="left">
  <img src="https://github.com/robotoss/filament-flutter/blob/main/images/drone_screen.png?raw=true" width="250">
  <img src="https://github.com/robotoss/filament-flutter/blob/main/images/helmet_screen.png?raw=true" width="250">
</p>

## Getting Started

### Requirements

- **Kotlin Version:** 1.9.0 or higher
- **Minimum SDK Version:** 21
- **Gradle Version:** 7.6.3 or higher

### Installation

#### 🤖 Android

1. **Clone and Navigate:** Clone the repository and navigate to `filament_flutter_android/example`.

2. **Update Dependencies:** Update `build.gradle` in the `/android` directory to change the Filament version:
    ```gradle
    dependencies {
        implementation 'com.google.android.filament:filament-android:1.51.2'
        implementation 'com.google.android.filament:gltfio-android:1.51.2'
        implementation 'com.google.android.filament:filament-utils-android:1.51.2'
    }
    ```

3. **Rebuild the Project:**
   - Download the appropriate Filament version from [Google Filament Releases](https://github.com/google/filament/releases) for your OS.
   - Navigate to `filament_flutter_android/example/android/app/src/main/assets/envs`.
   - Generate environment assets using the downloaded Filament binaries:
     ```sh
     path_to_downloaded_filament/bin/cmgen -x default_env --format=ktx --size=256 --extract-blur=0.1 lightroom_14b.hdr
     ```

### Project Structure

- **3D Models:** Located at `filament_flutter_android/example/android/app/src/main/assets/models`.
- **Skybox Environments:** Stored in `filament_flutter_android/example/android/app/src/main/assets/envs`.

#### 🍏 iOS

Ensure you have added the following parameters to the Build Settings of the project:

- Linking General => Other Linker Flag => `-lstdc++`
- Apple Clang - Language - C++ => C Language Dialect => `GNU++17 [std=gnu++17]`

The project already includes pre-built Filament libraries. You can also build new ones when new Filament versions are released.


## Support

For support, please open an issue in the [GitHub repository](https://github.com/robotoss/filament-flutter/issues).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---