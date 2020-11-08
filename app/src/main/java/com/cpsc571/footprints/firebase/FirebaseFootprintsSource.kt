package com.cpsc571.footprints.firebase

import android.util.Log
import com.cpsc571.footprints.CONSTANTS;
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

public class FirebaseFootprintsSource: FirebaseFootprints {
    private final var URL = CONSTANTS.FIREBASE_URL

    override fun get(jsonAddress: String, onChange: (value: Any?) -> Unit) {
        val ref = getDatabaseRef(jsonAddress)

        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue()
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

    override fun push(jsonAddress: String, jsonData: String) {
        val ref = getDatabaseRef(jsonAddress)
        val newChild = ref.push()
        newChild.setValue(jsonData)
    }

    override fun delete(jsonAddress: String) {
        val ref = getDatabaseRef(jsonAddress);
        ref.setValue(null);
    }

    private fun getDatabaseRef(jsonAddress: String): DatabaseReference {
        return Firebase.database.getReferenceFromUrl(URL)
    }
}