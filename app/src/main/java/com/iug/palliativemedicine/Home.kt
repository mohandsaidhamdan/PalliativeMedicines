package com.iug.palliativemedicine


import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.iug.palliativemedicine.databinding.ActivityHomeBinding

class Home : AppCompatActivity() {
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


         analytics = Firebase.analytics
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Home")
        }


        val navView: BottomNavigationView = binding.navView
//     val typeAcount  = intent.getStringExtra("typeAcount").toString()
//        Toast.makeText(this, "typeAcount typeAcount $typeAcount", Toast.LENGTH_SHORT).show()
        val navController = findNavController(R.id.nav_host_fragment_activity_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,R.id.navigation_home,
                R.id.navigation_notifications,  R.id.navigation_chat,  R.id.navigation_notifications
            )
        )


//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }


    override fun onStart() {
        super.onStart()

    }

    override fun onDestroy() {
        super.onDestroy()
    }
    override fun onBackPressed() {

    }

}