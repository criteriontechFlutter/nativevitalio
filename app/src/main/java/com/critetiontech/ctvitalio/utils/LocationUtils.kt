package com.critetiontech.ctvitalio.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices

object LocationUtils {


    @SuppressLint("MissingPermission")
    fun getDistanceFromCurrentLocation(
        context: Context,
        targetLat: Double,
        targetLng: Double,
        result: (Double) -> Unit
    ) {

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    val startPoint = Location("current").apply {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                    Log.d("TAG", "getDistanceFrom CurrentLocation: $startPoint")

                    val endPoint = Location("target").apply {
                        latitude = targetLat
                        longitude = targetLng
                    }
                    Log.d("TAG", "getDistanceFromCurrentLocation: $endPoint")
                    val distanceMeters = startPoint.distanceTo(endPoint)

                    val distanceKm: Double = (distanceMeters / 1000).toFloat().toDouble()
                    Log.d("TAG", "getDistanceFromCurrentLocation: $distanceKm")
                    result(distanceKm)

                } else {
                    result(0.0)
                }
            }
            .addOnFailureListener {
                result(0.0)
            }
    }
}