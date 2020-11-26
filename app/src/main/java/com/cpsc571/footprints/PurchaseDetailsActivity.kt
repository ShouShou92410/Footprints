package com.cpsc571.footprints

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cpsc571.footprints.Adapter.ItemsAdapter
import com.cpsc571.footprints.entity.*
import com.cpsc571.footprints.firebase.FirebaseFootprints
import com.cpsc571.footprints.firebase.FirebaseFootprintsSource
import com.google.firebase.database.DataSnapshot
import kotlinx.android.synthetic.main.activity_purchase_details.*
import kotlinx.android.synthetic.main.image_popup.view.*

class PurchaseDetailsActivity : AppCompatActivity() {
    private lateinit var purchaseDetail: PurchaseDetailObject
    private var items = ArrayList<ItemObject>()
    private lateinit var itemsAdapter: ItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_details)

        setup()
    }

    private fun setup() {
        val firebaseFootprints: FirebaseFootprints = FirebaseFootprintsSource()

        purchasedTotalCost_tv.text = intent.getStringExtra("purchaseTotal")

        val pdID = intent.getStringExtra("purchaseDetailKey")
        val onChange: (DataSnapshot) -> Unit = {
                value: DataSnapshot ->
            purchaseDetail = PurchaseDetailObject(value)
            items.addAll(purchaseDetail.itemObjects)
            itemsAdapter.notifyDataSetChanged()
        }
        firebaseFootprints.get("PurchaseDetail/${pdID}", onChange)

        setupItemsAdapter(items)
    }

    private fun setupItemsAdapter(dataSet: ArrayList<ItemObject>) {
        itemsAdapter = ItemsAdapter(dataSet)

        val layoutManager = LinearLayoutManager(applicationContext)
        purchasedItemList_rv.layoutManager = layoutManager
        purchasedItemList_rv.itemAnimator = DefaultItemAnimator()
        purchasedItemList_rv.adapter = itemsAdapter
    }

    fun showReceiptImage(view: View) {
        val firebase: FirebaseFootprints = FirebaseFootprintsSource()

        val dialogView = LayoutInflater.from(this).inflate(R.layout.image_popup,null)
        val dialog = Dialog(this)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        dialog.show();

        val imageBitmap = firebase.uncompressBitmapForFirebase(purchaseDetail.imageSource.toString())
        dialogView.receiptImageView.setImageBitmap(imageBitmap)
    }
}