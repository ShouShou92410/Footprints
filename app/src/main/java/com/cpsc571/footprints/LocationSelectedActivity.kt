package com.cpsc571.footprints

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cpsc571.footprints.entity.LocationObject
import com.cpsc571.footprints.entity.LocationTupleObject
import com.cpsc571.footprints.entity.PurchaseObject
import com.cpsc571.footprints.firebase.FirebaseFootprints
import com.cpsc571.footprints.firebase.FirebaseFootprintsSource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.ktx.Firebase

class LocationSelectedActivity : AppCompatActivity() {

    private var locationID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_selected)

        val longitude = intent.getStringExtra("locationLongitude")
        val latitude = intent.getStringExtra("locationLatitude")
        val address = intent.getStringExtra("locationAddress")
        val locationName = intent.getStringExtra("locationName")
        locationID = intent.getStringExtra("locationID")
        //Log.d("LocationSelectedActivty", "locationID: $locationID")
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
        val adapter = setupPurchasesList()
        getAndDisplayPurchases(adapter)
        //Log.d("LocationSelectedActivty", "longitude: ${longitude}, latitude: $latitude")

    }

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
        firebaseFootprints.get("Purchases/${currentUser?.uid}/${locationID}", onChange)
    }

    private fun setupPurchasesList(adapter: LocationSelectedActivity.CustomAdapter = LocationSelectedActivity.CustomAdapter(arrayOf())): RecyclerView.Adapter<LocationSelectedActivity.CustomAdapter.ViewHolder> {
        val purchaseList = findViewById<RecyclerView>(R.id.spendingList)
        purchaseList.adapter = adapter
        purchaseList.layoutManager = LinearLayoutManager(this)
        return adapter
    }

    private class CustomAdapter(private val dataSet: Array<PurchaseObject>) :
            RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        /*
        This is where you define what the list elements are
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemName: TextView = view.findViewById(R.id.itemNameTextView)
            val itemCost: TextView = view.findViewById(R.id.itemCostTextView)
            val rowLayout: FrameLayout = view.findViewById(R.id.purchaseRowLayout)


            /*
            init {
                // Define click listener for the ViewHolder's View.
                view.setOnClickListener{ v: View ->
                    val intent = Intent(v.context, LocationSelectedActivity::class.java)
                    intent.putExtra("itemName", itemName.text)
                    intent.putExtra("itemCost", itemCost.text)
                    v.context.startActivity(intent)
                }
            }*/
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.purchase_history_purchases_iterable, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.itemName.text = dataSet[position].itemName
            viewHolder.itemCost.text = dataSet[position].itemCost
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size
    }


}