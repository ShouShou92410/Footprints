package com.cpsc571.footprints

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.cpsc571.footprints.firebase.FirebaseFootprints
import com.cpsc571.footprints.firebase.FirebaseFootprintsSource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.ktx.Firebase

class LocationHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_history)

        getLocations()
    }

    private fun getLocations() {
        var firebaseFootprints: FirebaseFootprints = FirebaseFootprintsSource()
        var currentUser = Firebase.auth.currentUser
        var onChange: (DataSnapshot) -> Unit = {
            value: DataSnapshot ->
                Log.d("a", "a")
                var locations = value.children.find { childSnapshot: DataSnapshot -> childSnapshot.key == "locations" }
                if (locations != null && locations.exists()) {
                    // TODO Show empty locations
                } else {
                    locations?.children?.forEach(fun(locationSnapshot: DataSnapshot) {
                        Log.d("t", "t")
                    })
                }
        }
        firebaseFootprints.get("Users/${currentUser?.uid.toString()}", onChange)

    }
}