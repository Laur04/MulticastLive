package com.lauren.multicastlive.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lauren.multicastlive.configuration.Configuration
import com.lauren.multicastlive.databinding.MainFragmentBinding
import com.lauren.multicastlive.utils.DialogUtils
import com.lauren.multicastlive.utils.PermissionManager
import com.lauren.multicastlive.utils.StreamerManager
import io.github.thibaultbee.streampack.utils.getCameraCharacteristics
import io.github.thibaultbee.streampack.views.getPreviewOutputSize

class PreviewFragment : Fragment() {
    private lateinit var binding: MainFragmentBinding

    companion object {
        private const val TAG = "PreviewFragment"
    }

    private val viewModel: PreviewViewModel by lazy {
        ViewModelProvider(
            this,
            PreviewViewModelFactory(
                StreamerManager(
                    requireContext(),
                    Configuration(requireContext())
                )
            )
        )[PreviewViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        bindProperties()
        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun bindProperties() {
        binding.liveButton.setOnClickListener {
            requestStreamerPermissions(viewModel.requiredPermissions)
        }

        viewModel.streamerError.observe(viewLifecycleOwner) {
            showError("Oops", it)
        }
    }

    private fun startStopLive() {
        if (binding.liveButton.isChecked) {
            startStream()
        } else {
            stopStream()
        }
    }

    private fun requestStreamerPermissions(permissions: List<String>) {
        when {
            PermissionManager.hasPermissions(
                requireContext(),
                *permissions.toTypedArray()
            ) -> {
                startStopLive()
            }
            else -> {
                requestStreamerPermissionsLauncher.launch(
                    permissions.toTypedArray()
                )
            }
        }
    }

    private fun startStream() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        viewModel.startStream()
    }

    private fun unLockScreen() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun stopStream() {
        viewModel.stopStream()
        unLockScreen()
    }

    private fun showPermissionError() {
        binding.liveButton.isChecked = false
        unLockScreen()
        DialogUtils.showPermissionAlertDialog(requireContext())
    }

    private fun showPermissionErrorAndFinish() {
        binding.liveButton.isChecked = false
        DialogUtils.showPermissionAlertDialog(requireContext()) { requireActivity().finish() }
    }

    private fun showError(title: String, message: String) {
        binding.liveButton.isChecked = false
        unLockScreen()
        DialogUtils.showAlertDialog(requireContext(), "Error: $title", message)
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        requestCameraAndMicrophonePermissions()
        binding.preview.holder.addCallback(surfaceViewCallback)
    }

    @SuppressLint("MissingPermission")
    private fun requestCameraAndMicrophonePermissions() {
        when {
            PermissionManager.hasPermissions(
                requireContext(),
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
            ) -> {
                createStreamer()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                showPermissionError()
                requestCameraAndMicrophonePermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO
                    )
                )
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionError()
                requestCameraAndMicrophonePermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA
                    )
                )
            }
            else -> {
                requestCameraAndMicrophonePermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val surfaceViewCallback = object : SurfaceHolder.Callback {
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            viewModel.stopPreview()
            binding.preview.holder.removeCallback(this)
        }

        override fun surfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) = Unit

        override fun surfaceCreated(holder: SurfaceHolder) {
            viewModel.cameraId?.let {
                val previewSize = getPreviewOutputSize(
                    binding.preview.display,
                    requireContext().getCameraCharacteristics(it),
                    SurfaceHolder::class.java
                )
                Log.d(
                    TAG,
                    "View finder size: ${binding.preview.width} x ${binding.preview.height}"
                )
                Log.d(TAG, "Selected preview size: $previewSize")
                binding.preview.setAspectRatio(previewSize.width, previewSize.height)

                // To ensure that size is set, initialize camera in the view's thread
                binding.preview.post { viewModel.startPreview(holder.surface) }
            }
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun createStreamer() {
        viewModel.createStreamer()
        binding.preview.visibility = View.VISIBLE
    }

    @SuppressLint("MissingPermission")
    private val requestCameraAndMicrophonePermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.toList().all {
                    it.second == true
                }) {
                createStreamer()
            } else {
                showPermissionErrorAndFinish()
            }
        }

    private val requestStreamerPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.toList().all {
                    it.second == true
                }) {
                startStopLive()
            } else {
                showPermissionError()
            }
        }
}
