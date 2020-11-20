package com.cpsc571.footprints.entity

import android.util.Log
import com.google.firebase.database.DataSnapshot

class PurchaseObject(
        var itemName: String?,
        var itemCost: String?
): JsonObject()
{
    constructor(firebaseObject: DataSnapshot):
            this(firebaseObject.child("itemName").value.toString(),
                    firebaseObject.child("itemCost").value.toString())
}