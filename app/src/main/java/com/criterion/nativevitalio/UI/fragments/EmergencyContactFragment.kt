package com.critetiontech.ctvitalio.UI.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.adapter.omronAdapter.EmergencyContactAdapter
import com.critetiontech.ctvitalio.databinding.FragmentEmergencyContactBinding
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.viewmodel.EmergencyContactViewModel


class EmergencyContactFragment : Fragment() {
    private lateinit var binding: FragmentEmergencyContactBinding
    private val viewModel: EmergencyContactViewModel by viewModels()

    private lateinit var adapter: EmergencyContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentEmergencyContactBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }

        binding.emergencyAddButton.setOnClickListener {
            val bottomSheet = AddContactBottomSheet()
            bottomSheet.show(parentFragmentManager, "AddContactBottomSheet")
        }


        setupRecyclerView()
        observeViewModel()

        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun setupRecyclerView() {
        viewModel.emergencyContactList.observe(viewLifecycleOwner) { isLoading ->
            adapter = EmergencyContactAdapter(isLoading) { contact ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.contactNumber}"))
                startActivity(intent)
            }
            binding.scrollView.layoutManager = LinearLayoutManager(requireContext())
            binding.scrollView.adapter = adapter
        }


    }

    private fun observeViewModel() {
        viewModel.emergencyContactList.observe(viewLifecycleOwner) { updatedList ->
            adapter.updateList(updatedList)
        }
    }


}