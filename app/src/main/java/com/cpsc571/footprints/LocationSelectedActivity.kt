package com.cpsc571.footprints

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cpsc571.footprints.entity.ItemObject
import com.cpsc571.footprints.entity.PurchaseDetailObject
import com.cpsc571.footprints.entity.PurchaseObject
import com.cpsc571.footprints.firebase.FirebaseFootprints
import com.cpsc571.footprints.firebase.FirebaseFootprintsSource
import com.cpsc571.footprints.vision.PriceExtractor
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_location_selected.*
import java.io.File
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class LocationSelectedActivity : AppCompatActivity() {
    companion object {
        private const val RC_RECEIPT_CAPTURE = 100
    }

    private var locationID: String? = null
    private lateinit var photoFile: File
    private var purchases = ArrayList<PurchaseObject>()
    private lateinit var adapter: CustomAdapter

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

        setup()
        //Log.d("LocationSelectedActivty", "longitude: ${longitude}, latitude: $latitude")
    }

    private fun updateData() {
        val firebaseDB: FirebaseFootprints = FirebaseFootprintsSource()
        val priceExtractor = PriceExtractor
        val imageBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

        priceExtractor.getTotalCost(imageBitmap) {
            itemsPairingsAndTotal: Pair<List<ItemObject>, String> ->

            val newPurchaseDetailObject = PurchaseDetailObject(itemsPairingsAndTotal.first, firebaseDB.compressBitmapForFirebase(imageBitmap))
            val detailKey = firebaseDB.push("PurchaseDetail", newPurchaseDetailObject)

            val newPurchaseObject = PurchaseObject(itemsPairingsAndTotal.second, detailKey, LocalDate.now().atStartOfDay().toInstant(
                ZoneOffset.UTC).toEpochMilli())

            firebaseDB.push("Receipts/${Firebase.auth.currentUser?.uid}/${locationID}", newPurchaseObject)
            purchases.add(newPurchaseObject)
            adapter.notifyDataSetChanged()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == RC_RECEIPT_CAPTURE) {
            updateData()
        }
        else {
            Toast.makeText(this, "No image found.", Toast.LENGTH_LONG).show()
        }
    }

    fun scanReceipt(view: View) {
        val fileName: String = System.currentTimeMillis().toString()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile(fileName)

        val fileProvider = FileProvider.getUriForFile(this, "com.cpsc571.footprints.fileprovider", photoFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        startActivityForResult(intent, RC_RECEIPT_CAPTURE)
    }

    private fun setup() {
        val firebaseFootprints: FirebaseFootprints = FirebaseFootprintsSource()
        val currentUser = Firebase.auth.currentUser
        val onChange: (DataSnapshot) -> Unit = {
                receipts: DataSnapshot ->
            if (receipts != null && !receipts.exists()) {
                // TODO Show empty locations
            } else {
                val data = mutableListOf<PurchaseObject>()
                receipts?.children?.forEach(fun(purchaseSnapshot: DataSnapshot) {
                    data.add(PurchaseObject(purchaseSnapshot))
                })
                purchases.addAll(data.toTypedArray())
                adapter.notifyDataSetChanged()
            }
        }
        firebaseFootprints.get("Receipts/${currentUser?.uid}/${locationID}", onChange)

        setupAdapter(purchases)
    }

    private fun setupAdapter(dataSet: ArrayList<PurchaseObject>) {
        adapter = CustomAdapter(dataSet)

        val layoutManager = LinearLayoutManager(applicationContext)
        receiptList.layoutManager = layoutManager
        receiptList.itemAnimator = DefaultItemAnimator()
        receiptList.adapter = adapter
    }

    private class CustomAdapter(private val dataSet: ArrayList<PurchaseObject>) :
            RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

        /*
        This is where you define what the list elements are
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val total: TextView = view.findViewById(R.id.totalTextView)
            val receiptDate: TextView = view.findViewById(R.id.receiptDateTextView)
            val rowLayout: FrameLayout = view.findViewById(R.id.receiptRowLayout)

            lateinit var purchaseDetailKey: String
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.scanned_receipts_iterable, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.total.text = dataSet[position].total
            viewHolder.receiptDate.text = Instant.ofEpochMilli(dataSet[position].date).atZone(ZoneOffset.UTC).toLocalDate().toString()
            viewHolder.purchaseDetailKey = dataSet[position].purchaseDetailKey.toString()

            viewHolder.itemView.setOnClickListener{ v: View ->
                val intent = Intent(v.context, PurchaseDetailsActivity::class.java)
                intent.putExtra("purchaseDetailKey", dataSet[position].purchaseDetailKey)
                v.context.startActivity(intent)
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size
    }
}