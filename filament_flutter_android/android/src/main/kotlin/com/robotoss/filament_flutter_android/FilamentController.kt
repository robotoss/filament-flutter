package com.robotoss.filament_flutter_android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import com.google.android.filament.Box
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.Filament
import com.google.android.filament.IndexBuffer
import com.google.android.filament.Material
import com.google.android.filament.RenderableManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.SwapChain
import com.google.android.filament.VertexBuffer
import com.google.android.filament.Viewport
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


class FilamentController(
        private val viewId: Int,
        private val context: Context,
        private val activity: Activity,
        private val binaryMessenger: BinaryMessenger,
) : DefaultLifecycleObserver, MethodChannel.MethodCallHandler, PlatformView {

    private val _methodChannel: MethodChannel
    private val _textureView: SurfaceView
    private var _disposed = false

    // Filament base items:

    private lateinit var _engine: Engine
    private lateinit var _renderer: Renderer
    private lateinit var _scene: Scene
    private lateinit var _view: com.google.android.filament.View
    private lateinit var _camera: Camera

    private val _uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private lateinit var _displayHelper: DisplayHelper
    private var _swapChain: SwapChain? = null

    private lateinit var _triangle: Mesh

    init {
        Filament.init()

        MethodChannel(binaryMessenger, FilamentFlutterAndroidPlugin.VIEW_TYPE + '_' + viewId).also {
            _methodChannel = it
            it.setMethodCallHandler(this)
        }

        SurfaceView(context).also {
            _textureView = it
        }
    }

    private fun setupFilament() {
        _displayHelper = DisplayHelper(context)

        _engine = Engine.create().also {
            _renderer = it.createRenderer()
            _scene = it.createScene()
            _view = it.createView()
            _camera = it.createCamera(it.entityManager.create())
        }
    }

    private fun setupView() {
        _scene.skybox = Skybox.Builder()
                .color(0.035f, 0.035f, 0.035f, 1.0f)
                .build(_engine)

        with(_view) {
            camera = _camera
            scene = _scene
        }
    }

    private fun setupGeometry() {
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

        // Create the indices
        val indexData = ByteBuffer.allocate(vertexCount * shortSize)
                .order(ByteOrder.nativeOrder())
                .putShort(0)
                .putShort(1)
                .putShort(2)
                .flip()

        _triangle = Mesh().apply {
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
            vertexBuffer?.setBufferAt(_engine, 0, vertexData)

            indexBuffer = IndexBuffer.Builder()
                    .indexCount(3)
                    .bufferType(IndexBuffer.Builder.IndexType.USHORT)
                    .build(_engine)
            indexBuffer?.setBuffer(_engine, indexData)

            val materialBuffer = AssetHelper.readMaterial(context, "baked_color.filamat")

            material = Material.Builder()
                    .payload(materialBuffer, materialBuffer.remaining())
                    .build(_engine)
        }
    }

    private fun setupScene() {
        if (_triangle.vertexBuffer == null || _triangle.indexBuffer == null) return

        RenderableManager.Builder(1)
                .boundingBox(Box(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.01f))
                .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, _triangle.vertexBuffer!!, _triangle.indexBuffer!!, 0, 3)
                .material(0, _triangle.material!!.defaultInstance)
                .build(_engine, _triangle.rendererable)

        _scene.addEntity(_triangle.rendererable)
    }

    // PlatformView

    override fun onFlutterViewAttached(flutterView: View) {
//        setupFilament()
//        setupView()
//        setupGeometry()
//        setupScene()
//
//        _uiHelper.attachTo(_textureView)
//        _uiHelper.renderCallback = SurfaceCallback()
    }

    override fun getView(): View {
        setupFilament()
        setupView()
        setupGeometry()
        setupScene()

        _uiHelper.attachTo(_textureView)
        _uiHelper.renderCallback = SurfaceCallback()

        return  _textureView
    }

    override fun dispose() {
        if (_disposed) return

        _methodChannel.setMethodCallHandler(null)

        _disposed = true
    }

    // MethodCallHandler

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            else -> Unit
        }
    }

    // DefaultLifecycleObserver

    inner class SurfaceCallback : UiHelper.RendererCallback {
        @SuppressLint("NewApi")
        override fun onNativeWindowChanged(surface: Surface) {
            _swapChain?.let { _engine.destroySwapChain(it) }
            _swapChain = _engine.createSwapChain(surface, _uiHelper.swapChainFlags)
            _displayHelper.attach(_renderer, _textureView.display);
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