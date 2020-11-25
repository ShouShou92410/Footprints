package com.cpsc571.footprints.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.cpsc571.footprints.R
import com.cpsc571.footprints.entity.ItemObject

 class ItemsAdapter(
    private var itemList: ArrayList<ItemObject>
) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.purchasedItemName_tv)
        var cost: TextView = view.findViewById(R.id.purchasedItemCost_tv)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.purchased_item, parent, false)
        return ViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.name.text = item.name
        holder.cost.text = item.cost
    }
    override fun getItemCount(): Int {
        return itemList.size
    }
}