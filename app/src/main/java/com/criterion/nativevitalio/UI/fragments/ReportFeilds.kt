package com.criterion.nativevitalio.UI.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentReportFeildsBinding
import com.criterion.nativevitalio.viewmodel.ReportFeildsViewModel
import kotlinx.coroutines.launch

class ReportFieldsFragment : Fragment() {
    private lateinit var binding: FragmentReportFeildsBinding
    private lateinit var viewModel: ReportFeildsViewModel

    private val reportInputList = mutableListOf<Pair<Map<String, Any>, EditText>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportFeildsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ReportFeildsViewModel::class.java]

        val testType = arguments?.getString("testType") ?: "N/A"
        val testName = arguments?.getString("testName") ?: "N/A"
        val imagePath = arguments?.getString("imagePath")
        val dateTime = arguments?.getString("dateTime") ?: ""

        val containerLayout = binding.containerDynamicFields
        val parsedData = arguments?.getSerializable("parsedData") as? List<Map<String, Any>> ?: emptyList()

        parsedData.forEachIndexed { index, report ->
            val reportContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 24, 0, 16)
                }
            }

            val heading = TextView(requireContext()).apply {
                text = "Report #${index + 1}"
                textSize = 18f
                setPadding(0, 0, 0, 12)
            }
            reportContainer.addView(heading)

            report.forEach { (key, value) ->
                if (key == "report" && value is List<*>) {
                    value.filterIsInstance<Map<String, Any>>().forEach { testItem ->
                        val itemName = testItem["test_name"]?.toString() ?: "-"
                        val result = testItem["result"]?.toString() ?: "-"
                        val unit = testItem["unit"]?.toString() ?: ""
                        val normal = testItem["normal_values"]?.toString() ?: ""

                        val label = TextView(requireContext()).apply {
                            text = "$itemName ($unit)"
                            textSize = 16f
                            setPadding(0, 12, 0, 4)
                        }

                        val resultField = EditText(requireContext()).apply {
                            setText(result)
                            hint = "Enter $itemName result"
                            textSize = 15f
                            setPadding(16, 12, 16, 12)
                            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edittext_border)
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }

                        reportInputList.add(testItem to resultField)

                        val normalText = TextView(requireContext()).apply {
                            text = "Normal Range: $normal"
                            textSize = 13f
                            setPadding(0, 4, 0, 4)
                        }

                        reportContainer.addView(label)
                        reportContainer.addView(resultField)
                        reportContainer.addView(normalText)
                    }
                } else {
                    val label = TextView(requireContext()).apply {
                        text = "$key:"
                        textSize = 16f
                        setPadding(0, 8, 0, 4)
                    }

                    val editText = EditText(requireContext()).apply {
                        setText(value.toString())
                        hint = "Enter $key"
                        textSize = 16f
                        setPadding(16, 12, 16, 12)
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edittext_border)
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    reportContainer.addView(label)
                    reportContainer.addView(editText)
                }
            }

            containerLayout.addView(reportContainer)
        }
            binding.backButton.setOnClickListener(){

                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

        binding.btnUploadFinal.setOnClickListener {
            if (!imagePath.isNullOrEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    context?.let { safeContext ->
                        val responseUrl = viewModel.insertPatientMediaData(
                            context = safeContext,
                            testName = testName,
                            category = testType,
                            remark = "",
                            dateTime = dateTime,
                            imagePath = imagePath
                        )

                        if (!responseUrl.isNullOrEmpty()) {
                            Toast.makeText(safeContext, "Image Upload Successful!", Toast.LENGTH_SHORT).show()
// ✅ Safely update reports with modified 'result' values
                            val updatedParsedData: List<Map<String, Any>> = parsedData.map { report ->
                                val updatedReportList: List<Map<String, Any>> = (report["report"] as? List<Map<String, Any>>)?.map { item ->
                                    val matchedInput = reportInputList.find { it.first["test_name"] == item["test_name"] }?.second
                                    val updatedValue = matchedInput?.text?.toString()

                                    item.toMutableMap().apply {
                                        this["result"] = updatedValue ?: ""
                                    }
                                } ?: emptyList()

                                report.toMutableMap().apply {
                                    this["report"] = updatedReportList
                                }
                            }

// ✅ Extract proper patient_details
                            val firstReport = parsedData.firstOrNull() ?: emptyMap()
                            Log.e("InsertInvestigationdatafirstReport", firstReport.toString())
                            val patientDetailsRaw = firstReport

                            val patientDetails = mapOf(
                                "patient_name" to (patientDetailsRaw["patient_name"] ?: ""),
                                "sex" to (patientDetailsRaw["sex"] ?: ""),
                                "age" to (patientDetailsRaw["age"] ?: ""),
                                "lab_name" to (patientDetailsRaw["lab_name"] ?: ""),
                                "collection_date" to (patientDetailsRaw["collection_date"] ?: ""),
                                "reported_date" to (patientDetailsRaw["reported_date"] ?: ""),
                                "summary" to (firstReport["summary"] ?: ""),
                                "status" to (firstReport["status"] ?: ""),
                                "recommended_specialty" to (firstReport["recommended_specialty"] ?: "")
                            )

                            Log.e("InsertInvestigationdata", patientDetails.toString())
// ✅ Now call insertInvestigation
                            val investigationSuccess = viewModel.insertInvestigation(
                                context = safeContext,
                                dateTime = "$dateTime 00:00",
                                reportData = updatedParsedData,
                                patientDetails = patientDetails
                            )

                            requireActivity().onBackPressedDispatcher.onBackPressed()
                            if (investigationSuccess) {
                                Toast.makeText(safeContext, "Investigation uploaded successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(safeContext, "Investigation upload failed", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(safeContext, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Log.e("ReportFieldsFragment", "Context is null or fragment not attached")
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Image not selected", Toast.LENGTH_SHORT).show()
            }
        }



    }
}
