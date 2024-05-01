import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'filament_flutter_ios_method_channel.dart';

abstract class FilamentFlutterIosPlatform extends PlatformInterface {
  /// Constructs a FilamentFlutterIosPlatform.
  FilamentFlutterIosPlatform() : super(token: _token);

  static final Object _token = Object();

  static FilamentFlutterIosPlatform _instance = MethodChannelFilamentFlutterIos();

  /// The default instance of [FilamentFlutterIosPlatform] to use.
  ///
  /// Defaults to [MethodChannelFilamentFlutterIos].
  static FilamentFlutterIosPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FilamentFlutterIosPlatform] when
  /// they register themselves.
  static set instance(FilamentFlutterIosPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
