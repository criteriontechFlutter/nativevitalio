package com.criterion.nativevitalio.UI.ui.observer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentFluidIntake2Binding
import com.criterion.nativevitalio.databinding.FragmentObserverSharedDashboardBinding

class ObserverSharedDashboardFragment : Fragment() {
    private lateinit var binding: FragmentObserverSharedDashboardBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentObserverSharedDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.menuButton.setOnClickListener {
            val popup = PopupMenu(this.requireContext(), it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.menu_observer, popup.menu)
            popup.show()
        }
    }
}