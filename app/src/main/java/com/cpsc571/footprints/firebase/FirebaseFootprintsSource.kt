package com.cpsc571.footprints.firebase

import android.util.Log
import com.cpsc571.footprints.BuildConfig
import com.cpsc571.footprints.entity.JsonObject
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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
