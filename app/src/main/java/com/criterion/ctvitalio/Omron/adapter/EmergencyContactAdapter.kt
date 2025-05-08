package com.critetiontech.ctvitalio.Omron.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.ItemEmergencyContactBinding
import com.critetiontech.ctvitalio.model.EmergencyContact

class EmergencyContactAdapter(
    private var contactList: List<EmergencyContact>,
    private val onCallClicked: (EmergencyContact) -> Unit
) : RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(val binding: ItemEmergencyContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: EmergencyContact) {
            binding.contactName.text = contact.contactName
            binding.contactNumber.text = contact.contactNumber
            binding.contactRelation.text = contact.relationship

            binding.btnCall.setOnClickListener {
                onCallClicked(contact)
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
