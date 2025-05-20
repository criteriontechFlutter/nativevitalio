package com.critetiontech.ctvitalio.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File

object ImagePickerUtil {

    private const val REQUEST_CODE_PICK_IMAGE = 101
    private var imageUri: Uri? = null
    private var callback: ((Uri?) -> Unit)? = null

    fun pickImage(context: Context, activityOrFragment: Any, resultCallback: (Uri?) -> Unit) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"

        // Temp file to store camera image
        val photoFile = File.createTempFile("IMG_", ".jpg", (context as Activity).cacheDir)
        imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        callback = resultCallback

        when (activityOrFragment) {
            is Activity -> activityOrFragment.startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE)
            is Fragment -> activityOrFragment.startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE)
            else -> throw IllegalArgumentException("Invalid caller")
        }
    }

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedUri = data?.data ?: imageUri
            callback?.invoke(selectedUri)
            callback = null // clear after use
        }
    }
}
