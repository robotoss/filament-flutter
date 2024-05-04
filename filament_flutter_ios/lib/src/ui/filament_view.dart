import 'package:filament_flutter_ios/src/filament_flutter_ios.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef FilamentViewCreatedCallback = void Function(FilamentViewController);

class FilamentView extends StatefulWidget {
  const FilamentView({
    super.key,
    required this.onFilamentViewViewCreated,
  });

  final FilamentViewCreatedCallback onFilamentViewViewCreated;

  @override
  State<FilamentView> createState() => _FilamentViewState();
}

class _FilamentViewState extends State<FilamentView> {
  // This is used in the platform side to register the view.
  static const String viewType = 'flutter_filament_plugin.view';

  // Pass parameters to the platform side.
  static const Map<String, dynamic> creationParams = <String, dynamic>{
    'helmetModel': 'FlightHelmet',
    'droneModel': 'BusterDrone'
  };

  void _onPlatformViewCreated(int id) {
    // Start scan after creation of the view
    final controller = FilamentViewController();

    // Initialize the controller for controlling the QRView
    widget.onFilamentViewViewCreated(controller);
  }

  @override
  Widget build(BuildContext context) {
    return UiKitView(
      viewType: viewType,
      layoutDirection: TextDirection.ltr,
      creationParams: creationParams,
      creationParamsCodec: const StandardMessageCodec(),
      onPlatformViewCreated: (id) {
        _onPlatformViewCreated(id);
      },
    );
  }
}
