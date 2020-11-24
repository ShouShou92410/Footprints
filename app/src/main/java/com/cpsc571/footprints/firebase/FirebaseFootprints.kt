package com.cpsc571.footprints.firebase

import android.graphics.Bitmap
import com.cpsc571.footprints.entity.JsonObject
import com.google.firebase.database.DataSnapshot

public interface FirebaseFootprints {
    /**
     * @param jsonAddress Location of where you want to write, starting from root
     * @param onChange this is where data snapshots are passed to
     * @param notifyAllChanges set as true if you want updated values sent as well
     * Gets data at the address. Data can be Maps, Lists, Strings, Booleans, and more.
     * Whenever the data at the address is updated, the @param onChange will get called with the new value.
     */
    public fun get(jsonAddress: String, onChange: (value: DataSnapshot) -> Unit, notifyAllChanges: Boolean = false)

    /**
     * @param jsonAddress Location of where you want to write, starting from root
     * @param jsonData Data you wish to overwrite with
     * Replaces data at the given address with new data
     */
    public fun overwrite(jsonAddress: String, jsonData: String)

    /**
     * @param jsonAddress Location of where you want to write, starting from root
     * @param jsonData Data you wish to insert
     * @param id Optional id for the new json data
     * @return The key of the pushed value
     * Inserts a new value at the given address. Existing data will be a sibling of the new data inserted
     */
    public fun push(jsonAddress: String, jsonData: JsonObject, id: String? = null): String?

    /**
     * @param jsonAddress Location of where you want to write, starting from root
     */
    public fun delete(jsonAddress: String)

    /**
     * @param jsonAddress Address where the JpgObject is
     * @param onChange Callback when bitmap is processed
     * Reads an image written from writeBitmap()
     */
    public fun readBitmap(jsonAddress: String, onChange: (bitmap: Bitmap) -> Unit)

    /**
     * @param bitmap What to save
     * @return The key of the pushed value
     * Saves a bitmap as 64 bit input
     */
    public fun pushBitmap(jsonAddress: String, bitmap: Bitmap): String?
}