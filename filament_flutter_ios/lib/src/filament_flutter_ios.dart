import 'filament_flutter_ios_platform_interface.dart';

class FilamentViewController {
  /// Change 3D model
  Future<void> changeModel({required String modelName}) async {
    try {
      return FilamentFlutterIosPlatform.instance.changeModel(
        modelName: modelName,
      );
    } catch (e) {
      rethrow;
    }
  }
}
