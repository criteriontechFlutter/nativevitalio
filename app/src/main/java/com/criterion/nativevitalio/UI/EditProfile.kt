package com.critetiontech.ctvitalio.UI

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.critetiontech.ctvitalio.R

class EditProfile : AppCompatActivity() {

    data class BloodGroup(val id: Int, val name: String)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        val spinner: Spinner = findViewById(R.id.spinnerBloodGroup)
//
//// List of blood groups
//        val bloodGroupList = listOf(
//            BloodGroup(7, "AB+"),
//            BloodGroup(8, "AB-"),
//            BloodGroup(1, "A+"),
//            BloodGroup(2, "A-"),
//            BloodGroup(3, "B+"),
//            BloodGroup(4, "B-"),
//            BloodGroup(5, "O+"),
//            BloodGroup(6, "O-")
//        )

// Adapter with just the display names
//        val adapter = ArrayAdapter(
//            this,
//            android.R.layout.simple_spinner_item,
//            bloodGroupList.map { it.name } // we extract the name for display
//        )
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinner.adapter = adapter
//




//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>, view: View?, position: Int, id: Long
//            ) {
//                val selectedGroup = bloodGroupList[position]
//                val selectedId = selectedGroup.id
//                val selectedName = selectedGroup.name
//
//                Log.d("Selected", "ID: $selectedId, Name: $selectedName")
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Handle case if nothing is selected (optional)
//            }
//        }
    }
}