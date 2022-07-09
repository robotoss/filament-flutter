//
// import 'dart:async';
//
// import 'package:flutter/services.dart';
//
// class FilamentFlutterAndroid {
//   static const MethodChannel _channel = MethodChannel('filament_flutter_android');
//
//   static Future<String?> get platformVersion async {
//     final String? version = await _channel.invokeMethod('getPlatformVersion');
//     return version;
//   }
// }

library filament_flutter_android;

export 'src/filament_view.dart';
