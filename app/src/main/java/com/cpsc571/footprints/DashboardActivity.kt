package com.cpsc571.footprints

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = Firebase.auth
        signOutButton.setOnClickListener{
            signOut()
        }
    }

    private fun signOut() {
        auth.signOut()

        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    /** Called when the user taps the Location History button */
    fun locationHistoryPage(view: View) {
        val intent = Intent(this, LocationHistoryActivity::class.java)
        startActivity(intent)
    }

    /** Called when the user taps the GPS Tracker button */
    fun trackerPage(view: View) {
        val intent = Intent(this, TrackerActivity::class.java)
        startActivity(intent)
    }
}