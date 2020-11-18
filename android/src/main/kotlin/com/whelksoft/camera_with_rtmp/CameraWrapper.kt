package com.whelksoft.camera_with_rtmp

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.util.RecordController
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.TextureRegistry.SurfaceTextureEntry
import net.ossrs.rtmp.ConnectCheckerRtmp
import java.util.*
import kotlin.math.roundToInt

class CameraWrapper(val activity: Activity,
                    val flutterTexture: SurfaceTextureEntry,
                    val dartMessenger: DartMessenger,
                    val cameraName: String,
                    val resolutionPreset: String?,
                    val streamingPreset: String?,
                    val enableAudio: Boolean,
                    val useOpenGL: Boolean) : ConnectCheckerRtmp, CameraSurfaceRenderer.OnRendererStateChangedListener, SurfaceTexture.OnFrameAvailableListener {

    private val context = activity.baseContext
    private val glView: FlutterGLSurfaceView = FlutterGLSurfaceView(context, flutterTexture.surfaceTexture())
    private var currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN

    //    private val openGlView = OpenGlView(context)
//    private val rtmpCamera1 = RtmpCamera1(openGlView, this)
    private val previewSize: Size
    private val rtmpCamera1 = RtmpCamera1(glView, this)
    private val recordController: RecordController = RecordController()
    private val orientationEventListener: OrientationEventListener
    private var renderer = CameraSurfaceRenderer()
    private var cameraCaptureSession: CameraCaptureSession? = null
    private val cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var surfaceTexture: SurfaceTexture? = null

    var isStreaming = false
        private set
    var isRecording = false
        private set
    var onPreview = false
        private set

    init {
        cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        glView.setEGLContextClientVersion(2)
        renderer = CameraSurfaceRenderer()
//        renderer.addOnRendererStateChangedLister(streamer.getVideoHandlerListener())
        renderer.addOnRendererStateChangedLister(this)

        glView.setRenderer(renderer)
        glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        previewSize = when (resolutionPreset) {
            null -> Camera.ResolutionPreset.low
            else -> Camera.ResolutionPreset.valueOf(resolutionPreset)
        }.let { CameraUtils.computeBestPreviewSize(cameraName, it) }
//        glView.holder.addCallback(object : SurfaceHolder.Callback {
//            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
//                rtmpCamera1.startPreview()
//            }
//
//            override fun surfaceDestroyed(holder: SurfaceHolder?) {
//                if (rtmpCamera1.isRecording) {
//                    rtmpCamera1.stopRecord()
//                }
//                if (rtmpCamera1.isStreaming) {
//                    rtmpCamera1.stopStream()
//                }
//                rtmpCamera1.stopPreview()
//            }
//
//            override fun surfaceCreated(holder: SurfaceHolder?) {
//                Log.e("TAG", "on surface crated")
//            }
//        })

        orientationEventListener = object : OrientationEventListener(activity.applicationContext) {
            override fun onOrientationChanged(i: Int) {
                if (i == ORIENTATION_UNKNOWN) {
                    return
                }
                // Convert the raw deg angle to the nearest multiple of 90.
                currentOrientation = (i / 90.0).roundToInt() * 90
                // Send a message with the new orientation to the ux.
                dartMessenger.send(DartMessenger.EventType.ROTATION_UPDATE, (currentOrientation / 90).toString())
                Log.i(TAG, "Updated Orientation (sent) " + currentOrientation + " -- " + (currentOrientation / 90).toString())
                updateSurfaceView()
            }
        }
        orientationEventListener.enable()
        currentOrientation = (activity.resources.configuration.orientation / 90.0).roundToInt() * 90
        updateSurfaceView()
    }

    private fun updateSurfaceView() {
        resizeSurface()
        setCameraPreviewSize()
    }

    fun getPreviewWidth(): Int = previewSize.width

    fun getPreviewHeight(): Int = previewSize.height

    private val isPortrait: Boolean
        get() {
            val getOrient = activity.windowManager.defaultDisplay
            val pt = Point()
            getOrient.getSize(pt)
            return if (pt.x == pt.y) true
            else pt.x < pt.y
        }

    private fun setCameraPreviewSize() {
//        val swapped: Boolean = camera.isCameraWidthHeightSwapped()
        glView.queueEvent { renderer.setCameraPreviewSize(480, 640, false) }
    }

    private fun resizeSurface() {
//        if (!openGlView.isAttachedToWindow) {
//            activity.addContentView(openGlView, LinearLayout.LayoutParams(480, 640))
//        } else {
////            openGlView.layoutParams = FrameLayout.LayoutParams(getStreamWidth(), camera.getResultHeight())
//            openGlView.layoutParams = FrameLayout.LayoutParams(480, 640)
//        }
        if (!glView.isAttachedToWindow) {
            activity.addContentView(glView, LinearLayout.LayoutParams(480, 640))
        } else {
            glView.layoutParams = FrameLayout.LayoutParams(480, 640)
        }
    }

    private fun stopPreview() {

    }

    private fun stopStream() {

    }

    private fun stopRecord() {

    }

    fun close() {

    }

    fun startVideoRecording(argument: Any, result: MethodChannel.Result) {

    }

    fun startVideoStreaming(url: String?, bitrate: Int?, result: MethodChannel.Result) {

    }

    fun startVideoRecordingAndStreaming(filePath: String, url: String?, bitrate: Int?, result: MethodChannel.Result) {

    }

    fun pauseVideoStreaming(result: MethodChannel.Result) {

    }

    fun resumeVideoStreaming(result: MethodChannel.Result) {

    }

    fun stopVideoRecordingOrStreaming(result: MethodChannel.Result) {

    }

    fun stopVideoRecording(result: MethodChannel.Result) {

    }

    fun stopVideoStreaming(result: MethodChannel.Result) {

    }

    fun pauseVideoRecording(result: MethodChannel.Result) {

    }

    fun resumeVideoRecording(result: MethodChannel.Result) {

    }

    fun startPreviewWithImageStream(imageStreamChannel: Any) {

    }

    fun getStreamStatistics(result: MethodChannel.Result) {

    }

    fun open(result: MethodChannel.Result) {
        cameraManager.openCamera(
                cameraName,
                object : CameraDevice.StateCallback() {
                    override fun onOpened(device: CameraDevice) {
                        cameraDevice = device
                        try {
                            startPreview()
                        } catch (e: CameraAccessException) {
                            result.error("CameraAccess", e.message, null)
                            close()
                            return
                        }
                        val reply: MutableMap<String, Any> = HashMap()
                        reply["textureId"] = flutterTexture.id()

                        if (isPortrait) {
                            reply["previewWidth"] = previewSize.width
                            reply["previewHeight"] = previewSize.height
                        } else {
                            reply["previewWidth"] = previewSize.height
                            reply["previewHeight"] = previewSize.width
                        }
                        reply["previewQuarterTurns"] = currentOrientation / 90
                        Log.i(TAG, "open: width: " + reply["previewWidth"] + " height: " + reply["previewHeight"] + " currentOrientation: " + currentOrientation + " quarterTurns: " + reply["previewQuarterTurns"])
                        result.success(reply)
                    }

                    override fun onClosed(camera: CameraDevice) {
                        dartMessenger.sendCameraClosingEvent()
                        super.onClosed(camera)
                    }

                    override fun onDisconnected(cameraDevice: CameraDevice) {
                        Log.v("Camera", "onDisconnected()")
                        close()
                        dartMessenger.send(DartMessenger.EventType.ERROR, "The camera was disconnected.")
                    }

                    override fun onError(cameraDevice: CameraDevice, errorCode: Int) {
                        Log.v("Camera", "onError(" + errorCode + ")")
                        close()
                        val errorDescription: String
                        errorDescription = when (errorCode) {
                            ERROR_CAMERA_IN_USE -> "The camera device is in use already."
                            ERROR_MAX_CAMERAS_IN_USE -> "Max cameras in use"
                            ERROR_CAMERA_DISABLED -> "The camera device could not be opened due to a device policy."
                            ERROR_CAMERA_DEVICE -> "The camera device has encountered a fatal error"
                            ERROR_CAMERA_SERVICE -> "The camera service has encountered a fatal error."
                            else -> "Unknown camera error"
                        }
                        dartMessenger.send(DartMessenger.EventType.ERROR, errorDescription)
                    }
                },
                null)
    }

    fun dispose() {
        close()
        flutterTexture.release()
        surfaceTexture?.release()
        orientationEventListener.disable()
    }

    // RTMP Connect checker
    override fun onAuthSuccessRtmp() {
        Log.i(TAG, "Auth success.")
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
    }

    override fun onConnectionSuccessRtmp() {
        Log.i(TAG, "Connection success.")
    }

    override fun onConnectionFailedRtmp(reason: String) {
        Log.i(TAG, "Connection failed.")
        rtmpCamera1.stopStream()
    }

    override fun onAuthErrorRtmp() {
        Log.i(TAG, "Auth error.")
    }

    override fun onDisconnectRtmp() {
        Log.i(TAG, "Disconnected.")
    }

    // End
    companion object {
        val TAG = "CameraWrapper"
    }

    override fun onSurfaceCreated(surfaceTextureArg: SurfaceTexture?) {
//        onActivityResume()
        surfaceTextureArg?.setDefaultBufferSize(getPreviewWidth(), getPreviewHeight())
        surfaceTextureArg?.setOnFrameAvailableListener(this)
//        camera.startPreview(surfaceTextureArg)
        surfaceTexture = surfaceTextureArg
    }

    override fun onFrameDrawn(textureId: Int, transform: FloatArray?, timestamp: Long) {
        // no-op
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        glView.requestRender()
    }

    @Throws(CameraAccessException::class)
    fun startPreview() {
//        createCaptureSession(
//                CameraDevice.TEMPLATE_PREVIEW,
//                Runnable { },
//                pictureImageReader!!.surface)
    }

    @Throws(CameraAccessException::class)
    private fun createCaptureSession(
            templateType: Int, onSuccessCallback: Runnable, surface: Surface
    ) {
        // Close the old session first.
        closeCaptureSession()
//        Log.v("Camera", "createCaptureSession " + previewSize.width + "x" + previewSize.height + " mediaOrientation: " + mediaOrientation + " currentOrientation: " + currentOrientation + " sensorOrientation: " + sensorOrientation + " porteait: " + isPortrait)

        // Create a new capture builder.
        val requestBuilder = cameraDevice!!.createCaptureRequest(templateType)

        // Collect all surfaces we want to render to.
        val surfaceList: MutableList<Surface> = ArrayList()

        // Build Flutter surface to render to
        if (isPortrait) {
            surfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
        } else {
            surfaceTexture?.setDefaultBufferSize(previewSize.height, previewSize.width)
        }
        val flutterSurface = Surface(surfaceTexture)

        // The capture request.
        requestBuilder.addTarget(flutterSurface)
        if (templateType != CameraDevice.TEMPLATE_PREVIEW) {
            requestBuilder.addTarget(surface)
        }

        // Create the surface lists for the capture session.
        surfaceList.add(flutterSurface)
        surfaceList.add(surface)

        // Prepare the callback
        val callback: CameraCaptureSession.StateCallback = object : CameraCaptureSession.StateCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onConfigured(session: CameraCaptureSession) {
                try {
                    if (cameraDevice == null) {
                        dartMessenger.send(
                                DartMessenger.EventType.ERROR, "The camera was closed during configuration.")
                        return
                    }
                    Log.v("Camera", "open successful ")
                    requestBuilder.set(
                            CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    session.setRepeatingRequest(requestBuilder.build(), null, null)
                    cameraCaptureSession = session
                    onSuccessCallback.run()
                } catch (e: CameraAccessException) {
                    Log.v("Camera", "Error CameraAccessException", e)
                    dartMessenger.send(DartMessenger.EventType.ERROR, e.message)
                } catch (e: IllegalStateException) {
                    Log.v("Camera", "Error IllegalStateException", e)
                    dartMessenger.send(DartMessenger.EventType.ERROR, e.message)
                } catch (e: IllegalArgumentException) {
                    Log.v("Camera", "Error IllegalArgumentException", e)
                    dartMessenger.send(DartMessenger.EventType.ERROR, e.message)
                }
            }

            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                dartMessenger.send(
                        DartMessenger.EventType.ERROR, "Failed to configure camera session.")
            }
        }

        // Start the session
        cameraDevice!!.createCaptureSession(surfaceList, callback, null)
    }

    private fun closeCaptureSession() {
        if (cameraCaptureSession != null) {
            Log.v("Camera", "Close recordingCaptureSession")
            try {
                cameraCaptureSession!!.stopRepeating()
                cameraCaptureSession!!.abortCaptures()
                cameraCaptureSession!!.close()
            } catch (e: CameraAccessException) {
                Log.w("RtmpCamera", "Error from camera", e)
            }
            cameraCaptureSession = null
        } else {
            Log.v("Camera", "No recoordingCaptureSession to close")
        }
    }
}