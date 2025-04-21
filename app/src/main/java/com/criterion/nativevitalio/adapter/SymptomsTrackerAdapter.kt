package com.criterion.nativevitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.databinding.ItemSymptomTrackerBinding
import com.criterion.nativevitalio.model.SymptomDetail


class SymptomsTrackerAdapter(
    private val symptom: SymptomDetail,
    private val currentIndex: Int,
    private val totalCount: Int,
    private val isLastItem: Boolean, // ✅ NEW
    private val onYesClicked: (SymptomDetail) -> Unit,
    private val onNoClicked: (SymptomDetail) -> Unit,
    private val onBackClicked: () -> Unit,
) : RecyclerView.Adapter<SymptomsTrackerAdapter.SymptomViewHolder>() {

    inner class SymptomViewHolder(val binding: ItemSymptomTrackerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSymptomTrackerBinding.inflate(inflater, parent, false)
        return SymptomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SymptomViewHolder, position: Int) {
        holder.binding.symptom = symptom
        holder.binding.executePendingBindings()

        // Button Actions
        holder.binding.btnYes.setOnClickListener {
            symptom.selection = 1
            onYesClicked(symptom)
        }

        holder.binding.btnNo.setOnClickListener {
            symptom.selection = 0
            onNoClicked(symptom)
        }

//        holder.binding.tvProgress.text = "${currentIndex + 1} of $totalCount"
        holder.binding.btnBack.visibility = if (currentIndex == 0) View.GONE else View.VISIBLE

        holder.binding.btnBack.setOnClickListener {
            onBackClicked()
        }
        holder.binding.btnBack.setOnClickListener {
            onBackClicked()
        }

    }

    override fun getItemCount(): Int = 1
}