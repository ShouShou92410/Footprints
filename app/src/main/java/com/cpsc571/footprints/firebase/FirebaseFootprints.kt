package com.cpsc571.footprints.firebase

import com.google.firebase.database.ValueEventListener

public interface FirebaseFootprints {
    /**
     * @param jsonAddress Location of where you want to write, starting from root
     * @param onChange this is where values are passed to
     * Gets data at the address. Data can be Maps, Lists, Strings, Booleans, and more.
     * Whenever the data at the address is updated, the @param onChange will get called with the new value.
     */
    public fun get(jsonAddress: String, onChange: (value: Any?) -> Unit)

    /**
     * @param jsonAddress Location of where you want to write, starting from root
     * @param jsonData Data you wish to overwrite with
     * Replaces data at the given address with new data
     */
    public fun overwrite(jsonAddress: String, jsonData: String)

    /**
     * @param jsonAddress Location of where you want to write, starting from root
     * @param jsonData Data you wish to insert
     * Inserts a new value at the given address. Existing data will be a sibling of the new data inserted
     */
    public fun push(jsonAddress: String, jsonData: String)

    /**
     * @param jsonAddress Location of where you want to write, starting from root
     */
    public fun delete(jsonAddress: String)
}