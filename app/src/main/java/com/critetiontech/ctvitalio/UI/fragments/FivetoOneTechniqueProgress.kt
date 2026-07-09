package com.critetiontech.ctvitalio.UI.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.EndSessionBottomSheet
import com.critetiontech.ctvitalio.databinding.FragmentFivetoOneTechniqueProgressBinding
import com.critetiontech.ctvitalio.viewmodel.FiveToOneViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson

class FivetoOneTechniqueProgress : Fragment() {

    private var _binding: FragmentFivetoOneTechniqueProgressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FiveToOneViewModel by viewModels()

    private var currentStep = 0
    private var selectedCount = 0
    private val selectedItemsPerStep = mutableMapOf<Int, List<String>>()
    private var startTimeMillis: Long = 0

    data class StepData(
        val title: String,
        val requiredCount: Int,
        val imageRes: Int,
        val options: List<String>
    )

    private data class StepTemplate(
        val title: String,
        val requiredCount: Int,
        val imageRes: Int,
        val module: String,
        val fallbackOptions: List<String>
    )

    private val stepTemplates = listOf(
        StepTemplate(
            title = "Find 5 things you can SEE around you",
            requiredCount = 5,
            imageRes = R.drawable.ic_eye_step,
            module = "SEE",
            fallbackOptions = listOf("Bottle", "Notebook", "Sticky notes", "Curtains", "Earphones", "Window", "Plant")
        ),
        StepTemplate(
            title = "Find 4 things you can TOUCH",
            requiredCount = 4,
            imageRes = R.drawable.ic_hand_step,
            module = "TOUCH",
            fallbackOptions = listOf("Linen", "Velvet Fabric", "Canvas", "Fleece", "Waffle", "Tweed", "Flannel")
        ),
        StepTemplate(
            title = "Find 3 things you can HEAR",
            requiredCount = 3,
            imageRes = R.drawable.ic_ear_step,
            module = "HEAR",
            fallbackOptions = listOf("Fan noise", "Keyboard typing", "Music", "Footsteps", "Birds Chirping", "People talking", "Clock ticking")
        ),
        StepTemplate(
            title = "Find 2 things you can SMELL",
            requiredCount = 2,
            imageRes = R.drawable.ic_nose_step,
            module = "SMELL",
            fallbackOptions = listOf("Perfume", "Room Fragrance", "Hand sanitizer", "Paper", "Tea", "Marker")
        ),
        StepTemplate(
            title = "Find 1 thing you can TASTE",
            requiredCount = 1,
            imageRes = R.drawable.ic_tongue_step,
            module = "TASTE",
            fallbackOptions = listOf("Candy", "Juice", "Snack", "Coffee", "Fruit", "Mint", "Food")
        )
    )

    private var steps = listOf<StepData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFivetoOneTechniqueProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimeMillis = System.currentTimeMillis()

        // Start immediately with fallback data
        steps = buildSteps(emptyMap())
        loadStep()

        // Refresh chips with API data if user hasn't advanced yet
        viewModel.itemsByModule.observe(viewLifecycleOwner) { moduleMap ->
            steps = buildSteps(moduleMap)
            if (currentStep == 0) loadStep()
        }

        binding.imgClose.setOnClickListener {
            findNavController().navigateUp()
        }
        // Listen for bottom sheet results
        parentFragmentManager.setFragmentResultListener(
            EndSessionBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val action = bundle.getString(EndSessionBottomSheet.EXTRA_ACTION)
            if (action == "success") {
                showCompletionDialog()
            }
        }

        binding.btnReset.setOnClickListener {
            currentStep = 0
            selectedItemsPerStep.clear()
            loadStep()
        }

        binding.btnNext.setOnClickListener {
            captureCurrentSelections()
            advanceOrFinish()
        }
    }

    private fun buildSteps(moduleMap: Map<String, List<String>>): List<StepData> {
        return stepTemplates.map { template ->
            StepData(
                title = template.title,
                requiredCount = template.requiredCount,
                imageRes = template.imageRes,
                options = moduleMap[template.module]?.takeIf { it.isNotEmpty() }
                    ?: template.fallbackOptions
            )
        }
    }

    private fun captureCurrentSelections() {
        val selected = binding.chipGroup.children
            .filterIsInstance<Chip>()
            .filter { it.text.toString() != "+ Custom" && it.isChecked }
            .map { it.text.toString() }
            .toList()
        selectedItemsPerStep[currentStep] = selected
    }

    private fun advanceOrFinish() {
        if (currentStep < steps.lastIndex) {
            currentStep++
            loadStep()
        } else {
            openEndSessionBottomSheet()
        }
    }

    private fun openEndSessionBottomSheet() {
        val durationSeconds = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
        val exerciseId = arguments?.getString("exerciseId")?.toIntOrNull() ?: 0
        val mindfulnessData = stepTemplates.mapIndexed { index, template ->
            mapOf(
                "stepNo" to (index + 1),
                "title" to template.module,
                "items" to (selectedItemsPerStep[index] ?: emptyList<String>())
            )
        }
        val completedSteps = selectedItemsPerStep.values.count { it.isNotEmpty() }
        val mindfulnessJson = Gson().toJson(mindfulnessData)

        val bottomSheet = EndSessionBottomSheet.newInstance(
            exerciseId = exerciseId,
            duration = durationSeconds,
            totalSteps = completedSteps,
            mindfulnessJson = mindfulnessJson,
            title = "Session Completed! 🎉",
            description = "Nice work! You've completed your grounding session. Save your progress now."
        )
        bottomSheet.show(parentFragmentManager, EndSessionBottomSheet.TAG)
    }

    private fun loadStep() {

        val step = steps[currentStep]
        selectedCount = 0

        binding.txtQuestion.text = step.title
        binding.imgExercise.setImageResource(step.imageRes)
        binding.txtStep.text = "Step ${currentStep + 1} of ${steps.size}"
        binding.progressBar.progress = ((currentStep + 1) * 20)

        binding.chipGroup.removeAllViews()

        step.options.forEach { option ->
            val chip = Chip(requireContext()).apply {
                layoutParams = ChipGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = option
                isCheckable = true
                chipCornerRadius = 20f
                chipBackgroundColor = android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(
                        android.graphics.Color.parseColor("#1677FF"),
                        android.graphics.Color.WHITE
                    )
                )
                setTextColor(
                    android.content.res.ColorStateList(
                        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                        intArrayOf(
                            android.graphics.Color.WHITE,
                            android.graphics.Color.parseColor("#344054")
                        )
                    )
                )
                chipStrokeWidth = 1f
                chipStrokeColor = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#D0D5DD")
                )
                setOnCheckedChangeListener { _, _ ->
                    updateSelectionCount(step.requiredCount)
                }
            }
            binding.chipGroup.addView(chip)
        }

        // Custom chip
        val customChip = Chip(requireContext()).apply {
            layoutParams = ChipGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = "+ Custom"
            isCheckable = false
            chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#E0EEFF")
            )
            setTextColor(android.graphics.Color.parseColor("#1677FF"))
            chipStrokeWidth = 0f
            chipStrokeColor = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#1677FF")
            )
            setOnClickListener { showCustomInputDialog(step.requiredCount) }
        }
        binding.chipGroup.addView(customChip)

        binding.btnNext.isEnabled = true
        binding.btnNext.text = "Skip"
    }

    private fun updateSelectionCount(requiredCount: Int) {
        selectedCount = binding.chipGroup.children.count {
            it is Chip && it.text.toString() != "+ Custom" && it.isChecked
        }

        if (selectedCount >= requiredCount) {
            binding.root.postDelayed({
                if (!isAdded) return@postDelayed
                captureCurrentSelections()
                advanceOrFinish()
            }, 500)
        }
    }

    private fun showCustomInputDialog(requiredCount: Int) {
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_custom_option, null)
        bottomSheet.setContentView(view)

        val etCustom = view.findViewById<EditText>(R.id.etCustom)
        val btnAdd = view.findViewById<MaterialButton>(R.id.btnAdd)

        btnAdd.setOnClickListener {
            val text = etCustom.text.toString().trim()
            if (text.isNotEmpty()) {
                addCustomChip(text, requiredCount)
                bottomSheet.dismiss()
            }
        }
        bottomSheet.show()
    }

    private fun addCustomChip(text: String, requiredCount: Int) {
        val chip = Chip(requireContext()).apply {
            layoutParams = ChipGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            this.text = text
            isCheckable = true
            chipCornerRadius = 20f
            chipBackgroundColor = android.content.res.ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(
                    android.graphics.Color.parseColor("#1677FF"),
                    android.graphics.Color.parseColor("#F2F4F7")
                )
            )
            setTextColor(
                android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(
                        android.graphics.Color.WHITE,
                        android.graphics.Color.parseColor("#475467")
                    )
                )
            )
            chipStrokeWidth = 0f
            setOnCheckedChangeListener { _, _ ->
                updateSelectionCount(requiredCount)
            }
        }
        // Insert before the "+ Custom" chip
        binding.chipGroup.addView(chip, binding.chipGroup.childCount - 1)
    }

    private fun showCompletionDialog() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_mindfulness_completed, null)
        bottomSheet.setContentView(view)

        view.findViewById<MaterialButton>(R.id.btnOk).setOnClickListener {
            bottomSheet.dismiss()
            currentStep = 0
            selectedItemsPerStep.clear()
            loadStep()
        }

        bottomSheet.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}