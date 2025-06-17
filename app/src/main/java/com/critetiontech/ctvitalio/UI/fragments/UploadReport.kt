package com.critetiontech.ctvitalio.UI.fragments

import android.Manifest
import android.app.TimePickerDialog
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
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.critetiontech.ctvitalio.adapter.UploadReportAdapter
import com.critetiontech.ctvitalio.model.UploadReportItem
import com.critetiontech.ctvitalio.utils.FileUtil
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentUploadReportBinding
import com.critetiontech.ctvitalio.viewmodel.UploadReportViewModel
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UploadReport : Fragment() {


    private lateinit var binding: FragmentUploadReportBinding
    private lateinit var viewModel: UploadReportViewModel

    private var imageUri: Uri? = null
    private var imageFile: File? = null

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        when {
            result.isSuccessful && result.uriContent != null -> {
                val uri = result.uriContent!!
                imageUri = uri
                imageFile = FileUtil.from(requireContext(), uri)

                // ✅ Show filename and file info layout
                binding.layoutFileInfo.visibility = View.VISIBLE
                binding.tvSelectedFileName.text = imageFile?.name ?: "File selected"
            }

            else -> {
                Toast.makeText(requireContext(), result.error?.message ?: "Cropping failed", Toast.LENGTH_SHORT).show()
                Log.e("CropError", "Cropping failed", result.error)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            launchCropper(it)
        }
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openCamera()
            else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // Check if imageUri and imageFile are valid
                if (imageUri != null && imageFile?.exists() == true) {
                    // Image capture succeeded, proceed with cropping
                    launchCropper(imageUri!!)
                } else {
                    // Handle failure: Image URI or file is invalid
                    Toast.makeText(requireContext(), "Capture failed. Try again.", Toast.LENGTH_SHORT).show()
                    imageUri = null
                    imageFile = null
                }
            } else {
                // Handle the case where the camera capture itself failed (e.g., user canceled)
                Toast.makeText(requireContext(), "Capture failed. Try again.", Toast.LENGTH_SHORT).show()
                imageUri = null
                imageFile = null
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
        val imageFileName = "IMG_${timeStamp}_"
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

    private fun launchCropper(sourceUri: Uri) {
        val fileUri = getFileUriFromContentUri(sourceUri)
        val cropOptions = CropImageOptions().apply {
            activityTitle = "Crop Report Image"
            cropMenuCropButtonTitle = "Crop"
            cropMenuCropButtonIcon = 0
            guidelines = CropImageView.Guidelines.ON
            cropShape = CropImageView.CropShape.RECTANGLE
            fixAspectRatio = false
            autoZoomEnabled = true
            scaleType = CropImageView.ScaleType.FIT_CENTER
        }
        cropImageLauncher.launch(CropImageContractOptions(fileUri, cropOptions))
    }

    private fun getFileUriFromContentUri(uri: Uri): Uri {
        val inputStream = requireContext().contentResolver.openInputStream(uri)!!
        val file = File.createTempFile("cropped_", ".jpg", requireContext().cacheDir)
        file.outputStream().use { output -> inputStream.copyTo(output) }
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUploadReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[UploadReportViewModel::class.java]

        val upperList = listOf(
            UploadReportItem("Radiology", R.drawable.reminders, "/path/to/file1.pdf", "Radiology"),
            UploadReportItem("Imaging", R.drawable.reminders, "/path/to/file2.jpg", "Imaging"),
            UploadReportItem("Lab", R.drawable.reminders, "/path/to/file3.pdf", "Lab")
        )
        binding.spinnerTestType.adapter = UploadReportAdapter(requireContext(), upperList)


        binding.etDate.setOnClickListener {
            val today = Calendar.getInstance().timeInMillis

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Report Date")
                .setSelection(today)
                .build()

            datePicker.show(parentFragmentManager, "date_picker")

            datePicker.addOnPositiveButtonClickListener { selectedDateMillis ->
                if (selectedDateMillis > today) {
                    Toast.makeText(requireContext(), "Cannot select a future date!", Toast.LENGTH_SHORT).show()
                } else {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                    }

                    // Format the selected date only (no time)
                    val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(calendar.time)

                    binding.etDate.setText(formatted)
                }
            }
        }



        binding.camera.setOnClickListener { checkCameraPermissionAndLaunch() }
        binding.gallery.setOnClickListener { openGallery() }

        binding.backIcon.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnRemoveFile.setOnClickListener {
            imageUri = null
            imageFile = null
            binding.layoutFileInfo.visibility = View.GONE
            binding.tvSelectedFileName.text = "No file selected"
        }

//        binding.btnUploadSave.setOnClickListener {
//            val selectedFile = imageFile ?: run {
//                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            lifecycleScope.launch {
//                val result = viewModel.insertPatientMediaDataAndParseResponse(requireContext(), selectedFile)
//
//                val testType = binding.spinnerTestType.selectedItem?.let {
//                    if (it is UploadReportItem) it.title else it.toString()
//                } ?: "Unknown"
//
//                val testName = binding.etTestName.text.toString()
//                val imagePath = selectedFile.absolutePath
//
//                val bundle = Bundle().apply {
//                    putSerializable("parsedData", ArrayList(result))
//                    putString("testType", testType)
//                    putString("testName", testName)
//                    putString("imagePath", imagePath)
//                    putString("dateTime", binding.etDate.text.toString())
//                }
//
//                findNavController().navigate(R.id.action_uploadReport_to_reportFieldsFragment, bundle)
//
//                Log.d("ParsedReportData", result.toString())
//            }
//        }
        binding.btnUploadSave.setOnClickListener {
            val selectedFile = imageFile // Check if image is selected or captured
            // Validate that the date is not empty and is a valid date
            val selectedDate = binding.etDate.text.toString().trim()
            if (selectedDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a valid date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate if the date is valid (Optional: You could use a specific date format here)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                dateFormat.parse(selectedDate) // Try to parse the date
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate that the test name is not empty
            val testName = binding.etTestName.text.toString().trim()
            if (testName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a test name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ensure that the selectedFile is not null and exists before proceeding
            if (selectedFile == null || !selectedFile.exists() ) {
                // Display message to the user if no file is selected or captured
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit the method to prevent further action
            }

            lifecycleScope.launch {
                // Proceed with uploading the file since it's selected and valid
                val result = viewModel.insertPatientMediaDataAndParseResponse(requireContext(), selectedFile)

                // Get selected test type, and fallback to "Unknown" if not selected
                val testType = binding.spinnerTestType.selectedItem?.let {
                    if (it is UploadReportItem) it.title else it.toString()
                } ?: "Unknown"

                // Get the test name from the EditText
                val testName = binding.etTestName.text.toString()

                // Get the image path
                val imagePath = selectedFile.absolutePath
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                // Prepare the bundle for navigation
                val bundle = Bundle().apply {
                    putSerializable("parsedData", ArrayList(result)) // Serialized result data
                    putString("testType", testType) // Test type
                    putString("testName", testName) // Test name
                    putString("imagePath", imagePath) // Path to the uploaded image
                    putString("dateTime", binding.etDate.text.toString()+' '+currentTime) // DateTime string
                }

                // Navigate to the next fragment with the uploaded data
                findNavController().navigate(R.id.action_uploadReport_to_reportFieldsFragment, bundle)

                // Log the parsed data (for debugging purposes)
                Log.d("ParsedReportData", result.toString())
            }
        }
    }
}
