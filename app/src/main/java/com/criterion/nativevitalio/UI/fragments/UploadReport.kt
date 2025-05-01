package com.criterion.nativevitalio.UI.fragments

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
import com.criterion.nativevitalio.adapter.UploadReportAdapter
import com.criterion.nativevitalio.model.UploadReportItem
import com.criterion.nativevitalio.utils.FileUtil
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentUploadReportBinding
import com.criterion.nativevitalio.viewmodel.UploadReportViewModel
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
            if (success && imageUri != null && imageFile?.exists() == true) {
                launchCropper(imageUri!!)
            } else {
                Toast.makeText(requireContext(), "Capture failed", Toast.LENGTH_SHORT).show()
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
            UploadReportItem("Radiology", R.drawable.reminders),
            UploadReportItem("Imaging", R.drawable.ic_list_icon),
            UploadReportItem("Lab", R.drawable.ic_list_icon)
        )
        binding.spinnerTestType.adapter = UploadReportAdapter(requireContext(), upperList)

        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Report Date")
                .build()

            datePicker.show(parentFragmentManager, "date_picker")

            datePicker.addOnPositiveButtonClickListener { selectedDateMillis ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = selectedDateMillis
                }

                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(requireContext(), { _, h, m ->
                    calendar.set(Calendar.HOUR_OF_DAY, h)
                    calendar.set(Calendar.MINUTE, m)

                    val formatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(calendar.time)

                    binding.etDate.setText(formatted)
                }, hour, minute, false).apply {
                    setTitle("Select Time")
                    show()
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

        binding.btnUploadSave.setOnClickListener {
            val selectedFile = imageFile ?: run {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = viewModel.insertPatientMediaDataAndParseResponse(requireContext(), selectedFile)

                val testType = binding.spinnerTestType.selectedItem?.let {
                    if (it is UploadReportItem) it.title else it.toString()
                } ?: "Unknown"

                val testName = binding.etTestName.text.toString()
                val imagePath = selectedFile.absolutePath

                val bundle = Bundle().apply {
                    putSerializable("parsedData", ArrayList(result))
                    putString("testType", testType)
                    putString("testName", testName)
                    putString("imagePath", imagePath)
                    putString("dateTime", binding.etDate.text.toString())
                }

                findNavController().navigate(R.id.action_uploadReport_to_reportFieldsFragment, bundle)

                Log.d("ParsedReportData", result.toString())
            }
        }
    }
}
