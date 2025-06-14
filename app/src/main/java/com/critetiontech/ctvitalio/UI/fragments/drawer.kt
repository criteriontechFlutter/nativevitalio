package com.critetiontech.ctvitalio.UI.fragments

import PrefsManager
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentDrawerBinding
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
    private lateinit var viewModel: LoginViewModel
    private lateinit var drawerViewModel: DrawerViewModel
    private   val REQUEST_CODE_PICK_IMAGE = 1001
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawerBinding.inflate(inflater, container, false)
        return binding.root
        // Inflate the layout for this fragment

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        drawerViewModel = ViewModelProvider(this)[DrawerViewModel::class.java]
        binding.allergiesRow.root.setOnClickListener {
            findNavController().navigate(R.id.action_drawer4_to_allergies3)

        }

        binding.emergencyContactRow.root.setOnClickListener {
findNavController().navigate(R.id.action_drawer4_to_emergencyContactFragment)
        }

        activity?.let {
            viewModel.finishEvent.observe(it) { shouldFinish ->
                if (shouldFinish) {
                    requireActivity().finish()
                }
            }
        }

        binding.editIcon.setOnClickListener {
//            val activity = context as? Activity
//            activity?.let {
//                ActivityCompat.requestPermissions(
//                    it,
//                    arrayOf(Manifest.permission.CAMERA),
//                    1001
//                )
//            }
//            ImagePickerUtil.pickImage(requireContext(), this) { uri ->
//                uri?.let {
//                    drawerViewModel.updateUserData(requireContext(), it) // PASS URI
//                    binding.userImage.setImageURI(it)
//                }
//            }
            showProfileImageBottomSheet()
        }

        binding.btnEditProfile.setOnClickListener {
//            val intent = Intent(MyApplication.appContext, EditProfile::class.java)
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            MyApplication.appContext.startActivity(intent)
            val bundle = Bundle().apply {
                putString("isProfile", "1" )
            }
            findNavController().navigate(R.id.action_drawer4_to_editProfile3,bundle)
        }




        binding.personalInfoRow.root.setOnClickListener {
            val bundle = Bundle().apply {
                putString("isProfile", "0" )
            }
            findNavController().navigate(R.id.action_drawer4_to_editProfile3,bundle)
        }

        binding.darkModeRow.root.setOnClickListener {
            //PrefsManager().clearPatient()
            findNavController().navigate(R.id.action_drawer4_to_settingsFragmentVitalio)

        }

        binding.connectSmartWatchRow.root.setOnClickListener {
            findNavController().navigate(R.id.action_drawer4_to_connectSmartWatchFragment)
        }

        binding.userName.text = PrefsManager().getPatient()!!.patientName
        binding.userUhid.text = PrefsManager().getPatient()!!.uhID
        Glide.with(MyApplication.appContext) // or `this` if inside Activity
            .load(PrefsManager().getPatient()!!.profileUrl) // or R.drawable.image
            .placeholder(com.critetiontech.ctvitalio.R.drawable.baseline_person_24)
            .circleCrop() // optional: makes it circular
            .into(binding.userImage)

        binding.backDrawer.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.logoutMenu.setOnClickListener {
            val popupView: View =
                LayoutInflater.from(context).inflate(R.layout.layout_logout_popup, null)
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow.elevation = 10f

            // Optional: handle logout click
            popupView.findViewById<View>(R.id.logoutText).setOnClickListener { view: View? ->
                popupWindow.dismiss()

                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.dialog_logout_app, null)

                val dialog = context?.let { it1 ->
                    AlertDialog.Builder(it1, R.style.BottomDialogTheme)
                        .setView(dialogView)
                        .create()
                }

                dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                val displayMetrics = Resources.getSystem().displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val marginInPx = (40 * displayMetrics.density).toInt() // 40dp margin
                val popupWidth = screenWidth - (2 * marginInPx)
                 // ⚙ Fix width and gravity
                dialog.window?.setLayout(
                    popupWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialog.window?.setGravity(Gravity.CENTER_VERTICAL)

// Button listeners
                dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
                    dialog.dismiss()
                }
                dialogView.findViewById<View>(R.id.btnRemove).setOnClickListener {
                    dialog.dismiss()
                    viewModel.logoutFromApp(PrefsManager().getPatient()!!.uhID,"")
                }


            }


            // Show below the menu icon
            popupWindow.showAsDropDown(binding.logoutMenu, -200, -75)

        }

        initDrawerLayout()

    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 101
    }
    private fun launchCamera() {
        ImagePickerUtil.takePhoto(requireContext(), this) { uri ->
            uri?.let {
                val compressedUri = compressImageUnder8MB(requireContext(), it)
                compressedUri?.let { safeUri ->
                    drawerViewModel.updateUserData(requireContext(), safeUri)
                    binding.userImage.setImageURI(safeUri)
                } ?: run {
                    Toast.makeText(requireContext(), "Image is too large even after compression", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showProfileImageBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.upload_profile_img, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(bottomSheetView)

        val btnTake = bottomSheetView.findViewById<LinearLayout>(R.id.imgTake)
        val btnUpload = bottomSheetView.findViewById<LinearLayout>(R.id.imgUpload)
        val btnRemove = bottomSheetView.findViewById<LinearLayout>(R.id.imgUpload)

        btnTake.setOnClickListener {
            dialog.dismiss()
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                launchCamera()
            }
        }

        btnUpload.setOnClickListener {

            dialog.dismiss()
            ImagePickerUtil.pickImage(requireContext(), this) { uri ->
                uri?.let {
                    val compressedUri = compressImageUnder8MB(requireContext(), it)

                    compressedUri?.let { finalUri ->
                        drawerViewModel.updateUserData(requireContext(), finalUri)
                        binding.userImage.setImageURI(finalUri)
                    } ?: run {
                        Toast.makeText(requireContext(), "Image too large even after compression", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnRemove.setOnClickListener {
            dialog.dismiss()
//            binding.userImage.setImageResource(R.drawable.baseline_person_24)
            Toast.makeText(requireContext(), " ", Toast.LENGTH_SHORT).show()

        }

        dialog.show()
    }



    fun compressImageUnder8MB(context: Context, imageUri: Uri): Uri? {
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val resizedBitmap = Bitmap.createScaledBitmap(
            originalBitmap,
            minOf(originalBitmap.width, 2048),
            minOf(originalBitmap.height, 2048),
            true
        )

        var quality = 100
        var byteArray: ByteArray
        val outputStream = ByteArrayOutputStream()

        do {
            outputStream.reset()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            byteArray = outputStream.toByteArray()
            quality -= 5
        } while (byteArray.size > 8 * 1024 * 1024 && quality > 5)

        return if (byteArray.size <= 8 * 1024 * 1024) {
            // Save to file and return Uri
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { it.write(byteArray) }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } else {
            null // still too big
        }
    }
    @SuppressLint("SetTextI18n")
    private fun initDrawerLayout() {
        // Personal Info
        binding.personalInfoRow.title.text = getString(R.string.personal_info)
        binding.personalInfoRow.icon.setImageResource(R.drawable.ic_personal_info)

        binding.allergiesRow.title.text = getString(R.string.allergies)
        binding.allergiesRow.icon.setImageResource(R.drawable.ic_allergies)
        val allergiesValue = PrefsManager().getAllergies().toString()
        binding.allergiesRow.count.text = if (allergiesValue == "0") "" else allergiesValue

        // Observer & Smartwatch
//        binding.myObserverRow.title.text = getString(R.string.my_observer)
//        binding.myObserverRow.count.text = "4"
//        binding.myObserverRow.icon.setImageResource(R.drawable.ic_myobserver)

        binding.sharedAccountRow.title.text = getString(R.string.shared_accounts)
        binding.sharedAccountRow.icon.setImageResource(R.drawable.ic_shared)

        binding.connectSmartWatchRow.title.text = getString(R.string.connect_smart_watch)
        binding.connectSmartWatchRow.icon.setImageResource(R.drawable.ic_smartwatch)
        val smartWatchRow = PrefsManager().getSmartWatch().toString()
        binding.connectSmartWatchRow.count.text = if (smartWatchRow == "0") "" else smartWatchRow

        binding.emergencyContactRow.title.text = getString(R.string.emergency_contacts)
        binding.emergencyContactRow.icon.setImageResource(R.drawable.ic_emergency_contact)
        val emergencyValue = PrefsManager().getEmergency().toString()
        binding.emergencyContactRow.count.text = if (emergencyValue == "0") "" else emergencyValue
//
//        binding.familyHealthHistoryRow.title.text = getString(R.string.family_health_history)
//        binding.familyHealthHistoryRow.icon.setImageResource(R.drawable.ic_health_history)

        // Settings Section
        binding.languageRow.title.text = getString(R.string.language)
        binding.languageRow.count.text = "English"
        binding.languageRow.icon.setImageResource(R.drawable.ic_language)

        binding.darkModeRow.title.text = getString(R.string.dark_mode)
        binding.darkModeRow.icon.setImageResource(R.drawable.ic_theme)


        binding.FAQsRow.title.text = getString(R.string.f_q)
        binding.FAQsRow.icon.setImageResource(R.drawable.ic_faqs)

        binding.feedbackRow.title.text = getString(R.string.feedback)
        binding.feedbackRow.icon.setImageResource(R.drawable.ic_feedback)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePickerUtil.handleActivityResult(requestCode, resultCode, data)
    }


}