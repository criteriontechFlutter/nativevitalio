package com.critetiontech.ctvitalio.UI.ui.signupFragment

import PrefsManager
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.signupFragment.ProgressViewModel
import com.critetiontech.ctvitalio.databinding.FragmentAddYourPhotoBinding
import com.critetiontech.ctvitalio.databinding.FragmentWeightBinding
import java.io.File

class AddYourPhoto : Fragment() {
    private lateinit var binding: FragmentAddYourPhotoBinding
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddYourPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the Spinner
//        val items = listOf("Kg" )  // Units list
//        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
//        adapter.setDropDownViewResource(R.layout.spinner_item)
//        binding.spinnerUnit.adapter = adapter  // Bind the adapter to the spinner

        // Get the ViewModel instances

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]
        // Restore previously entered weight if any
        binding.nameId.text= PrefsManager().getPatient()?.patientName.toString()
        binding.testid.text= PrefsManager().getPatient()?.empId.toString()

        viewModel.selectedImageUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.profileImageView.setImageURI(it)
            }
        }
        binding.profileImageContainer.setOnClickListener(){
            showImagePickerOptions()
         }

        binding.btnNext.setOnClickListener(){

            progressViewModel.updateProgress(7)
            progressViewModel.updatepageNo(7)

            findNavController().navigate(R.id.action_addYourPhoto_to_setYourOwnGoal)
        }
    }
    private fun showImagePickerOptions() {
        val options = arrayOf("Camera", "Gallery")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Profile Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }
    private var tempImageUri: Uri? = null
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val imageFile = File.createTempFile("profile_", ".jpg", requireContext().cacheDir)
            tempImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                imageFile
            )
            cameraLauncher.launch(tempImageUri)
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }
    // Open Gallery
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setSelectedImage(it) // store in ViewModel
        }
    }
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    // Open Camera
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let {
                viewModel.setSelectedImage(it) // store in ViewModel
            }
        }
    }
}