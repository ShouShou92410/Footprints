package com.cpsc571.footprints

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cpsc571.footprints.entity.LocationObject
import com.cpsc571.footprints.entity.PurchaseObject
import com.cpsc571.footprints.firebase.FirebaseFootprints
import com.cpsc571.footprints.firebase.FirebaseFootprintsSource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.ktx.Firebase

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
            text = latitude
        }

        //Log.d("LocationSelectedActivty", "longitude: ${longitude}, latitude: $latitude")
    }
    /*
    private fun getAndDisplayPurchases(adapter: RecyclerView.Adapter<LocationSelectedActivity.CustomAdapter.ViewHolder>) {
        val firebaseFootprints: FirebaseFootprints = FirebaseFootprintsSource()
        val currentUser = Firebase.auth.currentUser
        val onChange: (DataSnapshot) -> Unit = {
            value: DataSnapshot ->
            val purchases = value
            if (purchases != null && !purchases.exists()) {
                // TODO Show empty locations
            } else {
                val data = mutableListOf<PurchaseObject>()
                purchases?.children?.forEach(fun(purchaseSnapshot: DataSnapshot) {
                    data.add(PurchaseObject(purchaseSnapshot))
                })
                val adapter = LocationSelectedActivity.CustomAdapter(data.toTypedArray())
                setupPurchasesList(adapter)
            }
        }
        firebaseFootprints.get("Purchases/${currentUser?.uid}/", onChange)
    }

    private fun setupPurchasesList(adapter: LocationSelectedActivity.CustomAdapter = LocationSelectedActivity.CustomAdapter(arrayOf())): RecyclerView.Adapter<LocationSelectedActivity.CustomAdapter.ViewHolder> {
        val purchaseList = findViewById<RecyclerView>(R.id.spendingList)
        purchaseList.adapter = adapter
        purchaseList.layoutManager = LinearLayoutManager(this)
        return adapter
    }
*/

}