package com.whelksoft.camera_with_rtmp

import android.app.Activity
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import com.pedro.encoder.input.video.CameraHelper.Facing.BACK
import com.pedro.encoder.input.video.CameraHelper.Facing.FRONT
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.view.OpenGlView
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.platform.PlatformView
import net.ossrs.rtmp.ConnectCheckerRtmp
import java.io.*
import java.nio.ByteBuffer


class CameraNativeView(
        private val activity: Activity,
        id: Int,
        var isFrontFacingOnStart: Boolean,
        creationParams: Map<String?, Any?>?) : PlatformView, SurfaceHolder.Callback, ConnectCheckerRtmp {

    private val glView: OpenGlView = OpenGlView(activity)
    private val rtmpCamera: RtmpCamera2

    private var isSurfaceCreated = false
    private var fps = 0

    private val videoWidth = 600
    private val videoHeight = 480

    init {
        glView.isKeepAspectRatio = true
        glView.holder.addCallback(this)
        rtmpCamera = RtmpCamera2(glView, this)
        rtmpCamera.setReTries(10)
        rtmpCamera.setFpsListener { fps = it }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.d("CameraNativeView", "surfaceCreated")
        isSurfaceCreated = true
        startPreview(isFrontFacingOnStart)
    }

    override fun onAuthSuccessRtmp() {
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
    }

    override fun onConnectionSuccessRtmp() {
    }

    override fun onConnectionFailedRtmp(reason: String) {
        activity.runOnUiThread { //Wait 5s and retry connect stream
            if (rtmpCamera.reTry(5000, reason)) {
//                dartMessenger.send(DartMessenger.EventType.RTMP_RETRY, reason)
                Toast.makeText(getView().context, "Retry", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getView().context, "Connection failed. $reason", Toast.LENGTH_SHORT).show()
                rtmpCamera.stopStream()
            }
        }
    }

    override fun onAuthErrorRtmp() {
    }

    override fun onDisconnectRtmp() {
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.d("CameraNativeView", "surfaceChanged $width $height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.d("CameraNativeView", "surfaceDestroyed")
    }

    fun close() {
        Log.d("CameraNativeView", "close")
    }

    fun takePicture(filePath: String, result: MethodChannel.Result) {
        Log.d("CameraNativeView", "takePicture filePath: $filePath result: $result")
        val file = File(filePath)
        if (file.exists()) {
            result.error("fileExists", "File at path '$filePath' already exists. Cannot overwrite.", null)
            return
        }
        glView.takePhoto {
            try {
                val outputStream: OutputStream = BufferedOutputStream(FileOutputStream(file))
                it.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                result.success(null)
            } catch (e: IOException) {
                result.error("IOError", "Failed saving image", null)
            }
        }
    }

    fun startVideoRecording(filePath: String?, result: MethodChannel.Result) {
        val file = File(filePath)
        if (file.exists()) {
            result.error("fileExists", "File at path '$filePath' already exists. Cannot overwrite.", null)
            return
        }
        Log.d("CameraNativeView", "startVideoRecording filePath: $filePath result: $result")
    }

    fun startVideoStreaming(url: String?, bitrate: Int?, result: MethodChannel.Result) {
        Log.d("CameraNativeView", "startVideoStreaming url: $url bitrate: $bitrate result: ${result != null}")
        if (url == null) {
            result.error("startVideoStreaming", "Must specify a url.", null)
            return
        }
        try {
            if (!rtmpCamera.isStreaming) {
                if (rtmpCamera.isRecording || rtmpCamera.prepareAudio() && rtmpCamera.prepareVideo(videoWidth, videoHeight, bitrate
                                ?: 1200 * 1024)) {
                    // ready to start streaming
                    rtmpCamera.startStream(url)
                } else {
                    result.error("videoStreamingFailed", "Error preparing stream, This device cant do it", null)
                }
            } else {
                rtmpCamera.stopStream()
            }
            result.success(null)
        } catch (e: CameraAccessException) {
            result.error("videoStreamingFailed", e.message, null)
        } catch (e: IOException) {
            result.error("videoStreamingFailed", e.message, null)
        }
    }

    fun startVideoRecordingAndStreaming(filePath: String?, url: String?, bitrate: Int?, result: MethodChannel.Result) {
        Log.d("CameraNativeView", "startVideoStreaming url: $url bitrate: $bitrate result: ${result != null}")
        // TODO: Implement video recording
        startVideoStreaming(url, bitrate ?: 1200 * 1024, result)
    }

    fun pauseVideoStreaming(result: Any) {
        // TODO: Implement pause video streaming
    }

    fun resumeVideoStreaming(result: Any) {
        // TODO: Implement resume video streaming
    }

    fun stopVideoRecordingOrStreaming(result: MethodChannel.Result) {
        try {
            rtmpCamera.apply {
                if (isStreaming) stopStream()
                if (isRecording) stopRecord()
            }
            result.success(null)
        } catch (e: CameraAccessException) {
            result.error("videoRecordingFailed", e.message, null)
        } catch (e: IllegalStateException) {
            result.error("videoRecordingFailed", e.message, null)
        }
    }

    fun stopVideoRecording(result: MethodChannel.Result) {
        try {
            rtmpCamera.apply {
                if (isRecording) stopRecord()
            }
            result.success(null)
        } catch (e: CameraAccessException) {
            result.error("stopVideoRecordingFailed", e.message, null)
        } catch (e: IllegalStateException) {
            result.error("stopVideoRecordingFailed", e.message, null)
        }
    }

    fun stopVideoStreaming(result: MethodChannel.Result) {
        try {
            rtmpCamera.apply {
                if (isStreaming) stopStream()
            }
            result.success(null)
        } catch (e: CameraAccessException) {
            result.error("stopVideoStreamingFailed", e.message, null)
        } catch (e: IllegalStateException) {
            result.error("stopVideoStreamingFailed", e.message, null)
        }
    }

    fun pauseVideoRecording(result: Any) {
        // TODO: Implement pause Video Recording
    }

    fun resumeVideoRecording(result: Any) {
        // TODO: Implement resume video recording
    }

    fun startPreviewWithImageStream(imageStreamChannel: Any) {
        // TODO: Implement start preview with image stream
    }

    fun startPreview(isFrontFacing: Boolean) {
        Log.d("CameraNativeView", "startPreview")
        if (isSurfaceCreated) {
            try {
                if (rtmpCamera.isOnPreview) {
                    rtmpCamera.stopPreview()
                }
                rtmpCamera.startPreview(if (isFrontFacing) FRONT else BACK, videoWidth, videoHeight)
            } catch (e: CameraAccessException) {
                close()
                return
            }
        }
    }

    fun getStreamStatistics(result: MethodChannel.Result) {
        val ret = hashMapOf<String, Any>()
        ret["cacheSize"] = rtmpCamera.cacheSize
        ret["sentAudioFrames"] = rtmpCamera.sentAudioFrames
        ret["sentVideoFrames"] = rtmpCamera.sentVideoFrames
        ret["droppedAudioFrames"] = rtmpCamera.droppedAudioFrames
        ret["droppedVideoFrames"] = rtmpCamera.droppedVideoFrames
        ret["isAudioMuted"] = rtmpCamera.isAudioMuted
        ret["bitrate"] = rtmpCamera.bitrate
        ret["width"] = rtmpCamera.streamWidth
        ret["height"] = rtmpCamera.streamHeight
        ret["fps"] = fps
        result.success(ret)
    }

    @Throws(IOException::class)
    private fun writeToFile(buffer: ByteBuffer, file: File) {
        FileOutputStream(file).use { outputStream ->
            while (0 < buffer.remaining()) {
                outputStream.channel.write(buffer)
            }
        }
    }

    override fun getView(): View {
        return glView
    }

    override fun dispose() {
        isSurfaceCreated = false
    }
}
