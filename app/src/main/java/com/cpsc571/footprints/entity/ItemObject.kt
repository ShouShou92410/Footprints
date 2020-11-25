package com.cpsc571.footprints.entity

import android.util.Log
import com.google.firebase.database.DataSnapshot

class ItemObject(
        var name: String?,
        var cost: String?
): JsonObject()
{
    constructor(firebaseObject: DataSnapshot):
            this(firebaseObject.child("name").value.toString(),
                    firebaseObject.child("cost").value.toString())
}