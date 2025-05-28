package com.critetiontech.ctvitalio.adapter.omronAdapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ItemEmergencyContactBinding
import com.critetiontech.ctvitalio.model.EmergencyContact

class EmergencyContactAdapter(
    private var contactList: List<EmergencyContact>,
    private val onCallClicked: (EmergencyContact) -> Unit,
    private val onDeleteClicked: (EmergencyContact) -> Unit  // Add a callback for delete
) : RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(val binding: ItemEmergencyContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: EmergencyContact) {
            binding.contactName.text = contact.contactName
            binding.contactNumber.text = contact.contactNumber
            binding.contactRelation.text = contact.relationship

            // Default to show the btnCall, hide btnRemove initially
            binding.btnCall.visibility = View.VISIBLE
            binding.btnRemove.visibility = View.GONE

            // Handle the "Call" button click
            binding.btnCall.setOnClickListener {
                onCallClicked(contact)
            }

            // Toggling visibility of btnCall and btnRemove when threeDotMenu is clicked
            binding.threeDotMenu.setOnClickListener {
                val isRemoveVisible = binding.btnRemove.visibility == View.VISIBLE
                if (isRemoveVisible) {
                    // If the remove button is already visible, hide it and show the call button
                    binding.btnRemove.visibility = View.GONE
                    binding.btnCall.visibility = View.VISIBLE
                } else {
                    // If the remove button is not visible, show it and hide the call button
                    binding.btnRemove.visibility = View.VISIBLE
                    binding.btnCall.visibility = View.GONE
                }
            }

            // Handle the Remove button click (Trigger the delete action)
            binding.btnRemove.setOnClickListener {
                onDeleteClicked(contact)  // Trigger delete callback
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemEmergencyContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contactList[position])
    }

    fun updateList(newList: List<EmergencyContact>) {
        contactList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = contactList.size
}