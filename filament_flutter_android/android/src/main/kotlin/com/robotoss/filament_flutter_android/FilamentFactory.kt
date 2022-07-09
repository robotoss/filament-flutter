package com.robotoss.filament_flutter_android

import android.app.Activity
import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class FilamentFactory(
        private val activity: Activity,
        private val binaryMessenger: BinaryMessenger,
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        val builder = FilamentBuilder()

        return builder.build(viewId, requireNotNull(context), activity, binaryMessenger)
    }
}