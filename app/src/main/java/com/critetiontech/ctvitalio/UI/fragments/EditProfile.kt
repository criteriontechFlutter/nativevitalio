package com.critetiontech.ctvitalio.UI.fragments

import DateUtils.showListBottomSheet
import PrefsManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.BloodGroupAdapter
import com.critetiontech.ctvitalio.databinding.FragmentEditProfileBinding
import com.critetiontech.ctvitalio.model.BloodGroup
import com.critetiontech.ctvitalio.model.CityModel
import com.critetiontech.ctvitalio.model.StateModel
import com.critetiontech.ctvitalio.viewmodel.EditProfileViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfile : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val prefsManager by lazy { PrefsManager() }
    private lateinit var viewModel: EditProfileViewModel


    private var showView: String? = null

    private var latestSuggestions: List<String> = emptyList()
    private var selectedCountryId: String? = null
    private var selectedState: StateModel? = null
    private lateinit var addressData: List<Map<String, Any>>
    private var stateListCache: List<StateModel> = emptyList()
    private var cityListCache: List<CityModel> = emptyList()

    private val bloodGroups = listOf(
        BloodGroup(1, "A+", 1, 1, "2023-05-02T11:22:58"),
        BloodGroup(2, "A-", 1, 1, "2023-05-30T12:52:37"),
        BloodGroup(3, "B+", 1, 1, "2023-05-30T12:52:56"),
        BloodGroup(4, "B-", 1, 1, "2023-05-31T13:59:25"),
        BloodGroup(6, "O+", 1, 1, "2023-09-05T12:23:47"),
        BloodGroup(7, "O-", 1, 1, "2023-09-05T12:23:56"),
        BloodGroup(8, "AB+", 1, 1, "2023-09-05T12:24:06"),
        BloodGroup(9, "AB-", 1, 1, "2023-09-05T12:24:11")
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        showView = arguments?.getString("isProfile")
        if(showView=="1"){
            binding.profileView.visibility= View.VISIBLE
            binding.personalDetails.visibility= View.GONE
            binding.titleId.text="Edit Profile"

        }
        else{
            binding.profileView.visibility= View.GONE
            binding.personalDetails.visibility= View.VISIBLE
            binding.titleId.text="Personal Info"

        }

        // Bind local patient data
        bindPatientData()
        addressData()
        bglist()
        chronicDiease()
        binding.dobField.setOnClickListener {
            showDatePicker()
        }
        val nameFilter = InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("[a-zA-Z ]") // Allow letters and space only
            if (source.isEmpty()) return@InputFilter null // Allow backspace

            val filtered = source.filter { it.toString().matches(regex) }
            if (filtered == source) null else filtered
        }
        binding.firstNameField.filters = arrayOf(nameFilter)
        binding.lastNameField.filters = arrayOf(nameFilter)


        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        // Handle update button click
        binding.updateProfileButton.setOnClickListener {
            val firstName = binding.firstNameField.text.toString().trim()
            val lastName = binding.lastNameField.text.toString().trim()


// 1. Validate names
            if (firstName.isEmpty()) {
                Toast.makeText(requireContext(), "First name is required", Toast.LENGTH_SHORT).show()
                binding.firstNameField.requestFocus()
                return@setOnClickListener
            }




            // 2. Prepare values
            val name = "$firstName $lastName"
            val address = prefsManager.getPatient()?.address.toString()
            val genderId = when (binding.genderGroup.checkedRadioButtonId) {
                R.id.radioMale -> "1"
                R.id.radioFemale -> "2"
                R.id.radioOther -> "3"
                else -> "1"
            }

            val rawDob = binding.dobField.text.toString()
            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val convertedDob = try {
                val parsedDate = inputFormat.parse(rawDob)
                outputFormat.format(parsedDate!!)
            } catch (e: Exception) {
                e.printStackTrace()
                rawDob
            }

            // 3. Call ViewModel if all fields are valid
            viewModel.updateUserData(
                requireContext(),
                filePath = null,
                name = name,
                phone = binding.mobileNo.text.toString(),
                email = binding.email.text.toString(),
                dob = convertedDob,
                genderId = genderId,


               chronicData=  Gson().toJson(viewModel.selectedDiseaseList.value ?: emptyList<Map<String, String>>()),
                street=address,
                zipCode=  binding.email.text.toString(),
                countryId = viewModel.selectedCountryId.value?.toString().orEmpty(),
                stateId = viewModel.selectedStateId.value?.toString().orEmpty(),
                cityId = viewModel.selectedCityId.value?.toString().orEmpty(),
                weight = binding.weight.text.toString(),
                height = binding.height.text.toString(),
                bgId = selectedBloodGroup?.id?.toString().orEmpty()
            )
        }
    }
    private fun loadAddressData(context: Context): List<Map<String, Any>> {
        val json = context.assets.open("country_json.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return Gson().fromJson(json, type)
    }


    private var selectedBloodGroup: BloodGroup? = null


    private fun   chronicDiease(){
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().trim()
                if (input.isNotEmpty()) {
                    viewModel.getProblemList(input.toString())
                    binding.chronicDieases.setDropDownBackgroundResource(android.R.color.white)
                    binding.chronicDieases.showDropDown()
                }
            }
        }

        binding.chronicDieases.addTextChangedListener(textWatcher)

        binding.chronicDieases.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position).toString()
            val selectedProblem = viewModel.problemList.value?.find { it.problemName == selectedName }

            if (selectedProblem != null) {
                viewModel.addSelectedDisease(selectedProblem, requireContext())
                binding.chronicDieases.removeTextChangedListener(textWatcher)
                binding.chronicDieases.setText("", false)
                binding.chronicDieases.addTextChangedListener(textWatcher)
                binding.chronicDieases.dismissDropDown()
            }
        }

        viewModel.problemList.observe(viewLifecycleOwner) { problemList ->
            if (!problemList.isNullOrEmpty()) {
                latestSuggestions = problemList.map { it.problemName }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, latestSuggestions)
                binding.chronicDieases.setAdapter(adapter)
            }
        }

        viewModel.selectedDiseaseList.observe(viewLifecycleOwner) { list ->
            binding.selectedListContainer.removeAllViews()
            list.distinctBy { it["detailID"] }.forEach { disease ->
                addRemovableChip(disease)
            }
        }
    }

    private fun addRemovableChip(disease: Map<String, String>) {
        val inflater = LayoutInflater.from(requireContext())
        val chipView = inflater.inflate(R.layout.chip_removable, binding.selectedListContainer, false)

        val chipText = chipView.findViewById<TextView>(R.id.chipText)
        val chipRemove = chipView.findViewById<ImageView>(R.id.chipRemove)

        chipText.text = disease["details"] ?: ""

        chipRemove.setOnClickListener {
            viewModel.removeSelectedDisease(disease["detailID"].orEmpty())
        }

        binding.selectedListContainer.addView(chipView)
    }
    private fun   bglist(){


        val bloodGroupNames    = bloodGroups.map { it.groupName } // or .groupName

        binding.bloodGroupSpinner.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select Blood Group")
            builder.setItems(bloodGroupNames.toTypedArray()) { _, which ->
                selectedBloodGroup = bloodGroups[which]
                binding.bloodGroupSpinner.setText(selectedBloodGroup!!.groupName)
                Log.d("SelectedBloodGroup", selectedBloodGroup.toString())
            }
            builder.show()
        }


    }

    fun addressData(){
        addressData = loadAddressData(requireContext())

        // Observe latest data
        viewModel.updateStateList.observe(viewLifecycleOwner) { stateList ->
            stateListCache = stateList
        }

        viewModel.updateCityList.observe(viewLifecycleOwner) { cityList ->
            cityListCache = cityList
        }

        // Restore previous values from ViewModel
        binding.countrySpinner.setText(viewModel.selectedCountryName.value ?: "")
        binding.stateSpinner.setText(viewModel.selectedStateName.value ?: "")
        binding.citySpinner.setText(viewModel.selectedCityName.value ?: "")
        binding.zipCode.setText(viewModel.pinCode.value ?: "")
        binding.streetAdd.setText(viewModel.streetAddress.value ?: "")

        viewModel.selectedCountryId.value?.let {
            viewModel.getStateMasterByCountryId(it )
        }
        viewModel.selectedStateId.value?.let {
            viewModel.getCityMasterByStateId(it )
        }

        // COUNTRY SELECTION
        binding.countrySpinner.setOnClickListener {
            showListBottomSheet(
                context = requireContext(),
                title = "Select Country",
                list = addressData,
                displayKey = "countryName"
            ) { selectedMap ->
                val countryName = selectedMap["countryName"]?.toString()
                val countryId = selectedMap["id"]?.toString()

                selectedCountryId = countryId
                binding.countrySpinner.setText(countryName)
                binding.stateSpinner.setText("")
                binding.citySpinner.setText("")
                selectedState = null
                stateListCache = emptyList()
                cityListCache = emptyList()

                viewModel.selectedCountryId.value = countryId
                viewModel.selectedCountryName.value = countryName

                countryId?.let { viewModel.getStateMasterByCountryId(it) }
            }
        }

        // STATE SELECTION
        binding.stateSpinner.setOnClickListener {
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

                    binding.stateSpinner.setText(stateName)
                    binding.citySpinner.setText("")
                    selectedState = stateListCache.find { it.id.toString() == stateId }

                    cityListCache = emptyList()

                    viewModel.selectedStateId.value = stateId
                    viewModel.selectedStateName.value = stateName

                    stateId?.let { viewModel.getCityMasterByStateId(it) }
                }
            }
        }

        // CITY SELECTION
        binding.citySpinner.setOnClickListener {
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
                    binding.citySpinner.setText(cityName)

                    viewModel.selectedCityId.value = cityId
                    viewModel.selectedCityName.value = cityName
                }
            }
        }

    }

    private fun bindPatientData() {
        prefsManager.getPatient()?.let { patient ->
            val nameParts = patient.patientName.split(" ")
            binding.firstNameField.setText(nameParts.getOrNull(0) ?: "")
            binding.lastNameField.setText(nameParts.getOrNull(1) ?: "")

            // Format date before showing
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Example: 20 May 2025

            val formattedDob = try {
                val parsedDate = inputFormat.parse(patient.dob)
                displayFormat.format(parsedDate!!)
            } catch (e: Exception) {
                e.printStackTrace()
                patient.dob // fallback
            }

            binding.dobField.setText(formattedDob)
            binding.mobileNo.setText(patient.mobileNo.toString())
            binding.email.setText(patient.emailID.toString())
            binding.zipCode.setText(patient.zip.toString())
            val weight = patient.weight.toString()
            binding.weight.setText(String.format("%.2f", weight.toDouble()))
            binding.height.setText(patient.height.toString())

            val height = patient.height.toString()
            binding.height.setText(String.format("%.1f", height.toDouble()))
            viewModel.selectedCountryId.value=patient.countryId.toString()

              selectedBloodGroup  = bloodGroups.find { it.id.toString() == patient.bloodGroupId  }


            if (selectedBloodGroup != null) {
                binding.bloodGroupSpinner.setText(selectedBloodGroup!!.groupName.toString(), TextView.BufferType.NORMAL)
            }

            viewModel.getStateMasterByCountryId(patient.countryId.toString())

            // Wait for the state list to be populated, then select state
            viewModel.updateStateList.observe(viewLifecycleOwner, Observer { stateList ->
                val state = stateList.find { it.id.toString() == patient.stateId.toString() }
                if (state != null) {
                    viewModel.selectedStateName.value = state.stateName
                    viewModel.selectedStateId.value = state.id.toString()
                    binding.stateSpinner.setText(state.stateName, TextView.BufferType.NORMAL)
                } else {
                    // Handle case where the state is not found
                    Log.e("Error", "State not found for ID: ${patient.stateId}")
                }
            })

            // Fetch city list by state ID
            viewModel.getCityMasterByStateId(patient.stateId.toString())

            // Wait for the city list to be populated, then select city
            viewModel.updateCityList.observe(viewLifecycleOwner, Observer { cityList ->
                val city = cityList.find { it.id.toString() == patient.cityId.toString() }
                if (city != null) {
                    viewModel.selectedCityName.value = city.name
                    viewModel.selectedCityId.value = city.id.toString()
                    binding.citySpinner.setText(city.name, TextView.BufferType.NORMAL)
                } else {
                    // Handle case where the city is not found
                    Log.e("Error", "City not found for ID: ${patient.cityId}")
                }
            })
            val gender = patient.genderId
            when (gender) {
                "1" -> binding.radioMale.isChecked = true
                "2" -> binding.radioFemale.isChecked = true
                "3" -> binding.radioOther.isChecked = true
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Try to parse the current DOB and pre-select in picker
        val currentDob = binding.dobField.text.toString()
        val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        try {
            val date = displayFormat.parse(currentDob)
            if (date != null) {
                calendar.time = date
            }
        } catch (_: Exception) {}

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            val formattedDate = displayFormat.format(selectedCalendar.time)
            binding.dobField.setText(formattedDate)
        }, year, month, day)

        datePicker.datePicker.maxDate = System.currentTimeMillis() // Optional: prevent future DOB
        datePicker.show()
    }
}