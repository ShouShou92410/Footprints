package com.cpsc571.footprints

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.cpsc571.footprints.entity.LocationObject
import com.cpsc571.footprints.firebase.FirebaseFootprints
import com.cpsc571.footprints.firebase.FirebaseFootprintsSource

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


import kotlinx.android.synthetic.main.activity_tracker.*
import kotlinx.android.synthetic.main.custom_dialog.view.*
import java.io.IOException
import java.util.*


class TrackerActivity : AppCompatActivity(), LocationListener {
    companion object {
        private const val RC_LOCATION = 421
        private const val MIN_INTERVAL = 10000L
        private const val MIN_DISTANCE = 0f
        private const val TRACKED_AREA_RADIUS = 15f
        private const val MAXIMUM_DISTANCE_MOVED_TO_BE_CONSIDERED_STATIONARY = 1f
    }

    private lateinit var locationManager: LocationManager
    private lateinit var geocoder: Geocoder


    private var trackingBool = false
    private var isPromptOpen = false;
    private var trackedArea: Location? = null
    private var previousLocation: Location? = null


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)
        gpsButtonListenerEnable()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geocoder = Geocoder(this, Locale.getDefault())
        checkPermission()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        setUpLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        if(previousLocation == null){
            previousLocation = location
        }
        var isStationary: Boolean = location.distanceTo(previousLocation) < MAXIMUM_DISTANCE_MOVED_TO_BE_CONSIDERED_STATIONARY
        var isOutsideTrackedArea: Boolean =  trackedArea == null || location.distanceTo(trackedArea) > TRACKED_AREA_RADIUS
        if (trackingBool && !isPromptOpen && isStationary && isOutsideTrackedArea) {
            handleLocationUIUpdate(location)
            trackedArea = location
        }
        previousLocation = location
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_LOCATION) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                trackButton.text = "No permission"
                Toast.makeText(this, "Location permission is required.", Toast.LENGTH_LONG).show()
            }
            else {
                trackButton.isClickable = true
            }
        }
    }

    fun gpsButtonListenerEnable(){
        val toggle: ToggleButton = findViewById(R.id.trackButton)
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                val textView = testingTextView.apply{text = "GPS ON"}
                trackingBool = true;
            } else {
                // The toggle is disabled
                val textView = findViewById<TextView>(R.id.testingTextView).apply{text = "GPS OFF"}
                trackingBool = false;
            }
        }
    }

    private fun checkPermission() {
        // If no permission, request them from the user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), RC_LOCATION)
        }
        else{
            trackButton.isClickable = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpLocationUpdates() {
        var provider: String? = locationManager.getBestProvider(Criteria(), true)
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, MIN_INTERVAL, MIN_DISTANCE, this)
        }
    }

    private fun handleLocationUIUpdate(location: Location) {
        try {
            val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addressList.isNotEmpty()) {
                isPromptOpen = true

                var defaultNameString = "Default Name"
                val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog,null)
                val builder = AlertDialog.Builder(this).setView(dialogView).setTitle("Save Prompt")
                builder.setOnCancelListener{
                    isPromptOpen = false;
                }
                builder.setOnDismissListener {
                    isPromptOpen = false;
                }
                val alertDialog = builder.show()
                dialogView.cancelLocationSaveButton.setOnClickListener{
                    alertDialog.dismiss()
                    isPromptOpen = false
                }
                dialogView.saveLocationButton.setOnClickListener{
                    alertDialog.dismiss()
                    val name = dialogView.editTextLocationName.text.toString()
                    defaultNameString = name
                    val address = addressList[0]
                    var localAddress = ""
                    for (i in 0..address.maxAddressLineIndex) {
                        localAddress += address.getAddressLine(i) + ", "
                    }

                    val user = Firebase.auth.currentUser
                    val firebaseDB: FirebaseFootprints = FirebaseFootprintsSource()
                    val jsonAddress = "Locations/${user?.uid}"
                    val jsonData = LocationObject(defaultNameString,localAddress,location.longitude.toString(),location.latitude.toString())
                    firebaseDB.push(jsonAddress, jsonData)
                    isPromptOpen = false

                    Toast.makeText(this, "Location saved.", Toast.LENGTH_LONG).show()
                }
            }
        }
        catch (e: IOException) {
            Log.w("TrackerActivity", "Network unavailable")
        }
    }
}