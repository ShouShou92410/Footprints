package com.cpsc571.footprints.entity

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import java.time.LocalDate

class PurchaseObject(
        var total: String?,
        var purchaseDetailKey: String?,
        var date: LocalDate
): JsonObject()
{
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(firebaseObject: DataSnapshot):
            this(firebaseObject.child("total").value.toString(),
                    firebaseObject.child("purchaseDetailKey").value.toString(),
                        LocalDate.parse(firebaseObject.child("date").value.toString() ))
}