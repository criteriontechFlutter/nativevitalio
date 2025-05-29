package com.critetiontech.ctvitalio.UI.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import android.widget.ImageView
//import com.github.barteksc.pdfviewer.PDFView

class ImagePdfViewFragment : Fragment() {

    private lateinit var imageView: ImageView
//    private lateinit var pdfView: PDFView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_image_pdf_view, container, false)

        imageView = binding.findViewById(R.id.imageView)
//        pdfView = binding.findViewById(R.id.pdfView)

        // Get file URI passed as argument
        val fileUri: Uri? = arguments?.getParcelable("fileUri")

        if (fileUri != null) {
            val fileExtension = getFileExtension(fileUri)

            if (fileExtension.equals("pdf", ignoreCase = true)) {
                // Show PDF
                imageView.visibility = View.GONE
//                pdfView.visibility = View.VISIBLE
                loadPdf(fileUri)
            } else {
                // Show Image
//                pdfView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                loadImage(fileUri)
            }
        }

        return binding
    }

    private fun loadPdf(uri: Uri) {
        // Load PDF into PDFView
//        pdfView.fromUri(uri)
//            .enableSwipe(true) // Allows to swipe to change pages
//            .swipeHorizontal(false)
//            .enableDoubletap(true)
//            .load()
    }

    private fun loadImage(uri: Uri) {
        // Load image into ImageView using Glide
        Glide.with(requireContext())
            .load(uri)
            .into(imageView)
    }

    // Get the file extension (pdf, jpg, etc.)
    private fun getFileExtension(uri: Uri): String {
        val path = uri.path ?: return ""
        return path.substring(path.lastIndexOf(".") + 1)
    }

    companion object {
        fun newInstance(uri: Uri): ImagePdfViewFragment {
            val fragment = ImagePdfViewFragment()
            val args = Bundle()
            args.putParcelable("fileUri", uri)
            fragment.arguments = args
            return fragment
        }
    }
}