package com.critetiontech.ctvitalio.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.SymptomDetail
import com.critetiontech.ctvitalio.model.SymptomItem
import com.critetiontech.ctvitalio.utils.FilterType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SymptomHistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val itemList = mutableListOf<SymptomItem>()

    @RequiresApi(Build.VERSION_CODES.O)
    fun submitGroupedList(symptoms: List<SymptomDetail>, filterType: FilterType) {
        val groupedItems = symptoms
            .filter { symptom ->
                val date = try {
                    LocalDate.parse(symptom.detailsDate.substring(0, 10), DateTimeFormatter.ISO_DATE)
                } catch (e: Exception) {
                    null
                }

                // Apply date filter based on filter type
                when (filterType) {
                    FilterType.DAILY -> date?.isEqual(LocalDate.now()) == true
                    FilterType.WEEKLY -> date?.isAfter(LocalDate.now().minusWeeks(1)) == true
                    FilterType.MONTHLY -> date?.month == LocalDate.now().month && date?.year == LocalDate.now().year
                    else -> true
                }
            }
            .sortedByDescending { it.detailsDate }
            .groupBy {
                val date = LocalDate.parse(it.detailsDate.substring(0, 10), DateTimeFormatter.ISO_DATE)
                date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))
            }
            .flatMap { (date, items) ->
                listOf(SymptomItem.DateHeader(date)) + items.map { SymptomItem.Symptom(it) }
            }

        itemList.clear()
        itemList.addAll(groupedItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is SymptomItem.DateHeader -> 0
            is SymptomItem.Symptom -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_symptom_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_symptom_card, parent, false)
            SymptomViewHolder(view)
        }
    }

    override fun getItemCount(): Int = itemList.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = itemList[position]) {
            is SymptomItem.DateHeader -> (holder as HeaderViewHolder).bind(item)
            is SymptomItem.Symptom -> (holder as SymptomViewHolder).bind(item.detail)
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDateHeader: TextView = view.findViewById(R.id.tvDateHeader)

        fun bind(item: SymptomItem.DateHeader) {
            tvDateHeader.text = item.date
        }
    }

    class SymptomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSymptomName: TextView = view.findViewById(R.id.tvSymptomName)
        private val tvSymptomTime: TextView = view.findViewById(R.id.tvTime)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: SymptomDetail) {
            tvSymptomName.text = item.details
            try {
                // Extract and format the time from the detailsDate
                val time = LocalTime.parse(item.detailsDate.substring(11, 19))
                tvSymptomTime.text = time.format(DateTimeFormatter.ofPattern("hh:mm a"))
            } catch (e: Exception) {
                tvSymptomTime.text = ""
            }
        }
    }
}