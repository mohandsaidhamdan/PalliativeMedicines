package com.iug.palliativemedicine.topic

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.databinding.ActivityAddtopicBinding
import com.iug.palliativemedicine.databinding.BootomdialogBinding
import com.squareup.picasso.Picasso
import java.util.*

class AddTopic : AppCompatActivity() {
    lateinit var binding : ActivityAddtopicBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore
    private val IMAGE_PICK_REQUEST = 100
    lateinit var images: ImageView
    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddtopicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            images = binding.image
            var check = false
            images.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK_REQUEST)
                check = true

            }
            add.setOnClickListener {
                if (check) {
                    if (titlesEt.text.toString().isNotEmpty() ) {
                        createTopic(url, titlesEt.text.toString())
                        Toast.makeText(
                            this@AddTopic,
                            "New topic added successfully",
                            Toast.LENGTH_LONG
                        ).show()
                        val i = Intent(this@AddTopic , Home::class.java)
                        startActivity(i)
                        finish()
                    } else
                        Toast.makeText(this@AddTopic, "All fields are required", Toast.LENGTH_SHORT).show()

                } else
                    Toast.makeText(this@AddTopic, "Add a photo, please", Toast.LENGTH_SHORT).show()


            }

        }
    }
    private fun createTopic(uri: String, name: String) {
        // Create a new user with a first and last name
        val Topic = hashMapOf(
            "uri" to uri,
            "name" to name
        )
        //  DwnloadImage(uri)

        val db = Firebase.firestore
        // Add a new document with a generated ID
        db.collection("Topic")
            .add(Topic)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "ContentValues.TAG",
                    "DocumentSnapshot added with ID: ${documentReference.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.w("ContentValues.TAG", "Error adding document", e)
            }
    }
    private fun uploadImage(imageUri: Uri) {
        val storageRef = storage.reference
        url = "images/${imageUri.lastPathSegment}"
        val imageRef = storageRef.child(url)
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val downloadUrl = task.result?.storage?.downloadUrl
            } else {
                // Image upload failed
                // Handle the failure
                Toast.makeText(this, "failed upload image", Toast.LENGTH_SHORT).show()

            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            try{
                images.setImageURI(selectedImageUri)
                data.data?.let { uploadImage(it) }
            }catch (e:Exception){
                Log.d("Ecoption" , e.message.toString())
            }
        }
    }
    private fun createdetails(uri: String, name: String, title: String, details: String , date : Date) {
        val Topic = hashMapOf(
            "uri" to uri,
            "name" to name ,
            "title" to title ,
            "details" to details ,
            "date" to date
        )
//          DwnloadImage(uri , images)

        val db = Firebase.firestore
        // Add a new document with a generated ID
        db.collection("details")
            .add(Topic)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "ContentValues.TAG",
                    "DocumentSnapshot added with ID: ${documentReference.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.w("ContentValues.TAG", "Error adding document", e)
            }
    }
    fun getCurrentDate(): Date {
        return Date()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this@AddTopic , Home::class.java)
        startActivity(i)
        finish()
    }


    private fun DwnloadImage(uri: String  , imageView2 : ImageView) {
        val storageRef =
            storage.reference.child(uri) // Replace "images/image.jpg" with your actual image path in Firebase Storage

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()

            Picasso.get().load(imageUrl).into(imageView2)
            // Use the imageUrl as needed (e.g., display the image, store it in a database, etc.)
        }.addOnFailureListener { exception ->
            // Handle any errors that occurred while retrieving the download URL
        }
    }


}