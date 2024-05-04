import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'filament_flutter_ios_platform_interface.dart';

/// An implementation of [FilamentFlutterIosPlatform] that uses method channels.
class MethodChannelFilamentFlutterIos extends FilamentFlutterIosPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('filament_flutter_ios');

  @override
  Future<void> changeModel({required String modelName}) async {
    await methodChannel.invokeMethod<String>(
      'change3DModel',
      {
        'modelName': modelName,
      },
    );
  }
}
