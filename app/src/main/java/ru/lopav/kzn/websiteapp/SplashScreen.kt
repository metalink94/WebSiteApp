package ru.lopav.kzn.websiteapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.lopav.kzn.websiteapp.push.Constants
import ru.lopav.kzn.websiteapp.web.MainActivity

class SplashScreen : AppCompatActivity() {

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
                    if (p0.child(Constants.DATABASE_IS_FINISH).value as? Boolean == true) {
                        finishApp()
                        return
                    }
                    val webUrl = setUrl(p0)
                    showMainScreen(webUrl)
                }
                Log.d("DataBase", "get database ${p0.value}")
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("URL", p0.message)
                showMainScreen(null)
            }
        })
    }

    private fun setUrl(dataSnapshot: DataSnapshot): String? {
        return when(BuildConfig.APPLICATION_ID) {
            Constants.FIRST_APP -> dataSnapshot.child(Constants.DATABASE_URL).value as String?
            Constants.SECOND_APP -> dataSnapshot.child(Constants.DATABASE_URL_2).value as String?
            else -> dataSnapshot.child(Constants.DATABASE_URL_3).value as String?
        }
    }

    private fun finishApp() {
        finish()
    }

    private fun showMainScreen(url: String?) {
        startActivity(MainActivity.getInstance(this, url ?: BuildConfig.URL))
        finish()
    }
}
