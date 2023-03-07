package com.lauren.multicastlive.ui.main

import android.Manifest
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lauren.multicastlive.utils.ObservableViewModel
import com.lauren.multicastlive.utils.StreamerManager
import io.github.thibaultbee.streampack.error.StreamPackError
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import kotlinx.coroutines.launch

class PreviewViewModel(private val streamerManager: StreamerManager) : ObservableViewModel() {
    companion object {
        private const val TAG = "PreviewViewModel"
    }

    val cameraId: String?
        get() = streamerManager.cameraId

    val streamerError = MutableLiveData<String>()

    val requiredPermissions: List<String>
        get() = streamerManager.requiredPermissions

    private val onErrorListener = object : OnErrorListener {
        override fun onError(error: StreamPackError) {
            Log.e(TAG, "onError", error)
            streamerError.postValue("${error.javaClass.simpleName}: ${error.message}")
        }
    }

    private val onConnectionListener = object : OnConnectionListener {
        override fun onLost(message: String) {
            streamerError.postValue("Connection lost: $message")
        }

        override fun onFailed(message: String) {
            // Not needed as we catch startStream
        }

        override fun onSuccess() {
            Log.i(TAG, "Connection succeeded")
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun createStreamer() {
        viewModelScope.launch {
            try {
                streamerManager.rebuildStreamer()
                streamerManager.onErrorListener = onErrorListener
                streamerManager.onConnectionListener = onConnectionListener
                Log.d(TAG, "Streamer is created")
            } catch (e: Throwable) {
                Log.e(TAG, "createStreamer failed", e)
                streamerError.postValue("createStreamer: ${e.message ?: "Unknown error"}")
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA])
    fun startPreview(previewSurface: Surface) {
        viewModelScope.launch {
            try {
                streamerManager.startPreview(previewSurface)
            } catch (e: Throwable) {
                Log.e(TAG, "startPreview failed", e)
                streamerError.postValue("startPreview: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun stopPreview() {
        viewModelScope.launch {
            try {
                streamerManager.stopPreview()
            } catch (e: Throwable) {
                Log.e(TAG, "stopPreview failed", e)
            }
        }
    }

    fun startStream() {
        viewModelScope.launch {
            try {
                streamerManager.startStream()
            } catch (e: Throwable) {
                Log.e(TAG, "startStream failed", e)
                streamerError.postValue("startStream: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun stopStream() {
        viewModelScope.launch {
            try {
                streamerManager.stopStream()
            } catch (e: Throwable) {
                Log.e(TAG, "stopStream failed", e)
            }
        }
    }

    fun setMute(isMuted: Boolean) {
        streamerManager.isMuted = isMuted
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun toggleCamera() {
        try {
            streamerManager.toggleCamera()
        } catch (e: Exception) {
            Log.e(TAG, "toggleCamera failed", e)
            streamerError.postValue("toggleCamera: ${e.message ?: "Unknown error"}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            streamerManager.release()
        } catch (e: Exception) {
            Log.e(TAG, "streamer.release failed", e)
        }
    }
}
