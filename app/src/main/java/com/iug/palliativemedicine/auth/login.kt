package com.iug.palliativemedicine.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.iug.palliativemedicine.Favorite
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.databinding.ActivityLoginBinding

class login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Firebase Aut With Email And Password
        firebaseAuth = FirebaseAuth.getInstance()

        // btn Login
        binding.button.setOnClickListener {
            // get email and password
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()
            // check email and password is not Empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // func in Check email and password are correct or not
                login(email, password)
            } else {
                // if email and password is Empty
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            }

        }

         // btn Register
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, signup::class.java))
        }

    }

    // func in Check email and password are correct or not
    private fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    val check = getSharedPreferences("SelectionFavorite", MODE_PRIVATE).getBoolean("che", false)

                    val i: Intent
                    if (check) {
                        i = Intent(this, Home::class.java)
                    } else {
//                        i = Intent(this, signup::class.java)
                        i = Intent(this, Favorite::class.java)
                    }


                    val user = firebaseAuth.currentUser
                    val sheard = getSharedPreferences("user", MODE_PRIVATE)

       // get data user and storage in SharedPreferences
                    val db = Firebase.firestore
                    db.collection("users").whereEqualTo("email", email).get()
                        .addOnSuccessListener { suc ->
                            for (suc in suc) {
                                val typeAcount = suc.getString("typeAcount").toString()
                                val name = suc.getString("name").toString()
                                val phone = suc.getString("phone").toString()
                                val title = suc.getString("title").toString()

                                val edit = sheard.edit()
                                edit.putString("email", email)
                                edit.putBoolean("che", true)
                                edit.putString("password", password)
                                edit.putString("typeAccount", typeAcount)
                                edit.putString("title", title)
                                edit.putString("phone", phone)
                                edit.putString("name", name)
                                edit.apply()
                                startActivity(i)
                                finish()
                                i.putExtra("typeAcount", typeAcount)
                            }
                        }


                } else {
                    // Login failed
                    Toast.makeText(this, "Incorrect password or email.", Toast.LENGTH_SHORT).show()
                }
            }
    }


}