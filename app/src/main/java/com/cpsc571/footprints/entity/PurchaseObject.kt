package com.cpsc571.footprints.entity

import android.util.Log
import com.google.firebase.database.DataSnapshot

class PurchaseObject(
        var total: String?,
        var purchaseDetailKey: String?
): JsonObject()
{
    constructor(firebaseObject: DataSnapshot):
            this(firebaseObject.child("total").value.toString(),
                    firebaseObject.child("purchaseDetailKey").value.toString())
}