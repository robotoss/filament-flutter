package com.robotoss.filament_flutter_android

import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class FilamentFlutterAndroidPlugin : FlutterPlugin, ActivityAware {
    companion object {
        const val VIEW_TYPE = "flutter_filament_plugin.view"
    }

    //  private lateinit var channel: MethodChannel
    private var lifecycle: Lifecycle? = null
    private lateinit var pluginBinding: FlutterPlugin.FlutterPluginBinding

    // FlutterPlugin

    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {


        pluginBinding = binding
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    }

    // ActivityAware

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        lifecycle = (binding.lifecycle as? HiddenLifecycleReference)?.lifecycle

        pluginBinding.platformViewRegistry
            .registerViewFactory(
                VIEW_TYPE,
                FilamentFactory(binding.activity, pluginBinding.binaryMessenger)
            )
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onDetachedFromActivity() {
        lifecycle = null
    }

}