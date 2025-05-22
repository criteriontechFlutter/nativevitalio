package com.critetiontech.ctvitalio.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.fragments.VitalHistoryFragment
import androidx.appcompat.app.AppCompatActivity
sealed class ChatItem {
    data class Message(val text: String, val isUser: Boolean) : ChatItem()
    object MainButtons : ChatItem()
    object VitalOptions : ChatItem()
    object SymptomOptions : ChatItem()
    data class History(val type: String, val entries: List<String>) : ChatItem()
    object VitalForm : ChatItem()
    object SymptomForm : ChatItem()
    object VitalHistoryView : ChatItem()
}

class ChatBotPageAdapter(
    private val onEvent: (ChatItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<ChatItem>()

    fun submitList(newList: List<ChatItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ChatItem.Message -> if ((items[position] as ChatItem.Message).isUser) 0 else 1
        is ChatItem.MainButtons -> 2
        is ChatItem.VitalOptions -> 3
        is ChatItem.SymptomOptions -> 4
        is ChatItem.VitalForm -> 5
        is ChatItem.SymptomForm -> 6
        is ChatItem.History -> 7
        is ChatItem.VitalHistoryView -> 8
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> UserMessageViewHolder(inflater.inflate(R.layout.item_user_message, parent, false))
            1 -> BotMessageViewHolder(inflater.inflate(R.layout.item_bot_message, parent, false))
            2 -> MainButtonsViewHolder(inflater.inflate(R.layout.item_main_buttons, parent, false))
            3 -> VitalOptionsViewHolder(inflater.inflate(R.layout.item_vital_options, parent, false))
            5 -> VitalFormViewHolder(inflater.inflate(R.layout.item_vital_form, parent, false))
            8 -> VitalHistoryFragmentViewHolder(inflater.inflate(R.layout.item_vital_history_embed, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind((item as ChatItem.Message).text)
            is BotMessageViewHolder -> holder.bind((item as ChatItem.Message).text)
            is MainButtonsViewHolder -> holder.bind(onEvent)
            is VitalOptionsViewHolder -> holder.bind(onEvent)
            is VitalFormViewHolder -> holder.bind(onEvent)
            is VitalHistoryFragmentViewHolder -> holder.bind((holder.itemView.context as AppCompatActivity).supportFragmentManager)
        }
    }

    class UserMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val msg = view.findViewById<TextView>(R.id.tvUserMsg)
        fun bind(text: String) { msg.text = text }
    }

    class BotMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val msg = view.findViewById<TextView>(R.id.tvBotMsg)
        fun bind(text: String) { msg.text = text }
    }

    class MainButtonsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            val btnVital = itemView.findViewById<Button>(R.id.btnVital)
            val btnSymptoms = itemView.findViewById<Button>(R.id.btnSymptoms)
            val btnFluid = itemView.findViewById<Button>(R.id.btnFluid)
            val btnDiet = itemView.findViewById<Button>(R.id.btnDiet)

            btnVital.setOnClickListener { handleSelection("Vital", onEvent, ChatItem.VitalOptions) }
            btnSymptoms.setOnClickListener { handleSelection("Symptoms", onEvent, ChatItem.SymptomOptions) }
            btnFluid.setOnClickListener { handleSelection("Fluid", onEvent, ChatItem.Message("Fluid feature coming soon", false)) }
            btnDiet.setOnClickListener { handleSelection("Diet", onEvent, ChatItem.Message("Diet feature coming soon", false)) }
        }

        private fun handleSelection(option: String, onEvent: (ChatItem) -> Unit, nextItem: ChatItem) {
            onEvent(ChatItem.Message("You selected: $option", isUser = false))
            onEvent(ChatItem.Message("Do you want to add a new $option or view $option history?", isUser = true))
            onEvent(nextItem)
        }
    }

    class VitalOptionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            itemView.findViewById<Button>(R.id.btnAddVital).setOnClickListener { onEvent(ChatItem.VitalForm) }
            itemView.findViewById<Button>(R.id.btnHistoryVital).setOnClickListener {
                onEvent(ChatItem.VitalHistoryView)
            }
        }
    }

    class VitalFormViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            val etBp = itemView.findViewById<EditText>(R.id.etBp)
            val etHr = itemView.findViewById<EditText>(R.id.etHr)
            val btnSave = itemView.findViewById<Button>(R.id.btnSaveVital)

            btnSave.setOnClickListener {
                val bp = etBp.text.toString()
                val hr = etHr.text.toString()
                onEvent(ChatItem.Message("Saved Vital: BP = $bp, HR = $hr", isUser = false))
            }
        }
    }

    class VitalHistoryFragmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(fragmentManager: FragmentManager) {
            val containerId = R.id.vitalHistoryFragmentHolder
            R.id.backBtn
            val fragment = VitalHistoryFragment().apply {
                arguments = Bundle().apply {
                    putString("vitalType", "Blood Pressure")
                    putString("itemData", "(Blood Pressure, 120/80, 2024-05-21)")
                }
            }


            // Post to ensure the view is laid out before transaction
            itemView.post {
                if (fragmentManager.findFragmentById(containerId) == null) {
                    fragmentManager.beginTransaction()
                        .replace(containerId, fragment, "vital_history_${adapterPosition}")
                        .commitAllowingStateLoss()
                }
            }
        }
    }
}
