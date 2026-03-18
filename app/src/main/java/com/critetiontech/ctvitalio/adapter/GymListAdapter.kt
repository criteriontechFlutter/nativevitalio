package com.critetiontech.ctvitalio.adapter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ItemGymListBinding
import com.critetiontech.ctvitalio.model.Gym
import com.critetiontech.ctvitalio.utils.LocationUtils
import com.critetiontech.ctvitalio.utils.MyApplication
import androidx.core.net.toUri

class GymAdapter(
    private val gymList: List<Gym>
) : RecyclerView.Adapter<GymAdapter.GymViewHolder>() {

    inner class GymViewHolder(val binding: ItemGymListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GymViewHolder {

        val binding = ItemGymListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return GymViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GymViewHolder, position: Int) {

        val gym = gymList[position]

        holder.binding.btnCheckin.setOnClickListener {
            val bundle = Bundle().apply {
                putString("gymName", gym.gymName)
                putString("gymAddress", gym.address)
                putDouble("latitude", gym.latitude)
                putDouble("longitude", gym.longitude)
                // Add any other data you want to pass
            }

            val navController = Navigation.findNavController(holder.itemView)
            navController.navigate(R.id.gymCheckInFragment, bundle)
        }

        holder.binding.btnDirection.setOnClickListener {
            openMapWithRoute(gym.latitude,gym.longitude)
        }

        holder.binding.apply {

            tvGymName.text = gym.gymName
           // txtDescription.text = gym.description
            tvAddress.text = "${gym.address}, ${gym.city}"
           // txtContact.text = gym.contactNumber

            Glide.with(root.context)
                .load(gym.imageURL.replace("\\","/"))
                .into(imgGym)



            LocationUtils.getDistanceFromCurrentLocation(
                holder.itemView.context,
                gym.latitude,
                gym.longitude
            ) { distance ->
                Log.d("TAG", "onBindViewHolder: "+distance.toString());
                holder.binding.tvDistance.text =
                    String.format("%.2f km away", distance)

            }
        }
    }

    private fun openMapWithRoute(
        destLatitude: Double,
        destLongitude: Double,
        destinationName: String = "Destination"
    ) {
        try {
            val uri = "google.navigation:q=$destLatitude,$destLongitude&label=$destinationName".toUri()

            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // ✅ Required for non-Activity context
            }

            if (mapIntent.resolveActivity(MyApplication.appContext.packageManager) != null) {
                MyApplication.appContext.startActivity(mapIntent)
            } else {
                // Fallback: open in browser
                val browserUri =
                    "https://www.google.com/maps/dir/?api=1&destination=$destLatitude,$destLongitude".toUri()

                val browserIntent = Intent(Intent.ACTION_VIEW, browserUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // ✅ Required here too
                }
                MyApplication.appContext.startActivity(browserIntent)
            }

        } catch (e: Exception) {
            Log.e("MapRoute", "Failed to open map: ${e.message}")
        }
    }
    override fun getItemCount(): Int {
        return gymList.size
    }
}