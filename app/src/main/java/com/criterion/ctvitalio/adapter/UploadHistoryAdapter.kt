package com.critetiontech.ctvitalio.adapter

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
    private val reports: List<UploadedReportItem>
) : RecyclerView.Adapter<UploadHistoryAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
//        val tvRemark: TextView = itemView.findViewById(R.id.tvRemark)
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
            .error(com.google.android.material.R.drawable.mtrl_ic_error)             // Fallback if image fails
            .into(holder.imagePreview)

        // Optional: Set other fields
        holder.tvTitle.text = report.subCategory
//        holder.tvRemark.text = report.remark
        holder.tvDate.text = report.dateTime.take(10)
    }
    override fun getItemCount(): Int = reports.size

    private fun formatDateTime(raw: String?): String {
        return raw?.substringBefore("T") ?: "Unknown Date"
    }
}