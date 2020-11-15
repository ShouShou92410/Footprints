package com.cpsc571.footprints

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cpsc571.footprints.firebase.FirebaseFootprints
import com.cpsc571.footprints.firebase.FirebaseFootprintsSource

class LocationHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_history)
    }

    private fun getLocations() {
        var firebaseFootprints: FirebaseFootprints = FirebaseFootprintsSource()
        firebaseFootprints.get()
    }
}