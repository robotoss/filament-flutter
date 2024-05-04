import 'package:filament_flutter_ios/filament_flutter_ios.dart';
import 'package:flutter/material.dart';

const _droneModel = 'BusterDrone';
const _helmetModel = 'FlightHelmet';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _activeModel = _droneModel;

  late final FilamentViewController _controller;

  void changeModelPressed() {
    if (_activeModel == _droneModel) {
      _activeModel = _helmetModel;
    } else {
      _activeModel = _droneModel;
    }
    _controller.changeModel(modelName: _activeModel);
  }

  @override
  void initState() {
    super.initState();

    if (!mounted) return;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Filament View'),
        ),
        body: FilamentView(
          onFilamentViewViewCreated: (controller) {
            _controller = controller;
          },
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: changeModelPressed,
          child: const Center(child: Icon(Icons.swap_horiz)),
        ),
      ),
    );
  }
}
