package com.critetiontech.ctvitalio.UI.fragments


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.databinding.FragmentScavengerHuntProgressBinding
import com.critetiontech.ctvitalio.utils.ColorDetector
import com.google.android.material.chip.Chip
import java.io.File
import org.opencv.android.OpenCVLoader
class ScavengerHuntProgress : Fragment() {

    private var _binding: FragmentScavengerHuntProgressBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageCapture: ImageCapture

    private var foundCount = 0

    private var selectedColor = "Red"

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

        OpenCVLoader.initLocal()

        checkCameraPermission()

        binding.layoutItemsFound.visibility =
            View.GONE

        binding.btnFound.setOnClickListener {

            takePhoto()
        }

        binding.imgClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->

            if (checkedIds.isNotEmpty()) {

                val chip =
                    group.findViewById<Chip>(
                        checkedIds.first()
                    )

                selectedColor =
                    chip.text.toString()
            }
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

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
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

                    val bitmap =
                        BitmapFactory.decodeFile(
                            file.absolutePath
                        )

                    val detectedColor =
                        ColorDetector.detectColor(
                            bitmap
                        )

                    if (
                        detectedColor.equals(
                            selectedColor,
                            true
                        )
                    ) {

                        foundCount++

                        binding.layoutItemsFound.visibility =
                            View.VISIBLE

                        when (foundCount) {

                            1 -> binding.imgItem1.setImageBitmap(bitmap)

                            2 -> binding.imgItem2.setImageBitmap(bitmap)

                            3 -> binding.imgItem3.setImageBitmap(bitmap)
                        }

                        updateProgress()

                        Toast.makeText(
                            requireContext(),
                            "Correct Color Found : $detectedColor",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        Toast.makeText(
                            requireContext(),
                            "Detected : $detectedColor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {

                    Toast.makeText(
                        requireContext(),
                        exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun updateProgress() {

        binding.txtProgress.text =
            "$foundCount / 3"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}