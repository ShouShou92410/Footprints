package com.cpsc571.footprints

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_tracker.*


class TrackerActivity : AppCompatActivity() {
    companion object {
        private const val RC_LOCATION = 421
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)
        gpsButtonListenerEnable()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startTracking()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationTest()
            }
            else {
                testingTextView.text = "Tracker feature requires location permission to work."
            }
        }
    }

    fun gpsButtonListenerEnable(){
        val toggle: ToggleButton = findViewById(R.id.toggleButton2)
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                val textView = testingTextView.apply{text = "GPS ON"}
            } else {
                // The toggle is disabled
                val textView = findViewById<TextView>(R.id.testingTextView).apply{text = "GPS OFF"}
            }
        }
    }

    private fun startTracking() {
        // If no permission, request them from the user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            getLocationTest()
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), RC_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationTest() {
        val locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult?.locations) {
                    if (location != null) {
                        handleLocationUIUpdate(location)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private fun handleLocationUIUpdate(location: Location) {
        location_tv.text = location_tv.text.toString() +
            "Latitude: " + location.latitude + System.getProperty ("line.separator") +
            "Longitude: " + location.longitude + System.getProperty ("line.separator")
    }
}