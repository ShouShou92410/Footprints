package com.cpsc571.footprints.entity

import android.util.Log
import com.google.firebase.database.DataSnapshot

class PurchaseDetailObject(
        var itemObjects: List<ItemObject>,
        var imageSource: String?
): JsonObject()
{
    constructor(firebaseObject: DataSnapshot):
            this(firebaseObject.child("itemObjects").children.map { x -> ItemObject(x.child("name").value.toString(), x.child("cost").value.toString()) },
                    firebaseObject.child("imageSource").value.toString())
}