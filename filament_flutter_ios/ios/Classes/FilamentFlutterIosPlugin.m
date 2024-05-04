#import "FilamentFlutterIosPlugin.h"
#import "FilamentView.h"

@implementation FilamentFlutterIosPlugin
+ (void)registerWithRegistrar:(NSObject <FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel = [FlutterMethodChannel
            methodChannelWithName:@"filament_flutter_ios"
                  binaryMessenger:[registrar messenger]];
    FilamentFlutterIosPlugin *instance = [[FilamentFlutterIosPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
    
    // Register platform view
    FilamentViewFactory *factory =
            [[FilamentViewFactory alloc] initWithMessenger:registrar.messenger];
    [registrar registerViewFactory:factory withId:@"flutter_filament_plugin.view"];
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    if ([@"getPlatformVersion" isEqualToString:call.method]) {
        result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
    } else {
        result(FlutterMethodNotImplemented);
    }
}

@end
