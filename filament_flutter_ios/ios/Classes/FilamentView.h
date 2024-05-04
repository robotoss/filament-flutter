//
//  FilamentView.h
//  Pods
//
//  Created by Konstantin Eftifeyev on 04/05/2024.
//

#import <Flutter/Flutter.h>

@interface FilamentViewFactory : NSObject <FlutterPlatformViewFactory>
- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger>*)messenger;
@end

@interface FilamentView : NSObject <FlutterPlatformView>

- (instancetype)initWithFrame:(CGRect)frame
        viewIdentifier:(int64_t)viewId
        arguments:(id _Nullable)args
        binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger;

- (UIView*)view;
@end
