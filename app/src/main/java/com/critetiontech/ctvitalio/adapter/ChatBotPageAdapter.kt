package com.critetiontech.ctvitalio.adapter

import android.os.Bundle
import android.util.Log
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
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.databinding.FragmentVitalHistoryBinding



sealed class ChatItem {
    data class Message(val text: String, val isUser: Boolean) : ChatItem()
    object MainButtons : ChatItem()
    object VitalOptions : ChatItem()
    object SymptomOptions : ChatItem()
    object DietOptions : ChatItem()
    object FluidOptions : ChatItem()
    object PillsReminderOptions : ChatItem()
    object UploadReportOptions : ChatItem()
    data class NavigationRoute(val path: String, val onGoClick: () -> Unit) : ChatItem()
}

fun handleSelection(
    selected: String,
    optionText: String,
    onEvent: (ChatItem) -> Unit,
    nextItem: ChatItem
) {
    onEvent(ChatItem.Message(selected, isUser = false))
    if (optionText.isNotBlank()) {
        onEvent(ChatItem.Message(optionText, isUser = true))
    }
    onEvent(nextItem)
}

class ChatBotPageAdapter(
    private val onEvent: (ChatItem) -> Unit,
    private val onNavigate: (Int) -> Unit
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
        is ChatItem.DietOptions -> 5
        is ChatItem.FluidOptions -> 6
        is ChatItem.PillsReminderOptions -> 7
        is ChatItem.UploadReportOptions -> 8
        is ChatItem.NavigationRoute -> 9
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> UserMessageViewHolder(inflater.inflate(R.layout.item_user_message, parent, false))
            1 -> BotMessageViewHolder(inflater.inflate(R.layout.item_bot_message, parent, false))
            2 -> MainButtonsViewHolder(inflater.inflate(R.layout.item_main_buttons, parent, false))
            3 -> VitalOptionsViewHolder(inflater.inflate(R.layout.item_vital_options, parent, false), onNavigate)
            4 -> SymptomOptionsViewHolder(inflater.inflate(R.layout.item_symptom_options, parent, false), onNavigate)
            5 -> DietOptionsViewHolder(inflater.inflate(R.layout.item_diet_options, parent, false), onNavigate)
            6 -> FluidOptionsViewHolder(inflater.inflate(R.layout.item_fluid_options, parent, false), onNavigate)
            7 -> PillsReminderOptionsViewHolder(inflater.inflate(R.layout.item_pills_options, parent, false), onNavigate)
            8 -> UploadReportOptionsViewHolder(inflater.inflate(R.layout.item_upload_options, parent, false), onNavigate)
            9 -> RouteViewHolder(inflater.inflate(R.layout.item_route, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ChatItem.Message -> {
                if (item.isUser) (holder as UserMessageViewHolder).bind(item.text)
                else (holder as BotMessageViewHolder).bind(item.text)
            }
            is ChatItem.MainButtons -> (holder as MainButtonsViewHolder).bind(onEvent)
            is ChatItem.VitalOptions -> (holder as VitalOptionsViewHolder).bind(onEvent)
            is ChatItem.SymptomOptions -> (holder as SymptomOptionsViewHolder).bind(onEvent)
            is ChatItem.DietOptions -> (holder as DietOptionsViewHolder).bind(onEvent)
            is ChatItem.FluidOptions -> (holder as FluidOptionsViewHolder).bind(onEvent)
            is ChatItem.PillsReminderOptions -> (holder as PillsReminderOptionsViewHolder).bind(onEvent)
            is ChatItem.UploadReportOptions -> (holder as UploadReportOptionsViewHolder).bind(onEvent)
            is ChatItem.NavigationRoute -> (holder as RouteViewHolder).bind(item)
        }
    }

    class UserMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val msg: TextView = view.findViewById(R.id.tvUserMsg)
        fun bind(text: String) { msg.text = text }
    }

    class BotMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val msg: TextView = view.findViewById(R.id.tvBotMsg)
        fun bind(text: String) { msg.text = text }
    }

    class MainButtonsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            itemView.findViewById<Button>(R.id.btnVital).setOnClickListener {
                handleSelection("Vital", "Choose what you want to do with vitals", onEvent, ChatItem.VitalOptions)
            }
            itemView.findViewById<Button>(R.id.btnSymptoms).setOnClickListener {
                handleSelection("Symptoms", "Choose symptoms options", onEvent, ChatItem.SymptomOptions)
            }
            itemView.findViewById<Button>(R.id.btnDiet).setOnClickListener {
                handleSelection("Diet", "Choose diet options", onEvent, ChatItem.DietOptions)
            }
            itemView.findViewById<Button>(R.id.btnFluid).setOnClickListener {
                handleSelection("Fluid", "Choose fluid options", onEvent, ChatItem.FluidOptions)
            }
            itemView.findViewById<Button>(R.id.btnPillsReminder).setOnClickListener {
                handleSelection("Pills Reminder", "Choose pills reminder options", onEvent, ChatItem.PillsReminderOptions)
            }
            itemView.findViewById<Button>(R.id.btnUploadReport).setOnClickListener {
                handleSelection("Upload Report", "Choose report upload options", onEvent, ChatItem.UploadReportOptions)
            }
        }
    }

    class VitalOptionsViewHolder(view: View, private val onNavigate: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            itemView.findViewById<Button>(R.id.btnAddVital).setOnClickListener {
                handleSelection("Add Vital", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Vital Details > Add Vital") {
                        onNavigate(R.id.action_chatBotPage_to_vitalDetail)
                    })
            }
            itemView.findViewById<Button>(R.id.btnHistoryVital).setOnClickListener {
                handleSelection("Vital History", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Vital Details > Click on Vital Container ") {
                        onNavigate(R.id.action_chatBotPage_to_vitalDetail)
                    })
            }
        }
    }

    class SymptomOptionsViewHolder(view: View, private val onNavigate: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            itemView.findViewById<Button>(R.id.btnAddSymptom).setOnClickListener {
                handleSelection("Add Symptom", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Symptoms Tracker > Select Symptoms > Save Symptom") {
                        onNavigate(R.id.action_chatBotPage_to_symptomsFragment)
                    })
            }
            itemView.findViewById<Button>(R.id.btnHistorySymptom).setOnClickListener {
                handleSelection("Symptom History", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Symptoms Tracker > History") {
                        onNavigate(R.id.action_chatBotPage_to_symptomHistory)
                    })
            }
        }
    }

    class DietOptionsViewHolder(view: View, private val onNavigate: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            itemView.findViewById<Button>(R.id.btnAddDiet).setOnClickListener {
                handleSelection("Intake Diet", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Diet Checklist > Click on Intake Icon") {
                        onNavigate(R.id.action_chatBotPage_to_dietChecklist)
                    })
            }
            itemView.findViewById<Button>(R.id.btnHistoryDiet).setOnClickListener {
                handleSelection("Diet History", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Diet Checklist > History") {
                        onNavigate(R.id.action_chatBotPage_to_dietChecklist)
                    })
            }
        }
    }

    class FluidOptionsViewHolder(view: View, private val onNavigate: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            itemView.findViewById<Button>(R.id.btnAddFluid).setOnClickListener {
                handleSelection("Add Fluid", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Fluid Intake/Output > Select Fluid > Add Intake") {
                        onNavigate(R.id.action_chatBotPage_to_fluidFragment)
                    })
            }
            itemView.findViewById<Button>(R.id.btnHistoryFluid).setOnClickListener {
                handleSelection("Fluid History", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Fluid Intake/Output > History") {
                        onNavigate(R.id.action_chatBotPage_to_fluidInputHistoryFragment)
                    })
            }
        }
    }

    class PillsReminderOptionsViewHolder(view: View, private val onNavigate: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            itemView.findViewById<Button>(R.id.btnAddReminder).setOnClickListener {
                handleSelection("Intake Pills", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Pills Reminder > Click on Intake Icon") {
                        //onNavigate(R.id.action_chatBotPage_to_pillsReminder)
                    })
            }
            itemView.findViewById<Button>(R.id.btnHistoryReminder).setOnClickListener {
                handleSelection("Pills History", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Pills Reminder > History") {
                  //      onNavigate(R.id.action_chatBotPage_to_pillsReminder)
                    })
            }
        }
    }

    class UploadReportOptionsViewHolder(view: View, private val onNavigate: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(onEvent: (ChatItem) -> Unit) {
            itemView.findViewById<Button>(R.id.upload_report_img).setOnClickListener {
                handleSelection("Upload Report", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Upload Reports > Add Report") {
                        onNavigate(R.id.action_chatBotPage_to_uploadReport)
                    })
            }
            itemView.findViewById<Button>(R.id.btnReportHistory).setOnClickListener {
                handleSelection("Report History", "Route :-", onEvent,
                    ChatItem.NavigationRoute("Dashboard > Upload Report > History") {
                        onNavigate(R.id.action_chatBotPage_to_uploadReportHistory)
                    })
            }
        }
    }

    class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: ChatItem.NavigationRoute) {
            itemView.findViewById<TextView>(R.id.tvRoute).text = item.path
            itemView.findViewById<Button>(R.id.btnGoRoute).setOnClickListener { item.onGoClick() }
        }
    }
}