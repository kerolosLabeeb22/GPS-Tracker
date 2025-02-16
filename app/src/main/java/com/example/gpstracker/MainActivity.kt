package com.example.gpstracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(),OnMapReadyCallback {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // access to info

                getUserLocation()
            } else {
                // explain to user why we need it

                showRationaleDialog()
            }
        }
    private var googleMap:GoogleMap?=null
    private var marker:Marker ?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun checkForPermission(permission: String) {
        when {
            ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED -> {
                // access info
                getUserLocation()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                showRationaleDialog()
            }

            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                //Log.e("TAG", "getUserLocation: ${location.latitude} , ${location.longitude}")

                putUserMarkerOnMap(location.latitude,location.longitude)
            }
        }
    }
    private fun putUserMarkerOnMap(latitude: Double, longitude: Double){
        val latLng = LatLng(latitude, longitude)
        if (marker==null){
            val markerOptions = MarkerOptions()
            markerOptions.title("Your Current Location")
            markerOptions.position(latLng)
            markerOptions.isDraggable
            googleMap?.addMarker(markerOptions)
        }else{
            marker?.position=latLng
        }

        googleMap?.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng,16f))

    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        Toast.makeText(this, "Getting user location", Toast.LENGTH_LONG).show()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000_000)
            .build()
//        fusedLocationProviderClient.getCurrentLocation(currentLocationRequest,null).addOnSuccessListener {location->
//            location.time
//            location.latitude
//            location.longitude
//            Log.e("TAG", "getUserLocation: ${location.latitude} , ${location.longitude}")
//
//
//        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun showRationaleDialog() {
        showDialog(
            title = getString(R.string.why_we_need_this_permission),
            message = getString(R.string.we_need_this_permission_to_find_and_locate_nearest_drivers_for_you),
            positiveButtonText = getString(R.string.ok_i_understand),
            onPositiveClick = {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            negativeButtonText = getString(R.string.no_thanks),
        )
    }

    private fun showDialog(
        title: String? = null,
        message: String,
        positiveButtonText: String? = null,
        onPositiveClick: () -> Unit,
        negativeButtonText: String? = null,
        onNegativeClick: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, which ->
            onPositiveClick.invoke()
            dialog.dismiss()
        }

        if (negativeButtonText != null)
            builder.setNegativeButton(negativeButtonText) { dialog, which ->
                onNegativeClick?.invoke()
                dialog.dismiss()
            }

        builder.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap=googleMap

    }



}