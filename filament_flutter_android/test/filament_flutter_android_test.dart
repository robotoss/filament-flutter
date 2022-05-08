import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:filament_flutter_android/filament_flutter_android.dart';

void main() {
  const MethodChannel channel = MethodChannel('filament_flutter_android');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FilamentFlutterAndroid.platformVersion, '42');
  });
}
