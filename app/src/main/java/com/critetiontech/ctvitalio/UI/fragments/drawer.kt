package com.critetiontech.ctvitalio.UI.fragments

import PrefsManager
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.BaseActivity
import com.critetiontech.ctvitalio.databinding.FragmentDrawerBinding
import com.critetiontech.ctvitalio.utils.FileUtil
import com.critetiontech.ctvitalio.utils.ImagePickerUtil
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.DrawerViewModel
import com.critetiontech.ctvitalio.viewmodel.LoginViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class drawer : Fragment() {


    private lateinit var binding: FragmentDrawerBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var drawerViewModel: DrawerViewModel

    private var imageUri: Uri? = null
    private var imageFile: File? = null

//    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        uri?.let {
//            launchCropper(it)
//        }
//    }

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful && result.uriContent != null) {
            imageUri = result.uriContent
            imageFile = FileUtil.from(requireContext(), result.uriContent!!)

            val compressedUri = compressImageUnder8MB(requireContext(), result.uriContent!!)
            val finalUri = compressedUri ?: result.uriContent

            drawerViewModel.updateUserData(requireContext(), finalUri)
            binding.userImage.setImageURI(finalUri)
        } else {
            Toast.makeText(requireContext(), result.error?.message ?: "Cropping failed", Toast.LENGTH_SHORT).show()
            Log.e("CropError", "Cropping failed", result.error)
        }
    }

    private fun launchCropper(sourceUri: Uri) {
        val fileUri = getFileUriFromContentUri(sourceUri)
        val cropOptions = CropImageOptions().apply {
            activityTitle = "Crop Profile Image"
            cropMenuCropButtonTitle = "Crop"
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
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
    }

//    private fun openGallery() {
//        galleryLauncher.launch("image/*")
//    }

//    private fun launchCamera() {
//        ImagePickerUtil.takePhoto(requireContext(), this) { uri ->
//            uri?.let {
//                launchCropper(it)  // ✅ Crop the taken photo just like gallery image
//            }
//        }
//    }

    private fun showProfileImageBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.upload_profile_img, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(bottomSheetView)

        val btnTake = bottomSheetView.findViewById<LinearLayout>(R.id.imgTake)
        val btnUpload = bottomSheetView.findViewById<LinearLayout>(R.id.imgUploadimg)

        btnTake.setOnClickListener {
            dialog.dismiss()
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
//                launchCamera()
                openCamera()
            }
        }

        btnUpload.setOnClickListener {
            dialog.dismiss()
            openGallery()
        }

        dialog.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        drawerViewModel = ViewModelProvider(this)[DrawerViewModel::class.java]
        (requireActivity() as? BaseActivity)?.setSystemBarsColor(
            statusBarColor = R.color.white,
            navBarColor = R.color.white,
            lightIcons = true
        )
        setupObservers()
        setupListeners()
        initDrawerLayout()

        val patient = PrefsManager().getPatient()
        binding.userName.text = patient?.patientName ?: ""
        binding.userUhid.text = patient?.uhID ?: ""

        Glide.with(MyApplication.appContext)
            .load("http://182.156.200.177:5082/"+PrefsManager().getPatient()?.imageURL.toString())
            .placeholder(R.drawable.baseline_person_24)
            .circleCrop()
            .into(binding.userImage)
//        binding.avatar.setOnClickListener {
//            findNavController().navigate(R.id.action_dashboard_to_drawer4)
//        }



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
            drawerViewModel.setSelectedImage(it) // store in ViewModel

            drawerViewModel.updateUserData(requireContext(), it)
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
                drawerViewModel.setSelectedImage(it) // store in ViewModel

                drawerViewModel.updateUserData(requireContext(), it)
            }
        }
    }

    private fun setupObservers() {
        loginViewModel.finishEvent.observe(viewLifecycleOwner) { shouldFinish ->
            if (shouldFinish) requireActivity().finish()
        }
    }

    private fun setupListeners() {
        binding.editIcon.setOnClickListener { showProfileImageBottomSheet() }

        binding.btnEditProfile.setOnClickListener {
            val bundle = Bundle().apply { putString("isProfile", "1") }
            findNavController().navigate(R.id.action_drawer4_to_editProfile3, bundle)
        }

        binding.personalInfoRow.root.setOnClickListener {
            val bundle = Bundle().apply { putString("isProfile", "0") }
            findNavController().navigate(R.id.action_drawer4_to_editProfile3, bundle)
        }

        binding.allergiesRow.root.setOnClickListener {
            findNavController().navigate(R.id.action_drawer4_to_allergies3)
        }

        binding.emergencyContactRow.root.setOnClickListener {
            findNavController().navigate(R.id.action_drawer4_to_emergencyContactFragment)
        }

        binding.darkModeRow.root.setOnClickListener {
            findNavController().navigate(R.id.action_drawer4_to_settingsFragmentVitalio)
        }

        binding.connectSmartWatchRow.root.setOnClickListener {
            findNavController().navigate(R.id.action_drawer4_to_connectSmartWatchFragment)
        }

        binding.backDrawer.setOnClickListener { findNavController().popBackStack() }

        binding.logoutMenu.setOnClickListener { showLogoutPopup() }






    }

    private fun showLogoutPopup() {
        val popupView = LayoutInflater.from(context).inflate(R.layout.layout_logout_popup, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply { elevation = 10f }

        popupView.findViewById<View>(R.id.logoutText).setOnClickListener {
            popupWindow.dismiss()
            showLogoutDialog()
        }

        popupWindow.showAsDropDown(binding.logoutMenu, -200, -75)
    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_logout_app, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.BottomDialogTheme)
            .setView(dialogView)
            .create()

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = Resources.getSystem().displayMetrics.widthPixels
            val margin = (40 * Resources.getSystem().displayMetrics.density).toInt()
            setLayout(width - 2 * margin, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER_VERTICAL)
        }

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnRemove).setOnClickListener {
            dialog.dismiss()
            loginViewModel.logoutFromApp(PrefsManager().getPatient()?.uhID.orEmpty(), "")
        }

        dialog.show()
    }

    private fun compressImageUnder8MB(context: Context, imageUri: Uri): Uri? {
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val resizedBitmap = Bitmap.createScaledBitmap(
            originalBitmap,
            minOf(originalBitmap.width, 2048),
            minOf(originalBitmap.height, 2048),
            true
        )

        val outputStream = ByteArrayOutputStream()
        var quality = 100
        var byteArray: ByteArray

        do {
            outputStream.reset()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            byteArray = outputStream.toByteArray()
            quality -= 5
        } while (byteArray.size > 8 * 1024 * 1024 && quality > 5)

        return if (byteArray.size <= 8 * 1024 * 1024) {
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { it.write(byteArray) }
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } else {
            null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initDrawerLayout() {
        binding.personalInfoRow.apply {
            title.text = getString(R.string.personal_info)
            icon.setImageResource(R.drawable.ic_personal_info)
        }

        binding.allergiesRow.apply {
            title.text = getString(R.string.allergies)
            icon.setImageResource(R.drawable.ic_allergies)
            count.text = PrefsManager().getAllergies().takeIf { it != "0" } ?: ""
        }

        binding.emergencyContactRow.apply {
            title.text = getString(R.string.emergency_contacts)
            icon.setImageResource(R.drawable.ic_emergency_contact)
            count.text = PrefsManager().getEmergency().takeIf { it != "0" } ?: ""
        }

        binding.sharedAccountRow.apply {
            title.text = getString(R.string.shared_accounts)
            icon.setImageResource(R.drawable.ic_shared)
        }

        binding.connectSmartWatchRow.apply {
            title.text = getString(R.string.connect_smart_watch)
            icon.setImageResource(R.drawable.ic_smartwatch)
            count.text = PrefsManager().getSmartWatch().takeIf { it != "0" } ?: ""
        }

        binding.languageRow.apply {
            title.text = getString(R.string.language)
            icon.setImageResource(R.drawable.ic_language)
            count.text = "English"
        }

        binding.darkModeRow.apply {
            title.text = getString(R.string.dark_mode)
            icon.setImageResource(R.drawable.ic_theme)
        }

        binding.FAQsRow.apply {
            title.text = getString(R.string.f_q)
            icon.setImageResource(R.drawable.ic_faqs)
        }

        binding.feedbackRow.apply {
            title.text = getString(R.string.feedback)
            icon.setImageResource(R.drawable.ic_feedback)
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == CAMERA_PERMISSION_CODE &&
//            grantResults.isNotEmpty() &&
//            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            launchCamera()
//        } else {
//            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
//        }
//    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 101
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePickerUtil.handleActivityResult(requestCode, resultCode, data)
    }
}