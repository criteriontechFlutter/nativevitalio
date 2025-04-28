package com.criterion.nativevitalio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import com.criterion.nativevitalio.model.UploadReportItem
import com.criterion.nativevitalio.R

class UploadReportAdapter(
    private val context: Context,
    private val itemList: List<UploadReportItem>
) : BaseAdapter() {

    override fun getCount(): Int = itemList.size

    override fun getItem(position: Int): Any = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.upload_report_spinner_item, parent, false)
        val icon = view.findViewById<ImageView>(R.id.imgIcon)
        val title = view.findViewById<TextView>(R.id.tvTitle)

        val item = itemList[position]
        title.text = item.title
        icon.setImageResource(item.iconResId)

        return view
    }
}