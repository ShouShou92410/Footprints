package com.cpsc571.footprints

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
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

        val currentUser = auth.currentUser
        handleUser(currentUser)
    }

    private fun handleUser(user: FirebaseUser?) {
        if (user != null) {
            val dashboardIntent = Intent(this, DashboardActivity::class.java)
            startActivity(dashboardIntent)
        }
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
                    Log.d("SingInActivity", "firebaseAuthWithGoogle" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                }
                catch(e: ApiException) {
                    Log.w("SingInActivity", "Google sign in failed", e)
                }
            }
            else {
                Log.w("SingInActivity", exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) {  task ->
                    if (task.isSuccessful) {
                        Log.d("SingInActivity", "signInWithCredential:success")
                        handleLoading(false)
                        val dashboardIntent = Intent(this, DashboardActivity::class.java)
                        startActivity(dashboardIntent)
                        finish()
                    }
                    else {
                        Log.w("SingInActivity", "signIntWithCredential:failure", task.exception)
                    }
                }
    }
}