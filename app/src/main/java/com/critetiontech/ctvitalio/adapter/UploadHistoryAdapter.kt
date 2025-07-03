package com.critetiontech.ctvitalio.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.model.UploadedReportItem
import com.critetiontech.ctvitalio.R


class UploadHistoryAdapter(
    private val reports: List<UploadedReportItem>,
    private val onImageClick: (Uri) -> Unit // Callback to handle image click
) : RecyclerView.Adapter<UploadHistoryAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val imagePreview: ImageView = itemView.findViewById(R.id.imagePreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.report_item, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]

        // Clean the backslashes from the image URL
        val cleanedUrl = report.url.replace("\\", "/")

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(cleanedUrl)
            .placeholder(R.drawable.uploadreport) // Show while loading
            .error(R.drawable.uploadreport) // Show while loading
            .into(holder.imagePreview)

        // Set the title and date
        holder.tvTitle.text = report.subCategory
        holder.tvDate.text = formatDateTime(report.dateTime)

        // Set click listener on the imagePreview
        holder.imagePreview.setOnClickListener {
            // You can trigger an action on image click here, e.g., open the image in full screen
            val fileUri = Uri.parse(cleanedUrl) // Example: converting the image URL to URI
            onImageClick(fileUri) // Call the provided callback
        }
    }

    override fun getItemCount(): Int = reports.size

    // Format date in a user-friendly way
    private fun formatDateTime(raw: String?): String {
        return try {
            val dateTime = raw?.substringBefore("T")
            dateTime ?: "Unknown Date"
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
}