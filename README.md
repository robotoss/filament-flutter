# Flutter Filament
## _Flutter with real-time physically based rendering engine_


This project demonstrates the capabilities of filament with the use in flutter.


## Features

- Render 3D model with HDR

Many thanks to the filament team for this. You can always check out filament in the original [repository]('https://github.com/google/filament')



## Run
### 🤖 Android

To run the project on Android at this point, you need to run the flutter project in `filament_flutter_android/example`.
Current use of filament version `1.25.0`, To change the filament version, change the /android/build.gradle dependencies:

```sh
dependencies {
    implementation 'com.google.android.filament:filament-android:1.25.0'
    implementation 'com.google.android.filament:gltfio-android:1.25.0'
    implementation 'com.google.android.filament:filament-utils-android:1.25.0'
}
```
If you have changed the filament version, it is advisable to rebuild the project files:
1) Download the filmaent version as indicated in the android dependencies, for your operating system https://github.com/google/filament/releases
2) Go to the terminal and open the `filament_flutter_android/example/android/app/src/main/assets/envs` folder
3) Run generator:

```sh
dowloaded_filament_folder/bin/cmgen -x default_env --format=ktx --size=256 --extract-blur=0.1 --extract-blur=0.1 lightroom_14b.hdr
```

Project files:
- `3D models` - filament_flutter_android/example/android/app/src/main/assets/models
- `Sky box` - filament_flutter_android/example/android/app/src/main/assets/envs
