package com.critetiontech.ctvitalio.UI.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.databinding.FragmentScavengerHuntProgressBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.io.File

class ScavengerHuntProgress : Fragment() {

    private var _binding: FragmentScavengerHuntProgressBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageCapture: ImageCapture

    private var foundCount = 0

    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                startCamera()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentScavengerHuntProgressBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        checkCameraPermission()
        binding.layoutItemsFound.visibility = View.GONE
        binding.btnFound.setOnClickListener {

            takePhoto()
        }

        binding.imgClose.setOnClickListener {

            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()
        }

        updateProgress()
    }

    private fun checkCameraPermission() {

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()

        } else {

            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    private fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(
                requireContext()
            )

        cameraProviderFuture.addListener({

            val cameraProvider =
                cameraProviderFuture.get()

            val preview =
                Preview.Builder().build()

            preview.setSurfaceProvider(
                binding.previewView.surfaceProvider
            )

            imageCapture =
                ImageCapture.Builder().build()

            val cameraSelector =
                CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {

        val file =
            File(
                requireContext().cacheDir,
                "IMG_${System.currentTimeMillis()}.jpg"
            )

        val outputOptions =
            ImageCapture.OutputFileOptions
                .Builder(file)
                .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults: ImageCapture.OutputFileResults
                ) {

                    foundCount++

                    if (foundCount > 0) {
                        binding.layoutItemsFound.visibility = View.VISIBLE
                    }

                    val bitmap =
                        BitmapFactory.decodeFile(file.absolutePath)

                    when (foundCount) {
                        1 -> binding.imgItem1.setImageBitmap(bitmap)
                        2 -> binding.imgItem2.setImageBitmap(bitmap)
                        3 -> binding.imgItem3.setImageBitmap(bitmap)
                    }

                    updateProgress()
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {
                }
            }
        )
    }

    private fun updateProgress() {

        binding.txtProgress.text =
            "$foundCount / 3"

        when (foundCount) {

            0 -> {

                binding.progress1.alpha = 0.1f
                binding.progress2.alpha = 0.1f
                binding.progress3.alpha = 0.1f
            }

            1 -> {

                binding.progress1.alpha = 1f
                binding.progress1.alpha = 1f
            }

            2 -> {

                binding.progress1.alpha = 1f
                binding.progress2.alpha = 1f
            }

            3 -> {

                binding.progress1.alpha = 1f
                binding.progress2.alpha = 1f
                binding.progress3.alpha = 1f
            }
        }
    }

    private fun showCompletionBottomSheet() {

        val bottomSheet =
            BottomSheetDialog(requireContext())

        val view =
            layoutInflater.inflate(
                com.critetiontech.ctvitalio.R.layout.bottomsheet_mindfulness_completed,
                null
            )

        bottomSheet.setContentView(view)

        val btnOk =
            view.findViewById<MaterialButton>(
                com.critetiontech.ctvitalio.R.id.btnOk
            )

        btnOk.setOnClickListener {

            bottomSheet.dismiss()

            foundCount = 0

            updateProgress()
        }

        bottomSheet.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}