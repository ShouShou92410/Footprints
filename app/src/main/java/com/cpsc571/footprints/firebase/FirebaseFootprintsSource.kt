package com.cpsc571.footprints.firebase

import android.util.Log
<<<<<<< HEAD
import com.cpsc571.footprints.BuildConfig
=======
import com.cpsc571.footprints.CONSTANTS;
import com.cpsc571.footprints.entity.JsonObject
>>>>>>> e18928927cabe1f05290bd5deb126a71fe4076b0
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

public class FirebaseFootprintsSource: FirebaseFootprints {
    private final var URL = BuildConfig.SERVER_URL

    override fun get(jsonAddress: String, onChange: (value: Any?) -> Unit) {
        val ref = getDatabaseRef(jsonAddress)
<<<<<<< HEAD
        ref.addValueEventListener(object: ValueEventListener {
=======

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
>>>>>>> e18928927cabe1f05290bd5deb126a71fe4076b0
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value
                val value = dataSnapshot.value
                onChange(value)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.d("FirebaseFtprintsSource", "ERROR MESSAGE: " + error.message + ", ERROR DETAILS: " + error.details)
            }
        })
    }

    override fun overwrite(jsonAddress: String, jsonData: String) {
        val ref = getDatabaseRef(jsonAddress)
        ref.setValue(jsonData)
    }

    override fun push(jsonAddress: String, jsonData: JsonObject, id: String?) {
        val ref = getDatabaseRef(jsonAddress)
        var newChild =
            if (id == null) ref.push()
            else ref.child(id)
        newChild.setValue(jsonData)
    }

    override fun delete(jsonAddress: String) {
        val ref = getDatabaseRef(jsonAddress);
        ref.setValue(null);
    }

    private fun getDatabaseRef(jsonAddress: String): DatabaseReference {
        return Firebase.database.getReferenceFromUrl(URL).child(jsonAddress)
    }
}
