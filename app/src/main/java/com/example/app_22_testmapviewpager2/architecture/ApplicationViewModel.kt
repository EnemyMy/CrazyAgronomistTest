package com.example.app_22_testmapviewpager2.architecture

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.example.app_22_testmapviewpager2.main.ACCESS_FINE_LOCATION_REQUEST_CODE
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import com.google.android.gms.location.LocationRequest
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult


class ApplicationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : Repository = Repository(application)

    fun getDiscussions() = repository.getDiscussions()

    fun updateDiscussions(lat : Float, lng : Float, radius : Int) = repository.updateDiscussions(lat, lng, radius)

    fun getCurrentLocation(activity: Activity, targetLocation: Location) {

        if (EasyPermissions.hasPermissions(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {

            val mLocationRequest = LocationRequest.create()
            mLocationRequest.interval = 60000
            mLocationRequest.fastestInterval = 5000
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            val client = LocationServices.getFusedLocationProviderClient(activity)

            client.requestLocationUpdates(mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult?) {
                    super.onLocationResult(p0)
                    if (p0 != null) {
                        targetLocation.latitude = p0.lastLocation.latitude
                        targetLocation.longitude = p0.lastLocation.longitude
                        targetLocation.provider = "currentLocationUpdated"
                    }
                }
            }, null)
        }
        else {
            if (EasyPermissions.permissionPermanentlyDenied(activity, Manifest.permission.ACCESS_FINE_LOCATION))
                AppSettingsDialog.Builder(activity).setRequestCode(ACCESS_FINE_LOCATION_REQUEST_CODE).build().show()
            else
                EasyPermissions.requestPermissions(
                    activity, "This permission required for proper functionality of this application",
                    ACCESS_FINE_LOCATION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION
                )
        }
    }

}