package com.sprinthubmobile.example.phoneauthdemo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.sprinthubmobile.example.phoneauthdemo.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.let {
            binding.textviewMain.text = getString(R.string.already_logged_in, user.phoneNumber)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sign_out -> {
                signOut()
                return true
            }
            else ->  return super.onOptionsItemSelected(item)
        }

    }

    private fun signOut() {
        val auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener {
            val currentUser = it.currentUser
            if (currentUser == null) {
                val loginIntent = Intent(this, LoginActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(loginIntent)
                finish()
            }
        }
        auth.signOut()
    }
}
