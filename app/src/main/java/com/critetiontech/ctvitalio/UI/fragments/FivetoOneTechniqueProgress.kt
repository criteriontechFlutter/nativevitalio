package com.critetiontech.ctvitalio.UI.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentFivetoOneTechniqueProgressBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class FivetoOneTechniqueProgress : Fragment() {

    private var _binding: FragmentFivetoOneTechniqueProgressBinding? = null
    private val binding get() = _binding!!

    private var currentStep = 0
    private var selectedCount = 0

    data class StepData(
        val title: String,
        val requiredCount: Int,
        val imageRes: Int,
        val options: List<String>
    )

    private val steps = listOf(

        StepData(
            title = "Find 5 things you can SEE around you",
            requiredCount = 5,
            imageRes = R.drawable.ic_eye_step,
            options = listOf(
                "Bottle",
                "Notebook",
                "Sticky notes",
                "Curtains",
                "Earphones",
                "Window",
                "Plant"
            )
        ),

        StepData(
            title = "Find 4 things you can TOUCH",
            requiredCount = 4,
            imageRes = R.drawable.ic_hand_step,
            options = listOf(
                "Linen",
                "Velvet Fabric",
                "Canvas",
                "Fleece",
                "Waffle",
                "Tweed",
                "Flannel"
            )
        ),

        StepData(
            title = "Find 3 things you can HEAR",
            requiredCount = 3,
            imageRes = R.drawable.ic_ear_step,
            options = listOf(
                "Fan noise",
                "Keyboard typing",
                "Music",
                "Footsteps",
                "Birds Chirping",
                "People talking",
                "Clock ticking"
            )
        ),

        StepData(
            title = "Find 2 things you can SMELL",
            requiredCount = 2,
            imageRes = R.drawable.ic_nose_step,
            options = listOf(
                "Perfume",
                "Room Fragrance",
                "Hand sanitizer",
                "Paper",
                "Tea",
                "Marker"
            )
        ),

        StepData(
            title = "Find 1 thing you can TASTE",
            requiredCount = 1,
            imageRes = R.drawable.ic_tongue_step,
            options = listOf(
                "Candy",
                "Juice",
                "Snack",
                "Coffee",
                "Fruit",
                "Mint",
                "Food"
            )
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFivetoOneTechniqueProgressBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        loadStep()

        binding.btnReset.setOnClickListener {
            currentStep = 0
            loadStep()
        }

        binding.btnNext.setOnClickListener {

            // Skip Current Step

            if (currentStep < steps.lastIndex) {

                currentStep++
                loadStep()

            } else {

                showCompletionDialog()
            }
        }
    }

    private fun loadStep() {

        val step = steps[currentStep]

        selectedCount = 0

        binding.txtQuestion.text = step.title

        binding.imgExercise.setImageResource(
            step.imageRes
        )

        binding.txtStep.text =
            "Step ${currentStep + 1} of ${steps.size}"

        binding.progressBar.progress =
            ((currentStep + 1) * 20)

        binding.chipGroup.removeAllViews()

        // Normal Options

        step.options.forEach { option ->

            val chip = Chip(requireContext()).apply {

                layoutParams =
                    ChipGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                text = option
                isCheckable = true
                chipCornerRadius = 20f

                chipBackgroundColor =
                    android.content.res.ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf()
                        ),
                        intArrayOf(
                            android.graphics.Color.parseColor("#1677FF"),
                            android.graphics.Color.WHITE
                        )
                    )

                setTextColor(
                    android.content.res.ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf()
                        ),
                        intArrayOf(
                            android.graphics.Color.WHITE,
                            android.graphics.Color.parseColor("#344054")
                        )
                    )
                )

                chipStrokeWidth = 1f
                chipStrokeColor =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#D0D5DD")
                    )

                setOnCheckedChangeListener { _, _ ->
                    updateSelectionCount(step.requiredCount)
                }
            }

            binding.chipGroup.addView(chip)
        }

        // Custom Chip

        val customChip = Chip(requireContext()).apply {

            layoutParams =
                ChipGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

            text = "+ Custom"
            isCheckable = false

            chipBackgroundColor =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#E0EEFF")
                )

            setTextColor(
                android.graphics.Color.parseColor("#1677FF")
            )

            chipStrokeWidth = 0f

            chipStrokeColor =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#1677FF")
                )
            setOnClickListener {
                showCustomInputDialog(step.requiredCount)
            }
        }
        binding.chipGroup.addView(customChip)

        binding.btnNext.isEnabled = true
        binding.btnNext.text = "Skip"
    }

    private fun showCustomInputDialog(
        requiredCount: Int
    ) {

        val bottomSheet =
            BottomSheetDialog(requireContext())

        val view = layoutInflater.inflate(
            R.layout.bottomsheet_custom_option,
            null
        )

        bottomSheet.setContentView(view)

        val etCustom =
            view.findViewById<EditText>(
                R.id.etCustom
            )

        val btnAdd =
            view.findViewById<MaterialButton>(
                R.id.btnAdd
            )

        btnAdd.setOnClickListener {

            val text =
                etCustom.text.toString().trim()

            if (text.isNotEmpty()) {

                addCustomChip(
                    text,
                    requiredCount
                )

                bottomSheet.dismiss()
            }
        }

        bottomSheet.show()
    }

    private fun addCustomChip(
        text: String,
        requiredCount: Int
    ) {

        val chip = Chip(requireContext())

        chip.layoutParams =
            ChipGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        chip.text = text
        chip.isCheckable = true
        chip.chipCornerRadius = 20f

        chip.chipBackgroundColor =
            android.content.res.ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                ),
                intArrayOf(
                    android.graphics.Color.parseColor("#1677FF"),
                    android.graphics.Color.parseColor("#F2F4F7")
                )
            )

        chip.setTextColor(
            android.content.res.ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                ),
                intArrayOf(
                    android.graphics.Color.WHITE,
                    android.graphics.Color.parseColor("#475467")
                )
            )
        )

        chip.chipStrokeWidth = 0f
        chip.setOnCheckedChangeListener { _, _ ->

            updateSelectionCount(
                requiredCount
            )
        }

        val customIndex =
            binding.chipGroup.childCount - 1

        binding.chipGroup.addView(
            chip,
            customIndex
        )
    }

    private fun updateSelectionCount(
        requiredCount: Int
    ) {

        selectedCount =
            binding.chipGroup.children.count {

                it is Chip &&
                        it.text != "+ Custom" &&
                        it.isChecked
            }

        // Auto Next

        if (selectedCount >= requiredCount) {

            binding.root.postDelayed({

                if (!isAdded) return@postDelayed

                if (currentStep < steps.lastIndex) {

                    currentStep++
                    loadStep()

                } else {

                    showCompletionDialog()
                }

            }, 500)
        }
    }

    private fun showCompletionDialog() {

        val bottomSheet =
            BottomSheetDialog(requireContext())

        val view = layoutInflater.inflate(
            R.layout.bottomsheet_mindfulness_completed,
            null
        )

        bottomSheet.setContentView(view)

        val btnOk =
            view.findViewById<MaterialButton>(
                R.id.btnOk
            )

        btnOk.setOnClickListener {

            bottomSheet.dismiss()

            currentStep = 0
            loadStep()
        }

        bottomSheet.show()
    }
    private fun showMindfulnessCompletedBottomSheet() {

        val bottomSheet = BottomSheetDialog(requireContext())

        val view = layoutInflater.inflate(
            R.layout.bottomsheet_mindfulness_completed,
            null
        )

        bottomSheet.setContentView(view)

        val btnOk =
            view.findViewById<MaterialButton>(R.id.btnOk)

        btnOk.setOnClickListener {

            bottomSheet.dismiss()

            currentStep = 0
            loadStep()
        }

        bottomSheet.show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}