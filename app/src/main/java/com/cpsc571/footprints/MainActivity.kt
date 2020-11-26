package com.cpsc571.footprints

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View


import com.cpsc571.footprints.entity.User
import com.cpsc571.footprints.firebase.FirebaseFootprints


import com.cpsc571.footprints.firebase.FirebaseFootprintsSource
import com.cpsc571.footprints.vision.PriceExtractor
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 420
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        signInButton.setOnClickListener{
            handleLoading(true)
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()


        // REMOVE THIS ALL BELOW

        val textScanner = PriceExtractor
        val receipts = arrayOf(R.drawable.receipt8, R.drawable.receipt13, R.drawable.receipt15)
        for ((index, receipt) in receipts.withIndex()) {
            val btmp = BitmapFactory.decodeStream(resources.openRawResource(receipt))
            textScanner.getTotalCost(btmp) {}
        }













        val currentUser = auth.currentUser
        handleSignIn(currentUser)
    }

    private fun handleSignIn(user: FirebaseUser?) {
        if (user != null) {
            handleUserCreation(user)

            val dashboardIntent = Intent(this, DashboardActivity::class.java)
            startActivity(dashboardIntent)
        }
    }

    private fun handleUserCreation(user: FirebaseUser) {
        val firebaseDB: FirebaseFootprints = FirebaseFootprintsSource()
        val jsonAddress = "Users/${user.uid}"
        val jsonData = User(user.displayName, user.email)
        val onChange: (DataSnapshot) -> Unit = {
            snapshot: DataSnapshot ->
                if (!snapshot.exists()) {
                    firebaseDB.push("Users", jsonData, user.uid)
                }
        }

        firebaseDB.get(jsonAddress, onChange)
    }

    private fun handleLoading(isLoading: Boolean) {
        if (isLoading){
            signInButton.visibility = View.GONE
            loadingIcon.visibility = View.VISIBLE
        }
        else {
            signInButton.visibility = View.VISIBLE
            loadingIcon.visibility = View.GONE
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception

            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("MainActivity", "firebaseAuthWithGoogle" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                }
                catch(e: ApiException) {
                    Log.w("MainActivity", "Google sign in failed", e)
                }
            }
            else {
                Log.w("MainActivity", exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) {  task ->
                    if (task.isSuccessful) {
                        Log.d("MainActivity", "signInWithCredential:success")

                        handleLoading(false)
                        handleSignIn(auth.currentUser)
                        finish()
                    }
                    else {
                        Log.w("MainActivity", "signIntWithCredential:failure", task.exception)
                    }
                }
    }
}
