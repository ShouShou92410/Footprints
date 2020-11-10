package com.cpsc571.footprints

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_tracker.*
import java.io.IOException
import java.util.*


class TrackerActivity : AppCompatActivity(), LocationListener {
    companion object {
        private const val RC_LOCATION = 421
        private const val MIN_INTERVAL = 3000L
        private const val MIN_DISTANCE = 0f
    }

    private lateinit var locationManager: LocationManager
    private lateinit var geocoder: Geocoder

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
        handleLocationUIUpdate(location)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_LOCATION) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                location_tv.text = "Tracker feature requires location permission to work."
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

    private fun checkPermission() {
        // If no permission, request them from the user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), RC_LOCATION)
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
                val address = addressList[0]
                var localAddress = ""
                for (i in 0..address.maxAddressLineIndex) {
                    localAddress += address.getAddressLine(i) + ", "
                }

                location_tv.text = location_tv.text.toString() +
                    "Latitude: " + location.latitude + System.getProperty ("line.separator") +
                    "Longitude: " + location.longitude + System.getProperty ("line.separator") +
                    "Address: " + localAddress + System.getProperty ("line.separator")
            }
        }
        catch (e: IOException) {
            Log.w("TrackerActivity", "Network unavailable")
        }
    }
}