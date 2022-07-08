package com.robotoss.filament_flutter_android

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.opengl.Matrix
import android.os.Build
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.robotoss.filament_flutter_android.helper.AssetHelper
import com.robotoss.filament_flutter_android.model.Mesh
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

import android.view.Choreographer
import android.view.animation.LinearInterpolator
import android.widget.TextView
import com.google.android.filament.*
import java.nio.channels.Channels


@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class FilamentController(
        private val viewId: Int,
        private val context: Context,
        private val activity: Activity,
        private val binaryMessenger: BinaryMessenger,
) : DefaultLifecycleObserver, MethodChannel.MethodCallHandler, PlatformView {

    private val _methodChannel: MethodChannel

    //    private val _textureView: SurfaceView
    private var disposed = false

    // Filament base items:
    // The View we want to render into
    private lateinit var surfaceView: SurfaceView

    // UiHelper is provided by Filament to manage SurfaceView and SurfaceTexture
    private lateinit var uiHelper: UiHelper

    // DisplayHelper is provided by Filament to manage the display
    private lateinit var displayHelper: DisplayHelper

    // Choreographer is used to schedule new frames
    private lateinit var choreographer: Choreographer

    // Engine creates and destroys Filament resources
    // Each engine must be accessed from a single thread of your choosing
    // Resources cannot be shared across engines
    private lateinit var engine: Engine

    // A renderer instance is tied to a single surface (SurfaceView, TextureView, etc.)
    private lateinit var renderer: Renderer

    // A scene holds all the renderable, lights, etc. to be drawn
    private lateinit var scene: Scene

    // A view defines a viewport, a scene and a camera for rendering
    private lateinit var view: com.google.android.filament.View

    // Should be pretty obvious :)
    private lateinit var camera: Camera

    private lateinit var material: Material
    private lateinit var vertexBuffer: VertexBuffer
    private lateinit var indexBuffer: IndexBuffer

    // Filament entity representing a renderable object
    @Entity
    private var renderable = 0

    // A swap chain is Filament's representation of a surface
    private var swapChain: SwapChain? = null

    // Performs the rendering and schedules new frames
    private val frameScheduler = FrameCallback()

    private val animator = ValueAnimator.ofFloat(0.0f, 360.0f)


    //    private lateinit var engine: Engine
//    private lateinit var renderer: Renderer
//    private lateinit var scene: Scene
//    private lateinit var view: com.google.android.filament.View
//    private lateinit var camera: Camera
//
//    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
//    private lateinit var displayHelper: DisplayHelper
    private var _swapChain: SwapChain? = null
//
//    private lateinit var _triangle: Mesh

    init {
        Filament.init()

        MethodChannel(binaryMessenger, FilamentFlutterAndroidPlugin.VIEW_TYPE + '_' + viewId).also {
            _methodChannel = it
            it.setMethodCallHandler(this)
        }

        surfaceView = SurfaceView(context)

        choreographer = Choreographer.getInstance()

        displayHelper = DisplayHelper(context)



        setupSurfaceView()
        setupFilament()
        setupView()
        setupScene()


    }



    private fun setupSurfaceView() {
        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
        uiHelper.renderCallback = SurfaceCallback()

        // NOTE: To choose a specific rendering resolution, add the following line:
        // uiHelper.setDesiredSize(1280, 720)
        uiHelper.attachTo(surfaceView)
    }

    private fun setupFilament() {
        engine = Engine.create()
        renderer = engine.createRenderer()
        scene = engine.createScene()
        view = engine.createView()
        camera = engine.createCamera(engine.entityManager.create())
    }

    private fun setupView() {
        scene.skybox = Skybox.Builder().color(0.035f, 0.035f, 0.035f, 1.0f).build(engine)

        // NOTE: Try to disable post-processing (tone-mapping, etc.) to see the difference
        // view.isPostProcessingEnabled = false

        // Tell the view which camera we want to use
        view.camera = camera

        // Tell the view which scene we want to render
        view.scene = scene
    }

    private fun setupScene() {
        val materialBuffer = AssetHelper.readMaterial(context, "baked_color.filamat")

        material = Material.Builder()
                .payload(materialBuffer, materialBuffer.remaining())
                .build(engine)


        createMesh()

        // To create a renderable we first create a generic entity
        renderable = EntityManager.get().create()

        // We then create a renderable component on that entity
        // A renderable is made of several primitives; in this case we declare only 1
        RenderableManager.Builder(1)
                // Overall bounding box of the renderable
                .boundingBox(Box(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.01f))
                // Sets the mesh data of the first primitive
                .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBuffer, indexBuffer, 0, 3)
                // Sets the material of the first primitive
                .material(0, material.defaultInstance)
                .build(engine, renderable)

        // Add the entity to the scene to render it
        scene.addEntity(renderable)

        startAnimation()
    }


    private fun createMesh() {
        val intSize = 4
        val floatSize = 4
        val shortSize = 2
        // A vertex is a position + a color:
        // 3 floats for XYZ position, 1 integer for color
        val vertexSize = 3 * floatSize + intSize

        // Define a vertex and a function to put a vertex in a ByteBuffer
        data class Vertex(val x: Float, val y: Float, val z: Float, val color: Int)

        fun ByteBuffer.put(v: Vertex): ByteBuffer {
            putFloat(v.x)
            putFloat(v.y)
            putFloat(v.z)
            putInt(v.color)
            return this
        }

        // We are going to generate a single triangle
        val vertexCount = 3
        val a1 = PI * 2.0 / 3.0
        val a2 = PI * 4.0 / 3.0

        val vertexData = ByteBuffer.allocate(vertexCount * vertexSize)
                // It is important to respect the native byte order
                .order(ByteOrder.nativeOrder())
                .put(Vertex(1.0f, 0.0f, 0.0f, 0xffff0000.toInt()))
                .put(Vertex(cos(a1).toFloat(), sin(a1).toFloat(), 0.0f, 0xff00ff00.toInt()))
                .put(Vertex(cos(a2).toFloat(), sin(a2).toFloat(), 0.0f, 0xff0000ff.toInt()))
                // Make sure the cursor is pointing in the right place in the byte buffer
                .flip()

        // Declare the layout of our mesh
        vertexBuffer = VertexBuffer.Builder()
                .bufferCount(1)
                .vertexCount(vertexCount)
                // Because we interleave position and color data we must specify offset and stride
                // We could use de-interleaved data by declaring two buffers and giving each
                // attribute a different buffer index
                .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, vertexSize)
                .attribute(VertexBuffer.VertexAttribute.COLOR, 0, VertexBuffer.AttributeType.UBYTE4, 3 * floatSize, vertexSize)
                // We store colors as unsigned bytes but since we want values between 0 and 1
                // in the material (shaders), we must mark the attribute as normalized
                .normalized(VertexBuffer.VertexAttribute.COLOR)
                .build(engine)

        // Feed the vertex data to the mesh
        // We only set 1 buffer because the data is interleaved
        vertexBuffer.setBufferAt(engine, 0, vertexData)

        // Create the indices
        val indexData = ByteBuffer.allocate(vertexCount * shortSize)
                .order(ByteOrder.nativeOrder())
                .putShort(0)
                .putShort(1)
                .putShort(2)
                .flip()

        indexBuffer = IndexBuffer.Builder()
                .indexCount(3)
                .bufferType(IndexBuffer.Builder.IndexType.USHORT)
                .build(engine)
        indexBuffer.setBuffer(engine, indexData)
    }

    private fun startAnimation() {
        // Animate the triangle
        animator.interpolator = LinearInterpolator()
        animator.duration = 4000
        animator.repeatMode = ValueAnimator.RESTART
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            val transformMatrix = FloatArray(16)
            override fun onAnimationUpdate(a: ValueAnimator) {
                Matrix.setRotateM(transformMatrix, 0, -(a.animatedValue as Float), 0.0f, 0.0f, 1.0f)
                val tcm = engine.transformManager
                tcm.setTransform(tcm.getInstance(renderable), transformMatrix)
            }
        })
        animator.start()
    }


    // PlatformView

    override fun onFlutterViewAttached(flutterView: View) {
        setupSurfaceView()
        setupFilament()
        setupView()
        setupScene()

    }


    override fun getView(): View {
//        var textView = TextView(context)
//        textView.textSize = 72f
//        textView.setBackgroundColor(Color.rgb(255, 255, 255))
//        textView.text = "Rendered on a native Android view (id: 2)"
//        return  textView




        return surfaceView
    }

    override fun dispose() {
        if (disposed) return

        _methodChannel.setMethodCallHandler(null)
        // Stop the animation and any pending frame
        choreographer.removeFrameCallback(frameScheduler)
        animator.cancel();

        // Always detach the surface before destroying the engine
        uiHelper.detach()

        // Cleanup all resources
        engine.destroyEntity(renderable)
        engine.destroyRenderer(renderer)
        engine.destroyVertexBuffer(vertexBuffer)
        engine.destroyIndexBuffer(indexBuffer)
        engine.destroyMaterial(material)
        engine.destroyView(view)
//        engine.destroyScene_(scene)
        engine.destroyCameraComponent(camera.entity)

        // Engine.destroyEntity() destroys Filament related resources only
        // (components), not the entity itself
        val entityManager = EntityManager.get()
        entityManager.destroy(renderable)
        entityManager.destroy(camera.entity)

        // Destroying the engine will free up any resource you may have forgotten
        // to destroy, but it's recommended to do the cleanup properly
        engine.destroy()

        disposed = true
    }

    // MethodCallHandler

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            else -> Unit
        }
    }

    inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            // Schedule the next frame
            choreographer.postFrameCallback(frameScheduler)

            // This check guarantees that we have a swap chain
            if (uiHelper.isReadyToRender) {
                // If beginFrame() returns false you should skip the frame
                // This means you are sending frames too quickly to the GPU
                if (renderer.beginFrame(swapChain!!, frameTimeNanos)) {
                    renderer.render(view)
                    renderer.endFrame()
                }
            }
        }
    }

    // DefaultLifecycleObserver

    inner class SurfaceCallback : UiHelper.RendererCallback {
        @SuppressLint("NewApi")
        override fun onNativeWindowChanged(surface: Surface) {
            _swapChain?.let { engine.destroySwapChain(it) }
            _swapChain = engine.createSwapChain(surface, uiHelper.swapChainFlags)
            displayHelper.attach(renderer, surfaceView.display);
        }

        override fun onDetachedFromSurface() {
            displayHelper.detach();
            _swapChain?.let {
                engine.destroySwapChain(it)
                // Required to ensure we don't return before Filament is done executing the
                // destroySwapChain command, otherwise Android might destroy the Surface
                // too early
                engine.flushAndWait()
                _swapChain = null
            }
        }

        override fun onResized(width: Int, height: Int) {
            val zoom = 1.5
            val aspect = width.toDouble() / height.toDouble()
            camera.setProjection(Camera.Projection.ORTHO,
                    -aspect * zoom, aspect * zoom, -zoom, zoom, 0.0, 10.0)

            view.viewport = Viewport(0, 0, width, height)
        }
    }
}