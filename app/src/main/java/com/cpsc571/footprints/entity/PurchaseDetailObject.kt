package com.cpsc571.footprints.entity

import android.util.Log
import com.google.firebase.database.DataSnapshot

class PurchaseDetailObject(
        var itemObjects: Array<ItemObject>,
        var imageSource: String?
): JsonObject()
{
    constructor(firebaseObject: DataSnapshot):
            this(firebaseObject.child("itemObjects").children.map { x -> ItemObject(x.child("name").value.toString(), x.child("cost").value.toString()) }.toTypedArray(),
                    firebaseObject.child("imageSource").value.toString())
}