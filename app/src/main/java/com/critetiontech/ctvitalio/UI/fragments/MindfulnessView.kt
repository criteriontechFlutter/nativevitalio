package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentMindfulnessViewBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MindfulnessView.newInstance] factory method to
 * create an instance of this fragment.
 */
class MindfulnessView : Fragment() {
    private var _binding: FragmentMindfulnessViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMindfulnessViewBinding.inflate(
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

        // Click Listeners



        binding.groundingTechniqueId.setOnClickListener {

            findNavController().navigate(R.id.action_mindfulnessView_to_groundingTecniqueView  )
        }

        binding.scavengerHuntId.setOnClickListener {

            findNavController().navigate(R.id.action_mindfulnessView_to_scavengerHunt )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}