package com.cpsc571.footprints

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class LocationSelectedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_selected)

        val longitude = intent.getStringExtra("locationLongitude")
        val latitude = intent.getStringExtra("locationLatitude")
        val address = intent.getStringExtra("locationAddress")
        val locationName = intent.getStringExtra("locationName")

        val locationNameTextView: TextView = findViewById<TextView>(R.id.displayLocationName).apply{
            text = locationName
        }
        val locationAddressTextView: TextView = findViewById<TextView>(R.id.displayLocationAddress).apply{
            text = address
        }
        val locationLongitudeTextView: TextView = findViewById<TextView>(R.id.displayLocationLongitude).apply{
            text = longitude
        }
        val locationLatitudeTextView: TextView = findViewById<TextView>(R.id.displayLocationLatitude).apply{
            text = longitude
        }


    }
}