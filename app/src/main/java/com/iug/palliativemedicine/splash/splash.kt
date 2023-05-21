package com.iug.palliativemedicine.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.airbnb.lottie.LottieAnimationView
import com.iug.palliativemedicine.Favorite
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.R
import com.iug.palliativemedicine.auth.login
import com.iug.palliativemedicine.databinding.ActivitySignupBinding
import com.iug.palliativemedicine.databinding.ActivitySplashBinding

class splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    lateinit var lottieAnimationView: LottieAnimationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
       val check = getSharedPreferences("SelectionFavorite" , MODE_PRIVATE).getBoolean("che" , false)

        lottieAnimationView = findViewById(R.id.lottie)
        lottieAnimationView.animate().translationY((0).toFloat()).setDuration(4000).setStartDelay(1500).withEndAction {
//        lottieAnimationView.animate().setDuration(3000).withEndAction {
            val ch=  getSharedPreferences("user", MODE_PRIVATE).getBoolean("che" , false)

                if (ch){
                    if (check) {
                        startActivity( Intent(this, Home::class.java))
                        finish()
                    }else{
                        startActivity(Intent(this,Favorite::class.java))
                        finish()
                    }

                }else{
                    startActivity(Intent(this , login::class.java))
                    finish()
                }
            }

        }




    }