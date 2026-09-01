package com.critetiontech.ctvitalio.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import android.Manifest

/**
 * ImagePickerUtil.kt
 *
 * Utility object for picking images from the gallery or taking a camera photo.
 *
 * Usage:
 *   // From a Fragment, pick gallery or camera:
 *   ImagePickerUtil.pickImage(requireContext(), this) { uri -> ... }
 *
 *   // Camera only:
 *   ImagePickerUtil.takePhoto(requireContext(), this) { uri -> ... }
 *
 *   // In onActivityResult:
 *   ImagePickerUtil.handleActivityResult(requestCode, resultCode, data)
 *
 * Request codes are defined in [AppConstants].
 */
object ImagePickerUtil {

    private var imageUri: Uri? = null
    private var tempImageUri: Uri? = null
    private var callback: ((Uri?) -> Unit)? = null

    /** Opens a chooser allowing the user to pick from gallery or take a photo. */
    fun pickImage(context: Context, activityOrFragment: Any, resultCallback: (Uri?) -> Unit) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .apply { type = "image/*" }

        val extraIntents = mutableListOf<Intent>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val photoFile = File.createTempFile("IMG_", ".jpg", (context as Activity).cacheDir)
            imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
            extraIntents += Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        callback = resultCallback
        val chooser = Intent.createChooser(galleryIntent, "Select Image").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents.toTypedArray())
        }

        when (activityOrFragment) {
            is Activity -> activityOrFragment.startActivityForResult(chooser, AppConstants.REQUEST_CODE_PICK_IMAGE)
            is Fragment  -> activityOrFragment.startActivityForResult(chooser, AppConstants.REQUEST_CODE_PICK_IMAGE)
            else -> throw IllegalArgumentException("activityOrFragment must be an Activity or Fragment")
        }
    }

    /** Opens the camera directly to capture a photo. */
    fun takePhoto(context: Context, fragment: Fragment, cb: (Uri?) -> Unit) {
        callback = cb
        val imageFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
        tempImageUri = uri

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        fragment.startActivityForResult(cameraIntent, AppConstants.REQUEST_CODE_CAMERA)
    }

    /** Must be called from the host Activity/Fragment's onActivityResult. */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AppConstants.REQUEST_CODE_PICK_IMAGE -> {
                callback?.invoke(
                    if (resultCode == Activity.RESULT_OK) data?.data ?: imageUri else null
                )
            }
            AppConstants.REQUEST_CODE_CAMERA -> {
                callback?.invoke(
                    if (resultCode == Activity.RESULT_OK) tempImageUri else null
                )
            }
        }
    }
}
