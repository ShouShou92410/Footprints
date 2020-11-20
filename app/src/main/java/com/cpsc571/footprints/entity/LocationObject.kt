package com.cpsc571.footprints.entity

import android.util.Log
import com.google.firebase.database.DataSnapshot

class LocationObject(
        var name: String?,
        var address: String?,
        var longitude: String?,
        var latitude: String?
): JsonObject()
{
    constructor(firebaseObject: DataSnapshot):
            this(firebaseObject.child("name").value.toString(),
                firebaseObject.child("address").value.toString(),
                firebaseObject.child("longitude").value.toString(),
                firebaseObject.child("latitude").value.toString())
}