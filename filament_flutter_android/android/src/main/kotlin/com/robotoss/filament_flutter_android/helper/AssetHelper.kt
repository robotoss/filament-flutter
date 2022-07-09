package com.robotoss.filament_flutter_android.helper

import android.content.Context
import java.nio.ByteBuffer
import java.nio.channels.Channels

sealed class AssetHelper {
    companion object {
        fun readMaterial(context: Context, assetName: String): ByteBuffer {
            context.assets.openFd(assetName).use { fd ->
                val input = fd.createInputStream()
                val dst = ByteBuffer.allocate(fd.length.toInt())

                val src = Channels.newChannel(input)
                src.read(dst)
                src.close()

                return dst.apply { rewind() }
            }
        }
    }
}