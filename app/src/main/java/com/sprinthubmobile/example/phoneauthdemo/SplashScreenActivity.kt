package com.sprinthubmobile.example.phoneauthdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            Timber.i("User not logged in")
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(loginIntent)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
