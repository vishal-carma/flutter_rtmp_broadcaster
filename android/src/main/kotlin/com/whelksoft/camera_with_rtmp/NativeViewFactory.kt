package com.whelksoft.camera_with_rtmp

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

internal class NativeViewFactory(private val activity: Activity) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    init {
    }

    var cameraNativeView: CameraNativeView? = null
    var isFrontFacingOnStart: Boolean = true

    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?
        cameraNativeView = CameraNativeView(activity, id, isFrontFacingOnStart, creationParams)
        Log.d("TAG", "NativeViewFactory create $cameraNativeView")
        return cameraNativeView!!
    }

}