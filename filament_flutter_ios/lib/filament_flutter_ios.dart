
import 'filament_flutter_ios_platform_interface.dart';

class FilamentFlutterIos {
  Future<String?> getPlatformVersion() {
    return FilamentFlutterIosPlatform.instance.getPlatformVersion();
  }
}
