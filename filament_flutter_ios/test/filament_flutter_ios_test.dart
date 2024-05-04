// import 'package:flutter_test/flutter_test.dart';
// import 'package:filament_flutter_ios/src/filament_flutter_ios.dart';
// import 'package:filament_flutter_ios/src/filament_flutter_ios_platform_interface.dart';
// import 'package:filament_flutter_ios/src/filament_flutter_ios_method_channel.dart';
// import 'package:plugin_platform_interface/plugin_platform_interface.dart';
//
// class MockFilamentFlutterIosPlatform
//     with MockPlatformInterfaceMixin
//     implements FilamentFlutterIosPlatform {
//
//   @override
//   Future<String?> getPlatformVersion() => Future.value('42');
// }
//
// void main() {
//   final FilamentFlutterIosPlatform initialPlatform = FilamentFlutterIosPlatform.instance;
//
//   test('$MethodChannelFilamentFlutterIos is the default instance', () {
//     expect(initialPlatform, isInstanceOf<MethodChannelFilamentFlutterIos>());
//   });
//
//   test('getPlatformVersion', () async {
//     FilamentFlutterIos filamentFlutterIosPlugin = FilamentFlutterIos();
//     MockFilamentFlutterIosPlatform fakePlatform = MockFilamentFlutterIosPlatform();
//     FilamentFlutterIosPlatform.instance = fakePlatform;
//
//     expect(await filamentFlutterIosPlugin.getPlatformVersion(), '42');
//   });
// }
