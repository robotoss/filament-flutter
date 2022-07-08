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
    private var _disposed = false

    // Filament base items:
    // The View we want to render into
    private lateinit var _surfaceView: SurfaceView

    // UiHelper is provided by Filament to manage SurfaceView and SurfaceTexture
    private lateinit var _uiHelper: UiHelper

    // DisplayHelper is provided by Filament to manage the display
    private lateinit var _displayHelper: DisplayHelper

    // Choreographer is used to schedule new frames
    private lateinit var _choreographer: Choreographer

    // Engine creates and destroys Filament resources
    // Each engine must be accessed from a single thread of your choosing
    // Resources cannot be shared across engines
    private lateinit var _engine: Engine

    // A renderer instance is tied to a single surface (SurfaceView, TextureView, etc.)
    private lateinit var _renderer: Renderer

    // A _scene holds all the renderable, lights, etc. to be drawn
    private lateinit var _scene: Scene

    // A view defines a viewport, a _scene and a camera for rendering
    private lateinit var _view: com.google.android.filament.View

    // Should be pretty obvious :)
    private lateinit var _camera: Camera

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


    //    private lateinit var _engine: Engine
//    private lateinit var _renderer: Renderer
//    private lateinit var _scene: Scene
//    private lateinit var _view: com.google.android.filament.View
//    private lateinit var _camera: Camera
//
//    private val _uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
//    private lateinit var _displayHelper: DisplayHelper
    private var _swapChain: SwapChain? = null
//
//    private lateinit var _triangle: Mesh

    init {
        Filament.init()

        MethodChannel(binaryMessenger, FilamentFlutterAndroidPlugin.VIEW_TYPE + '_' + viewId).also {
            _methodChannel = it
            it.setMethodCallHandler(this)
        }

        _surfaceView = SurfaceView(context)

        _choreographer = Choreographer.getInstance()

        _displayHelper = DisplayHelper(context)



        setupSurfaceView()
        setupFilament()
        setupView()
        setupScene()


    }



    private fun setupSurfaceView() {
        _uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
        _uiHelper.renderCallback = SurfaceCallback()

        // NOTE: To choose a specific rendering resolution, add the following line:
        // _uiHelper.setDesiredSize(1280, 720)
        _uiHelper.attachTo(_surfaceView)
    }

    private fun setupFilament() {
        _engine = Engine.create()
        _renderer = _engine.createRenderer()
        _scene = _engine.createScene()
        _view = _engine.createView()
        _camera = _engine.createCamera(_engine.entityManager.create())
    }

    private fun setupView() {
        _scene.skybox = Skybox.Builder().color(0.035f, 0.035f, 0.035f, 1.0f).build(_engine)

        // NOTE: Try to disable post-processing (tone-mapping, etc.) to see the difference
        // view.isPostProcessingEnabled = false

        // Tell the view which camera we want to use
        _view.camera = _camera

        // Tell the view which _scene we want to render
        _view.scene = _scene
    }

    private fun setupScene() {
        val materialBuffer = AssetHelper.readMaterial(context, "baked_color.filamat")

        material = Material.Builder()
                .payload(materialBuffer, materialBuffer.remaining())
                .build(_engine)


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
                .build(_engine, renderable)

        // Add the entity to the _scene to render it
        _scene.addEntity(renderable)

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
                .build(_engine)

        // Feed the vertex data to the mesh
        // We only set 1 buffer because the data is interleaved
        vertexBuffer.setBufferAt(_engine, 0, vertexData)

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
                .build(_engine)
        indexBuffer.setBuffer(_engine, indexData)
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
                val tcm = _engine.transformManager
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




        return _surfaceView
    }

    override fun dispose() {
        if (_disposed) return

        _methodChannel.setMethodCallHandler(null)
        // Stop the animation and any pending frame
        _choreographer.removeFrameCallback(frameScheduler)
        animator.cancel();

        // Always detach the surface before destroying the engine
        _uiHelper.detach()

        // Cleanup all resources
        _engine.destroyEntity(renderable)
        _engine.destroyRenderer(_renderer)
        _engine.destroyVertexBuffer(vertexBuffer)
        _engine.destroyIndexBuffer(indexBuffer)
        _engine.destroyMaterial(material)
        _engine.destroyView(_view)
//        _engine.destroyScene_(scene)
        _engine.destroyCameraComponent(_camera.entity)

        // Engine.destroyEntity() destroys Filament related resources only
        // (components), not the entity itself
        val entityManager = EntityManager.get()
        entityManager.destroy(renderable)
        entityManager.destroy(_camera.entity)

        // Destroying the engine will free up any resource you may have forgotten
        // to destroy, but it's recommended to do the cleanup properly
        _engine.destroy()

        _disposed = true
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
            _choreographer.postFrameCallback(frameScheduler)

            // This check guarantees that we have a swap chain
            if (_uiHelper.isReadyToRender) {
                // If beginFrame() returns false you should skip the frame
                // This means you are sending frames too quickly to the GPU
                if (_renderer.beginFrame(swapChain!!, frameTimeNanos)) {
                    _renderer.render(_view)
                    _renderer.endFrame()
                }
            }
        }
    }

    // DefaultLifecycleObserver

    inner class SurfaceCallback : UiHelper.RendererCallback {
        @SuppressLint("NewApi")
        override fun onNativeWindowChanged(surface: Surface) {
            _swapChain?.let { _engine.destroySwapChain(it) }
            _swapChain = _engine.createSwapChain(surface, _uiHelper.swapChainFlags)
            _displayHelper.attach(_renderer, _surfaceView.display);
        }

        override fun onDetachedFromSurface() {
            _displayHelper.detach();
            _swapChain?.let {
                _engine.destroySwapChain(it)
                // Required to ensure we don't return before Filament is done executing the
                // destroySwapChain command, otherwise Android might destroy the Surface
                // too early
                _engine.flushAndWait()
                _swapChain = null
            }
        }

        override fun onResized(width: Int, height: Int) {
            val zoom = 1.5
            val aspect = width.toDouble() / height.toDouble()
            _camera.setProjection(Camera.Projection.ORTHO,
                    -aspect * zoom, aspect * zoom, -zoom, zoom, 0.0, 10.0)

            _view.viewport = Viewport(0, 0, width, height)
        }
    }
}