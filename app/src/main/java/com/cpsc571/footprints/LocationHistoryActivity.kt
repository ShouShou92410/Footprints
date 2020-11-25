package com.cpsc571.footprints

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cpsc571.footprints.entity.LocationObject
import com.cpsc571.footprints.entity.LocationTupleObject
import com.cpsc571.footprints.firebase.FirebaseFootprints
import com.cpsc571.footprints.firebase.FirebaseFootprintsSource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.location_history_locations_iterable.view.*

class LocationHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_history)

        val adapter = setupLocationsList()
        getAndDisplayLocations(adapter)
    }

    private fun getAndDisplayLocations(adapter: RecyclerView.Adapter<CustomAdapter.ViewHolder>) {
        val firebaseFootprints: FirebaseFootprints = FirebaseFootprintsSource()
        val currentUser = Firebase.auth.currentUser
        val onChange: (DataSnapshot) -> Unit = {
            locations: DataSnapshot ->
                if (locations != null && !locations.exists()) {
                    // TODO Show empty locations
                } else {
                    val data = mutableListOf<LocationTupleObject>()
                    locations?.children?.forEach(fun(locationSnapshot: DataSnapshot) {
                        data.add(LocationTupleObject(LocationObject(locationSnapshot),locationSnapshot.key))
                    })
                    val adapter = CustomAdapter(data.toTypedArray())
                    setupLocationsList(adapter)
                }
        }
        firebaseFootprints.get("Locations/${currentUser?.uid.toString()}", onChange)
    }

    private fun setupLocationsList(adapter: CustomAdapter = CustomAdapter(arrayOf())): RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        val locationList = findViewById<RecyclerView>(R.id.locationList)
        locationList.adapter = adapter
        locationList.layoutManager = LinearLayoutManager(this)
        return adapter
    }
    private class CustomAdapter(private val dataSet: Array<LocationTupleObject>) :
            RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        /*
        This is where you define what the list elements are
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val locationName: TextView = view.findViewById(R.id.locationName)
            val locationAddress: TextView = view.findViewById(R.id.locationAddress)
            val rowLayout: FrameLayout = view.findViewById(R.id.locationRowLayout)
            lateinit var locationLongitude: String
            lateinit var locationLatitude: String
            lateinit var locationID: String
            init {
                // Define click listener for the ViewHolder's View.
                view.setOnClickListener{ v: View ->
                    val intent = Intent(v.context, LocationSelectedActivity::class.java)
                    intent.putExtra("locationAddress", locationAddress.text)
                    intent.putExtra("locationName", locationName.text)
                    intent.putExtra("locationLongitude", locationLongitude)
                    intent.putExtra("locationLatitude", locationLatitude)
                    intent.putExtra("locationID",locationID)
                    v.context.startActivity(intent)
                }
            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.location_history_locations_iterable, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.locationName.text = dataSet[position].locationObject.name
            viewHolder.locationAddress.text = dataSet[position].locationObject.address
            viewHolder.locationLatitude = dataSet[position].locationObject.latitude.toString()
            viewHolder.locationLongitude = dataSet[position].locationObject.longitude.toString()
            viewHolder.locationID = dataSet[position].locationID.toString()
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size
    }
}

