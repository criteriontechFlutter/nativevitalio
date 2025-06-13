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
object ImagePickerUtil {

    const val REQUEST_CODE_PICK_IMAGE = 1001
    private var imageUri: Uri? = null
    private var callback: ((Uri?) -> Unit)? = null

    private const val REQUEST_CAMERA = 1001
    private var tempImageUri: Uri? = null


    fun pickImage(context: Context, activityOrFragment: Any, resultCallback: (Uri?) -> Unit) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }

        val intents = mutableListOf<Intent>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val photoFile = File.createTempFile("IMG_", ".jpg", (context as Activity).cacheDir)
            imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intents.add(cameraIntent)
        }

        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
        }

        callback = resultCallback

        when (activityOrFragment) {
            is Activity -> activityOrFragment.startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE)
            is Fragment -> activityOrFragment.startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE)
            else -> throw IllegalArgumentException("Invalid caller")
        }
    }
    fun takePhoto(context: Context, fragment: Fragment, cb: (Uri?) -> Unit) {
        callback = cb
        val imageFile = File(context.cacheDir, "temp_image.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
        tempImageUri = uri
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        fragment.startActivityForResult(cameraIntent, REQUEST_CAMERA)
    }
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = data?.data ?: imageUri
                callback?.invoke(resultUri)
            } else {
                callback?.invoke(null) // Cancelled or failed
            }
        }
    }
}
