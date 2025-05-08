package com.critetiontech.ctvitalio.UI.ui.signupFragment

import DateUtils.showListBottomSheet
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentAdressBinding
import com.critetiontech.ctvitalio.model.CountryModel
import com.critetiontech.ctvitalio.model.StateModel
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class AdressFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var binding: FragmentAdressBinding
    private lateinit var addressData: List<Map<String, Any>>
    private lateinit var viewModel: RegistrationViewModel
    private var selectedCountry: CountryModel? = null
    private var selectedState: StateModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAdressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]
        // Load JSON data

        addressData = loadAddressData(requireContext())
        binding.etCountry.setOnClickListener {
            val countryList = addressData
//            showListBottomSheet(requireContext(), "Select Country", countryList) { selectedIdString ->
//                Log.e("InsertInvestigation", selectedIdString)
//                val selectedId = selectedIdString.toIntOrNull()
//
//                val selectedCountryObj = addressData.find { it.id == selectedId }
//
//                if (selectedCountryObj != null) {
//                    binding.etCountry.setText(selectedCountryObj.countryName)
//                    binding.etState.setText("")
//                    binding.etCity.setText("")
//                    selectedCountry = selectedCountryObj
//
//                    // Call API if needed
//                    // viewModel.getStateMasterByCountryId(selectedCountryObj.id.toString())
//                } else {
//                    Toast.makeText(requireContext(), "Country not found for ID: $selectedIdString", Toast.LENGTH_SHORT).show()
//                }
//            }
            showListBottomSheet(
                context = requireContext(),
                title = "Select Country",
                list = countryList, // List<Map<String, Any>>
                displayKey = "countryName"
            ) { selectedMap ->
                Log.d("UploadSuccess", "Response JSON: $selectedMap")
                val name = selectedMap["countryName"]?.toString()
                val id = selectedMap["id"]?.toString()
                binding.etCountry.setText(name)
                viewModel.getStateMasterByCountryId(id.toString().split(".")[0])
                viewModel.getCityMasterByStateId("10")
            }
        }

        binding.etState.setOnClickListener {
//            val stateList = selectedCountry?.states?.map { it.name } ?: emptyList()
//            showListBottomSheet(requireContext(), "Select State", stateList) { selected ->
//                binding.etState.setText(selected)
//                binding.etCity.setText("")
//                selectedState = selectedCountry?.states?.find { it.name == selected }
//            }
            viewModel.getCityMasterByStateId("10")
        }

        binding.etCity.setOnClickListener {
            val cityList = selectedState?.cities ?: emptyList()
//            showListBottomSheet(requireContext(), "Select City", cityList) {
//                binding.etCity.setText(it)
//            }
        }

        binding.btnNext.setOnClickListener {
            if (binding.etCountry.text.isNullOrEmpty() ||
                binding.etState.text.isNullOrEmpty() ||
                binding.etCity.text.isNullOrEmpty()
            ) {
                Toast.makeText(requireContext(), "Please complete address fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_adressFragment_to_weightFragment)
        }
    }

    fun loadAddressData(context: Context): List<Map<String, Any>> {
        val json = context.assets.open("country_json.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return Gson().fromJson(json, type)
    }
}