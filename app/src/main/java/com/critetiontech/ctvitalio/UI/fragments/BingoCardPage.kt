package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.BingoAdapter
import com.critetiontech.ctvitalio.databinding.FragmentBingoCardPageBinding
import com.critetiontech.ctvitalio.viewmodel.BingoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BingoCardPage : Fragment() {

    private var _binding: FragmentBingoCardPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BingoViewModel
    private lateinit var adapter: BingoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBingoCardPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BingoViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = BingoAdapter(emptyList()) { position ->
            viewModel.toggleTask(position)
        }

        binding.rvBingo.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            this.adapter = this@BingoCardPage.adapter
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            adapter.updateTasks(tasks)
            val completedCount = tasks.count { it.completed }
            binding.tvCompleted.text = "⭐ $completedCount / ${tasks.size}"
        }

        viewModel.bingoTrigger.observe(viewLifecycleOwner) { triggerBingo ->
            if (triggerBingo == true) {
                showBingoDialog()
                viewModel.resetBingoTrigger()
            }
        }
    }

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvTitle.setOnLongClickListener {
            viewModel.resetAllTasks()
            true
        }
    }

    private fun showBingoDialog() {
        binding.confettiView.visibility = View.VISIBLE
        binding.confettiView.startConfetti()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🎉 BINGO!")
            .setMessage("Congratulations! You completed your mindfulness challenge!")
            .setPositiveButton("Awesome") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                binding.confettiView.visibility = View.GONE
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}