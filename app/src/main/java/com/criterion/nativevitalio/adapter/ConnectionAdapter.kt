package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.VitalDevice

class ConnectionAdapter (private val devices: List<VitalDevice>) :
    RecyclerView.Adapter<ConnectionAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = "${device.name} ${device.modal}"
        holder.deviceType.text = device.deviceType

        val context = holder.itemView.context
        val imageId = context.resources.getIdentifier(device.image, "drawable", context.packageName)
        holder.deviceImage.setImageResource(imageId)
    }

    override fun getItemCount(): Int = devices.size

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.deviceName)
        val deviceType: TextView = view.findViewById(R.id.deviceType)
        val deviceImage: ImageView = view.findViewById(R.id.deviceImage)
    }
}