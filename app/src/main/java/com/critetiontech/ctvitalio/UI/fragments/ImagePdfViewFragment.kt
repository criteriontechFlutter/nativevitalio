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
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.databinding.FragmentImagePdfViewBinding
import com.critetiontech.ctvitalio.databinding.FragmentUploadReportHistoryBinding

//import com.github.barteksc.pdfviewer.PDFView

class ImagePdfViewFragment : Fragment() {

    private var _binding: FragmentImagePdfViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePdfViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileUriString = arguments?.getString("fileUri")
        val fileUri = fileUriString?.let { Uri.parse(it) }

        fileUri?.let {
            val fileExtension = getFileExtension(it)
            if (fileExtension.equals("pdf", ignoreCase = true)) {
                binding.imageView.visibility = View.GONE
                // TODO: Show PDF if implemented
            } else {
                binding.imageView.visibility = View.VISIBLE
                loadImage(it)
            }
        }

        binding.backIcon.setOnClickListener {
            findNavController().previousBackStackEntry?.savedStateHandle?.set("refreshNeeded", true)
            findNavController().popBackStack()

        }
    }

    private fun loadImage(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .into(binding.imageView)
    }
    private fun getFileExtension(uri: Uri): String {
        val path = uri.path ?: return ""
        return path.substringAfterLast('.', "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(uri: Uri): ImagePdfViewFragment {
            val fragment = ImagePdfViewFragment()
            val args = Bundle().apply {
                putParcelable("fileUri", uri)
            }
            fragment.arguments = args
            return fragment
        }
    }
}