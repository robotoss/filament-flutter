//
//  FilamentView.m
//  filament_flutter_ios
//
//  Created by Konstantin Eftifeyev on 04/05/2024.
//

#import "FilamentView.h"

#include "FilamentView.h"

#include <filament/Camera.h>
#include <filament/Engine.h>
#include <filament/IndexBuffer.h>
#include <filament/Material.h>
#include <filament/RenderableManager.h>
#include <filament/Renderer.h>
#include <filament/Scene.h>
#include <filament/TransformManager.h>
#include <filament/VertexBuffer.h>
#include <filament/View.h>
#include <filament/Viewport.h>

#include <utils/EntityManager.h>

using namespace filament;
using utils::Entity;
using utils::EntityManager;

@implementation FilamentViewFactory {
    NSObject<FlutterBinaryMessenger>* _messenger;
}

- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    self = [super init];
    if (self) {
        _messenger = messenger;
    }
    return self;
}

- (NSObject<FlutterPlatformView>*)createWithFrame:(CGRect)frame
                                   viewIdentifier:(int64_t)viewId
                                        arguments:(id _Nullable)args {
    return [[FilamentView alloc] initWithFrame:frame
                                viewIdentifier:viewId
                                     arguments:args
                               binaryMessenger:_messenger];
}

/// Implementing this method is only necessary when the `arguments` in `createWithFrame` is not `nil`.
- (NSObject<FlutterMessageCodec>*)createArgsCodec {
    return [FlutterStandardMessageCodec sharedInstance];
}

@end

struct App {
    VertexBuffer* vb;
    IndexBuffer* ib;
    Material* mat;
    Entity renderable;
};

struct Vertex {
    filament::math::float2 position;
    uint32_t color;
};

static const Vertex TRIANGLE_VERTICES[3] = {
    {{1, 0}, 0xffff0000u},
    {{cos(M_PI * 2 / 3), sin(M_PI * 2 / 3)}, 0xff00ff00u},
    {{cos(M_PI * 4 / 3), sin(M_PI * 4 / 3)}, 0xff0000ffu},
};

static constexpr uint16_t TRIANGLE_INDICES[3] = { 0, 1, 2 };

// This file is compiled via the matc tool. See the "Run Script" build phase.
static constexpr uint8_t BAKED_COLOR_PACKAGE[] = {
#include "../Materials/bakedColor.mat"
};


@implementation FilamentView {
    UIView *_view;
    Engine* engine;
    Renderer* renderer;
    Scene* scene;
    View* filaView;
    Camera* camera;
    SwapChain* swapChain;
    App app;
    CADisplayLink* displayLink;

    // The amount of rotation to apply to the camera to offset the device's rotation (in radians)
    float deviceRotation;
    float desiredRotation;
}

- (instancetype)initWithFrame:(CGRect)frame
               viewIdentifier:(int64_t)viewId
                    arguments:(id _Nullable)args
              binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
    if (self = [super init]) {
        _view = [[UIView alloc] init];
        engine = Engine::create(Engine::Backend::METAL);
        
        ///
        swapChain = engine->createSwapChain((__bridge void*) self.layer);
         renderer = engine->createRenderer();
         scene = engine->createScene();
         Entity c = EntityManager::get().create();
         camera = engine->createCamera(c);
         renderer->setClearOptions({.clearColor={0.1, 0.125, 0.25, 1.0}, .clear = true});

         filaView = engine->createView();
         filaView->setPostProcessingEnabled(false);

         app.vb = VertexBuffer::Builder()
             .vertexCount(3)
             .bufferCount(1)
             .attribute(VertexAttribute::POSITION, 0, VertexBuffer::AttributeType::FLOAT2, 0, 12)
             .attribute(VertexAttribute::COLOR, 0, VertexBuffer::AttributeType::UBYTE4, 8, 12)
             .normalized(VertexAttribute::COLOR)
             .build(*engine);
         app.vb->setBufferAt(*engine, 0,
                             VertexBuffer::BufferDescriptor(TRIANGLE_VERTICES, 36, nullptr));

         app.ib = IndexBuffer::Builder()
             .indexCount(3)
             .bufferType(IndexBuffer::IndexType::USHORT)
             .build(*engine);
         app.ib->setBuffer(*engine,
                           IndexBuffer::BufferDescriptor(TRIANGLE_INDICES, 6, nullptr));

         app.mat = Material::Builder()
             .package((void*) BAKED_COLOR_PACKAGE, sizeof(BAKED_COLOR_PACKAGE))
             .build(*engine);

         app.renderable = EntityManager::get().create();
         RenderableManager::Builder(1)
             .boundingBox({{ -1, -1, -1 }, { 1, 1, 1 }})
             .material(0, app.mat->getDefaultInstance())
             .geometry(0, RenderableManager::PrimitiveType::TRIANGLES, app.vb, app.ib, 0, 3)
             .culling(false)
             .receiveShadows(false)
             .castShadows(false)
             .build(*engine, app.renderable);
         scene->addEntity(app.renderable);

         filaView->setScene(scene);
         filaView->setCamera(camera);
         CGRect nativeBounds = [UIScreen mainScreen].nativeBounds;
         filaView->setViewport(Viewport(0, 0, nativeBounds.size.width, nativeBounds.size.height));
    }
    return self;
}

- (UIView*)view {
    return _view;
}


@end
