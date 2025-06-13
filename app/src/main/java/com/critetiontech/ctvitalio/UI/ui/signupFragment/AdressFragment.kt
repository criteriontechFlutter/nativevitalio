package com.critetiontech.ctvitalio.UI.ui.signupFragment

import DateUtils.showListBottomSheet
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentAdressBinding
import com.critetiontech.ctvitalio.model.CityModel
import com.critetiontech.ctvitalio.model.StateModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class AdressFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var binding: FragmentAdressBinding
    private lateinit var viewModel: RegistrationViewModel
    private var selectedCountryId: String? = null
    private var selectedState: StateModel? = null
    private lateinit var addressData: List<Map<String, Any>>
    private var stateListCache: List<StateModel> = emptyList()
    private var cityListCache: List<CityModel> = emptyList()

    private lateinit var progressViewModel: ProgressViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]

        addressData = loadAddressData(requireContext())

        // Observe latest data
        viewModel.updateStateList.observe(viewLifecycleOwner) { stateList ->
            stateListCache = stateList
        }

        viewModel.updateCityList.observe(viewLifecycleOwner) { cityList ->
            cityListCache = cityList
        }

        // Restore previous values from ViewModel
        binding.etCountry.setText(viewModel.selectedCountryName.value ?: "")
        binding.etState.setText(viewModel.selectedStateName.value ?: "")
        binding.etCity.setText(viewModel.selectedCityName.value ?: "")
        binding.etPinCode.setText(viewModel.pinCode.value ?: "")
        binding.etStreet.setText(viewModel.streetAddress.value ?: "")

        viewModel.selectedCountryId.value?.let {
            viewModel.getStateMasterByCountryId(it )
        }
        viewModel.selectedStateId.value?.let {
            viewModel.getCityMasterByStateId(it )
        }

        // COUNTRY SELECTION
        binding.etCountry.setOnClickListener {
            showListBottomSheet(
                context = requireContext(),
                title = "Select Country",
                list = addressData,
                displayKey = "countryName"
            ) { selectedMap ->
                val countryName = selectedMap["countryName"]?.toString()
                val countryId = selectedMap["id"]?.toString()

                selectedCountryId = countryId
                binding.etCountry.setText(countryName)
                binding.etState.setText("")
                binding.etCity.setText("")
                selectedState = null
                stateListCache = emptyList()
                cityListCache = emptyList()

                viewModel.selectedCountryId.value = countryId
                viewModel.selectedCountryName.value = countryName

                countryId?.let { viewModel.getStateMasterByCountryId(it) }
            }
        }

        // STATE SELECTION
        binding.etState.setOnClickListener {
            if (selectedCountryId == null && viewModel.selectedCountryId.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select a country first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (stateListCache.isEmpty()) {
                val countryIdToFetch = selectedCountryId ?: viewModel.selectedCountryId.value!!
                viewModel.getStateMasterByCountryId(countryIdToFetch)
                Toast.makeText(requireContext(), "Loading states...", Toast.LENGTH_SHORT).show()
            } else {
                val stateMappedList = stateListCache.map {
                    mapOf<String, Any>("id" to it.id, "name" to it.stateName)
                }

                showListBottomSheet(
                    context = requireContext(),
                    title = "Select State",
                    list = stateMappedList,
                    displayKey = "name"
                ) { selectedMap ->
                    val stateName = selectedMap["name"]?.toString()
                    val stateId = selectedMap["id"]?.toString()

                    binding.etState.setText(stateName)
                    binding.etCity.setText("")
                    selectedState = stateListCache.find { it.id.toString() == stateId }

                    cityListCache = emptyList()

                    viewModel.selectedStateId.value = stateId
                    viewModel.selectedStateName.value = stateName

                    stateId?.let { viewModel.getCityMasterByStateId(it) }
                }
            }
        }

        // CITY SELECTION
        binding.etCity.setOnClickListener {
            if (selectedState == null && viewModel.selectedStateId.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select a state first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cityListCache.isEmpty()) {
                val stateIdToFetch = selectedState?.id?.toString() ?: viewModel.selectedStateId.value!!
                viewModel.getCityMasterByStateId(stateIdToFetch)
                Toast.makeText(requireContext(), "Loading cities...", Toast.LENGTH_SHORT).show()
            } else {
                val cityMappedList: List<Map<String, Any>> = cityListCache.map {
                    mapOf<String, Any>(
                        "id" to it.id,
                        "name" to it.name
                    )
                }

                showListBottomSheet(
                    context = requireContext(),
                    title = "Select City",
                    list = cityMappedList,
                    displayKey = "name"
                ) { selectedMap ->
                    val cityName = selectedMap["name"]?.toString()
                    val cityId = selectedMap["id"]?.toString()
                    binding.etCity.setText(cityName)

                    viewModel.selectedCityId.value = cityId
                    viewModel.selectedCityName.value = cityName
                }
            }
        }

        // NEXT BUTTON
        binding.btnNext.setOnClickListener {

//            if (binding.etCountry.text.isNullOrEmpty() ||
//                binding.etState.text.isNullOrEmpty() ||
//                binding.etCity.text.isNullOrEmpty() ||
//                binding.etStreet.text.isNullOrEmpty()
//            ) {
//                Toast.makeText(requireContext(), "Please complete address fields", Toast.LENGTH_SHORT).show()
//            } else {
                progressViewModel.updateProgress(5)
                progressViewModel.updatepageNo(5)
                viewModel.pinCode.value = binding.etPinCode.text.toString()
                viewModel.streetAddress.value = binding.etStreet.text.toString()
                findNavController().navigate(R.id.action_adressFragment_to_weightFragment)
//            }
        }
    }

    private fun loadAddressData(context: Context): List<Map<String, Any>> {
        val json = context.assets.open("country_json.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return Gson().fromJson(json, type)
    }
}