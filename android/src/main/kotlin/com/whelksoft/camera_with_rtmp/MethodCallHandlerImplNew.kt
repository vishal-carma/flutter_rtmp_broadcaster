package com.whelksoft.camera_with_rtmp

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.OrientationEventListener
import androidx.annotation.RequiresApi
import com.whelksoft.camera_with_rtmp.CameraPermissions.ResultCallback
import io.flutter.plugin.common.*
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.view.TextureRegistry
import io.flutter.embedding.engine.FlutterEngine
import java.util.HashMap

class MethodCallHandlerImplNew(
        private val activity: Activity,
        private val messenger: BinaryMessenger,
        private val cameraPermissions: CameraPermissions,
        private val permissionsRegistry: PermissionStuff,
        private val flutterEngine: FlutterEngine) : MethodCallHandler {

    private val methodChannel: MethodChannel
    private val imageStreamChannel: EventChannel
//    private var cameraView: CameraNativeView? = null

    private val orientationEventListener: OrientationEventListener
    private var currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN
    private var dartMessenger: DartMessenger? = null
    private var nativeViewFactory: NativeViewFactory? = null
    private val  handler = Handler()

    private val textureId = 0L

    init {
        Log.d("TAG", "init $flutterEngine")
        methodChannel = MethodChannel(messenger, "plugins.flutter.io/camera_with_rtmp")
        imageStreamChannel = EventChannel(messenger, "plugins.flutter.io/camera_with_rtmp/imageStream")
        methodChannel.setMethodCallHandler(this)
        nativeViewFactory = NativeViewFactory(activity)

        flutterEngine
                .platformViewsController
                .registry
                .registerViewFactory("hybrid-view-type", nativeViewFactory)

        orientationEventListener = object : OrientationEventListener(activity.applicationContext) {
            override fun onOrientationChanged(i: Int) {
                if (i == ORIENTATION_UNKNOWN) {
                    return
                }
                // Convert the raw deg angle to the nearest multiple of 90.
                currentOrientation = Math.round(i / 90.0).toInt() * 90
                // Send a message with the new orientation to the ux.
                dartMessenger?.send(DartMessenger.EventType.ROTATION_UPDATE, (currentOrientation / 90).toString())
                Log.i("TAG", "Updated Orientation (sent) " + currentOrientation + " -- " + (currentOrientation / 90).toString())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "availableCameras" -> try {
                Log.i("Stuff", "availableCameras")
                result.success(CameraUtils.getAvailableCameras(activity))
            } catch (e: Exception) {
                handleException(e, result)
            }
            "initialize" -> {
                Log.i("Stuff", "initialize")
//                getCameraView()?.close()
                cameraPermissions.requestPermissions(
                        activity,
                        permissionsRegistry,
                        call.argument("enableAudio")!!,
                        object : ResultCallback {
                            override fun onResult(errorCode: String?, errorDescription: String?) {
                                if (errorCode == null) {
                                    try {
                                        instantiateCamera(call, result)
                                    } catch (e: Exception) {
                                        handleException(e, result)
                                    }
                                } else {
                                    result.error(errorCode, errorDescription, null)
                                }
                            }
                        })
            }
            "takePicture" -> {
                Log.i("Stuff", "takePicture")
                getCameraView()?.takePicture(call.argument("path")!!, result)
                result.success(null)
            }
            "prepareForVideoRecording" -> {
                Log.i("Stuff", "prepareForVideoRecording")
                // This optimization is not required for Android.
                result.success(null)
            }
            "startVideoRecording" -> {
                Log.i("Stuff", "startVideoRecording")
                getCameraView()?.startVideoRecording(call.argument("filePath")!!, result)
            }
            "startVideoStreaming" -> {
                Log.i("Stuff", "startVideoStreaming ${call.arguments.toString()}")
                var bitrate: Int? = null
                if (call.hasArgument("bitrate")) {
                    bitrate = call.argument("bitrate")
                }
                getCameraView()?.startVideoStreaming(
                        call.argument("url"),
                        bitrate,
                        result)
            }
            "startVideoRecordingAndStreaming" -> {
                Log.i("Stuff", "startVideoRecordingAndStreaming ${call.arguments.toString()}")
                var bitrate: Int? = null
                if (call.hasArgument("bitrate")) {
                    bitrate = call.argument("bitrate")
                }
                getCameraView()?.startVideoRecordingAndStreaming(
                        call.argument("filePath"),
                        call.argument("url"),
                        bitrate,
                        result)
            }
            "pauseVideoStreaming" -> {
                Log.i("Stuff", "pauseVideoStreaming")
                getCameraView()?.pauseVideoStreaming(result)
            }
            "resumeVideoStreaming" -> {
                Log.i("Stuff", "resumeVideoStreaming")
                getCameraView()?.resumeVideoStreaming(result)
            }
            "stopRecordingOrStreaming" -> {
                Log.i("Stuff", "stopRecordingOrStreaming")
                getCameraView()?.stopVideoRecordingOrStreaming(result)
            }
            "stopRecording" -> {
                Log.i("Stuff", "stopRecording")
                getCameraView()?.stopVideoRecording(result)
            }
            "stopStreaming" -> {
                Log.i("Stuff", "stopStreaming")
                getCameraView()?.stopVideoStreaming(result)
            }
            "pauseVideoRecording" -> {
                Log.i("Stuff", "pauseVideoRecording")
                getCameraView()?.pauseVideoRecording(result)
            }
            "resumeVideoRecording" -> {
                Log.i("Stuff", "resumeVideoRecording")
                getCameraView()?.resumeVideoRecording(result)
            }
            "startImageStream" -> {
                Log.i("Stuff", "startImageStream")
                try {
                    getCameraView()?.startPreviewWithImageStream(imageStreamChannel)
                    result.success(null)
                } catch (e: Exception) {
                    handleException(e, result)
                }
            }
            "stopImageStream" -> {
                Log.i("Stuff", "startImageStream")
                try {
                    getCameraView()?.startPreview(true)
                    result.success(null)
                } catch (e: Exception) {
                    handleException(e, result)
                }
            }
            "getStreamStatistics" -> {
                Log.i("Stuff", "getStreamStatistics")
                try {
                    getCameraView()?.getStreamStatistics(result)
                } catch (e: Exception) {
                    handleException(e, result)
                }
            }
            "dispose" -> {
                Log.i("Stuff", "dispose")
                // Native camera view handles the view lifecircle by themselves
//                getCameraView()?.dispose()
                orientationEventListener.disable()
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    fun stopListening() {
        methodChannel.setMethodCallHandler(null)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun instantiateCamera(call: MethodCall, result: MethodChannel.Result) {
        handler.postDelayed({
            val cameraName = call.argument<String>("cameraName") ?: "0"
            val resolutionPreset = call.argument<String>("resolutionPreset") ?: "low"
            val streamingPreset = call.argument<String>("streamingPreset")
            val enableAudio = call.argument<Boolean>("enableAudio")!!
            var enableOpenGL = true
            if (call.hasArgument("enableAndroidOpenGL")) {
                enableOpenGL = call.argument<Boolean>("enableAndroidOpenGL")!!
            }
            dartMessenger = DartMessenger(messenger, textureId)

            val preset = Camera.ResolutionPreset.valueOf(resolutionPreset)
            val previewSize = CameraUtils.computeBestPreviewSize(cameraName, preset)
            val reply: MutableMap<String, Any> = HashMap()
            reply["textureId"] = textureId

            if (isPortrait) {
                reply["previewWidth"] = previewSize.width
                reply["previewHeight"] = previewSize.height
            } else {
                reply["previewWidth"] = previewSize.height
                reply["previewHeight"] = previewSize.width
            }
            reply["previewQuarterTurns"] = currentOrientation / 90
            orientationEventListener.enable()
            Log.i("TAG", "open: width: " + reply["previewWidth"] + " height: " + reply["previewHeight"] + " currentOrientation: " + currentOrientation + " quarterTurns: " + reply["previewQuarterTurns"])
            // TODO Refactor cameraView initialisation
            nativeViewFactory?.isFrontFacingOnStart = isFrontFacing(cameraName)
            getCameraView()?.startPreview(isFrontFacing(cameraName))
            result.success(reply)
        }, 100)
    }

    private fun isFrontFacing(cameraName: String): Boolean {
        val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = cameraManager.getCameraCharacteristics(cameraName)
        return characteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT
    }

    private val isPortrait: Boolean
        get() {
            val getOrient = activity.windowManager.defaultDisplay
            val pt = Point()
            getOrient.getSize(pt)

            return when (pt.x) {
                pt.y -> true
                else -> pt.x < pt.y
            }
        }

    // We move catching CameraAccessException out of onMethodCall because it causes a crash
    // on plugin registration for sdks incompatible with Camera2 (< 21). We want this plugin to
    // to be able to compile with <21 sdks for apps that want the camera and support earlier version.
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun handleException(exception: Exception, result: MethodChannel.Result) {
        if (exception is CameraAccessException) {
            result.error("CameraAccess", exception.message, null)
        }
        throw (exception as RuntimeException)
    }

    fun getCameraView(): CameraNativeView? = nativeViewFactory?.cameraNativeView
}