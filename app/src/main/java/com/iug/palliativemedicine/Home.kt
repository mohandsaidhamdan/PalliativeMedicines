package com.iug.palliativemedicine


import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.iug.palliativemedicine.databinding.ActivityHomeBinding

class Home : AppCompatActivity() {


    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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