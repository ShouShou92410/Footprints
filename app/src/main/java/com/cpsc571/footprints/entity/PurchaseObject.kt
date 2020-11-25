package com.cpsc571.footprints.entity

import com.google.firebase.database.DataSnapshot

class PurchaseObject(
        var total: String?,
        var purchaseDetailKey: String?,
        var date: Long
): JsonObject()
{
    constructor(firebaseObject: DataSnapshot):
            this(firebaseObject.child("total").value.toString(),
                    firebaseObject.child("purchaseDetailKey").value.toString(),
                        firebaseObject.child("date").value as Long)
}