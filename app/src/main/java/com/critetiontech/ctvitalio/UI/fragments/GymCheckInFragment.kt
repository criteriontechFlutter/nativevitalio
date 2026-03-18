package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.databinding.FragmentGymCheckInBinding
import com.critetiontech.ctvitalio.viewmodel.GymCheckInViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class GymCheckInFragment : Fragment() {

    private lateinit var binding: FragmentGymCheckInBinding
    private lateinit var viewModel: GymCheckInViewModel

    private var gymId: String? = null
    private var gymName: String? = null
    private var gymAddress: String? = null
    private var gymLatitude: Double? = null
    private var gymLongitude: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gymId = it.getString("gymId")
            gymName = it.getString("gymName")
            gymAddress = it.getString("gymAddress")
            gymLatitude = it.getDouble("latitude")
            gymLongitude = it.getDouble("longitude")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentGymCheckInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[GymCheckInViewModel::class.java]

        startCamera()

        binding.tvGymName.text = gymName
        binding.tvAddress.text = gymAddress

        viewModel.gymCheckInResponse.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720)) // Add target resolution
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                // Use background executor instead of main executor
                val cameraExecutor = Executors.newSingleThreadExecutor()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    scanQr(imageProxy) // Ensure imageProxy.close() is called inside scanQr()
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            } catch (e: Exception) {
                Log.e("CameraX", "Camera binding failed", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun scanQr(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { qr ->
                        Log.d("TAG", "scanQr: $qr");
                        viewModel.onQrScanned(qr,gymLatitude,gymLongitude)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}