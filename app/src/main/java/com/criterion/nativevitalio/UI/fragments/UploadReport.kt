package com.criterion.nativevitalio.UI.fragments

import PrefsManager
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.criterion.nativevitalio.adapter.UploadReportAdapter
import com.criterion.nativevitalio.model.UploadReportItem
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentUploadReportBinding
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.viewmodel.UploadReportViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UploadReport : Fragment() {

    private lateinit var binding: FragmentUploadReportBinding
    private lateinit var viewModel: UploadReportViewModel

    private var imageUri: Uri? = null
    private var imageFile: File? = null
    private var selectedImageUri: Uri? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageFile != null) {
                Toast.makeText(requireContext(), "Image saved: ${imageFile!!.path}", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                Toast.makeText(requireContext(), "Image selected", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkCameraPermissionAndLaunch() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(permission)
        }
    }

    private fun openCamera() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile!!
        )

        cameraLauncher.launch(imageUri)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[UploadReportViewModel::class.java]

        val upperList = listOf(
            UploadReportItem("Radiology", R.drawable.reminders),
            UploadReportItem("Imaging", R.drawable.ic_list_icon),
            UploadReportItem("Lab", R.drawable.ic_list_icon)
        )
        val adapter = UploadReportAdapter(requireContext(), upperList)
        binding.spinnerTestType.adapter = adapter

        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Report Date")
                .build()

            datePicker.show(parentFragmentManager, "report_date_picker")
            datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
                val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .format(Date(selectedDateInMillis))
                binding.etDate.setText(formattedDate)
            }
        }

        binding.camera.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        binding.gallery.setOnClickListener {
            openGallery()
        }
        binding.btnUploadSave.setOnClickListener {

            val category = binding.spinnerTestType.selectedItem?.let {
                (it as? UploadReportItem)?.title
            } ?: "Unknown"

            val subCategory = binding.etTestName.text.toString()
            val dateTime = binding.etDate.text.toString()

            val imageFile = imageFile ?: run {
                Toast.makeText(requireContext(), "Please capture or select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val admitDoctorId = PrefsManager().getPatient()?.doctorID.toString()
            val api = RetrofitInstance.createApiService7082()

            lifecycleScope.launch {
                val result = viewModel.insertPatientMediaDataAndParseResponse(requireContext(), imageFile)
                Log.d("ParsedReportData", result.toString())
            }
        }
    }
}