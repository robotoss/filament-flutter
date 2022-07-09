import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

typedef FilamentViewCreatedCallback = void Function(FilamentViewController);

class FilamentView extends StatefulWidget {
  const FilamentView({
    Key? key,
    required this.onFilamentViewViewCreated,
  }) : super(key: key);

  final FilamentViewCreatedCallback onFilamentViewViewCreated;

  @override
  _FilamentViewState createState() => _FilamentViewState();
}

class _FilamentViewState extends State<FilamentView> {
  // This is used in the platform side to register the view.
  static const String viewType = 'flutter_filament_plugin.view';

  late MethodChannel _channel;

  // Pass parameters to the platform side.
  static const Map<String, dynamic> creationParams = <String, dynamic>{
    'helmetModel': 'FlightHelmet',
    'droneModel': 'BusterDrone'
  };

  void _onPlatformViewCreated(int id) {
    _channel = MethodChannel('${viewType}_$id');

    // Start scan after creation of the view
    final controller = FilamentViewController._(_channel);

    // Initialize the controller for controlling the QRView
    widget.onFilamentViewViewCreated(controller);
  }

  @override
  Widget build(BuildContext context) {
    return PlatformViewLink(
      viewType: viewType,
      surfaceFactory: (context, controller) {
        return AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      onCreatePlatformView: (params) {
        _onPlatformViewCreated(params.id);
        return PlatformViewsService.initSurfaceAndroidView(
          id: params.id,
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
          onFocus: () {
            params.onFocusChanged(true);
          },
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
    );
  }
}

class FilamentViewController {
  FilamentViewController._(MethodChannel channel) : _channel = channel;

  final MethodChannel _channel;

  /// Change 3D model
  Future<void> changeModel({required String modelName}) async {
    try {
      return await _channel.invokeMethod('change3DModel', {
        'modelName': modelName,
      });
    } catch (e) {
      rethrow;
    }
  }
}
