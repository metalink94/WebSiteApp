package ru.lopav.kzn.websiteapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.lopav.kzn.websiteapp.web.MainActivity

class SplashScreen: AppCompatActivity() {

    private var url: String = BuildConfig.URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkDatabase()
    }

    private fun checkDatabase() {
        val database = FirebaseDatabase.getInstance()
        database.reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null) {
                    url = p0.child(getString(R.string.key_url)).value as String? ?: url
                }
                showMainScreen()
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("URL", p0.message)
                showMainScreen()
            }
        })
    }

    private fun showMainScreen() {
        startActivity(MainActivity.getInstance(this, url))
        finish()
    }
}