 package com.critetiontech.ctvitalio.UI.fragments

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.DailyTipAdapter
import com.critetiontech.ctvitalio.adapter.GymAdapter
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.ctvitalio.databinding.FragmentGymListBinding
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import com.critetiontech.ctvitalio.viewmodel.GymViewModel


 class GymListFragment : Fragment() {
    private lateinit var binding: FragmentGymListBinding
     private lateinit var viewModel: GymViewModel

     private lateinit var adapter: GymAdapter
     private var isTextVisible = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGymListBinding.inflate(inflater, container, false)
        return binding.root
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)


         viewModel = ViewModelProvider(this)[GymViewModel::class.java]

         viewModel.getAllGymMasters()

         viewModel.gymList.observe(requireActivity()) {
             adapter = GymAdapter(it)
             binding.gymListRecyclerview.adapter = adapter
         }

         binding.gymListRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {

             override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                 super.onScrolled(recyclerView, dx, dy)

                 if (dy > 10 && isTextVisible) {
                     // Scroll UP → Hide
                     fadeOut(binding.textView5)
                     fadeOut(binding.textview6)
                     isTextVisible = false
                 } else if (dy < -10 && !isTextVisible) {
                     // Scroll DOWN → Show
                     fadeIn(binding.textView5)
                     fadeIn(binding.textview6)
                     isTextVisible = true
                 }
             }
         })
     }



     private fun fadeOut(view: View) {
         view.animate()
             .alpha(0f)
             .translationY(-20f)
             .setDuration(200)
             .withEndAction { view.visibility = View.GONE }
     }

     private fun fadeIn(view: View) {
         view.visibility = View.VISIBLE
         view.alpha = 0f
         view.translationY = -20f

         view.animate()
             .alpha(1f)
             .translationY(0f)
             .setDuration(200)
     }

}