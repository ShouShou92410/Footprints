package com.cpsc571.footprints.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.cpsc571.footprints.BuildConfig
import com.cpsc571.footprints.entity.JpgObject
import com.cpsc571.footprints.entity.JsonObject
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream

public class FirebaseFootprintsSource: FirebaseFootprints {
    private final var URL = BuildConfig.SERVER_URL

    override fun get(jsonAddress: String, onChange: (value: DataSnapshot) -> Unit, notifyAllChanges: Boolean) {
        val ref = getDatabaseRef(jsonAddress)
        val listener = object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value
                onChange(dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.d("FirebaseFtprintsSource", "ERROR MESSAGE: " + error.message + ", ERROR DETAILS: " + error.details)
            }
        }

        if (notifyAllChanges) {
            ref.addValueEventListener(listener)
        } else {
            ref.addListenerForSingleValueEvent(listener)
        }
    }

    override fun overwrite(jsonAddress: String, jsonData: String) {
        val ref = getDatabaseRef(jsonAddress)
        ref.setValue(jsonData)
    }

    override fun push(jsonAddress: String, jsonData: JsonObject, id: String?): String? {
        val ref = getDatabaseRef(jsonAddress)
        var newChild =
            if (id == null) ref.push()
            else ref.child(id)
        newChild.setValue(jsonData)
        return newChild.key
    }

    override fun delete(jsonAddress: String) {
        val ref = getDatabaseRef(jsonAddress);
        ref.setValue(null);
    }

    override fun compressBitmapForFirebase(bitmap: Bitmap): String? {
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)

        val byteArray = outStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    override fun uncompressBitmapForFirebase(src: String): Bitmap {
        val byteArray = Base64.decode(src, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun getDatabaseRef(jsonAddress: String): DatabaseReference {
        return Firebase.database.getReferenceFromUrl(URL).child(jsonAddress)
    }

}
