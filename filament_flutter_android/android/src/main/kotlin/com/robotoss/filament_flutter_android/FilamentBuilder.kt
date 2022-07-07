package com.robotoss.filament_flutter_android

import android.app.Activity
import android.content.Context
import io.flutter.plugin.common.BinaryMessenger

class FilamentBuilder {
    fun build(
            viewId: Int,
            context: Context,
            activity: Activity,
            binaryMessenger: BinaryMessenger
    ): FilamentController {
        return FilamentController(viewId, context, activity, binaryMessenger)
    }

}