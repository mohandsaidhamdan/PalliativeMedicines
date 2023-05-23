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
import com.iug.palliativemedicine.databinding.ActivityUpdateTopicBinding
import com.iug.palliativemedicine.databinding.BootomdialogBinding
import com.iug.palliativemedicine.model.topic
import com.squareup.picasso.Picasso
import java.util.*

class UpdateTopic : AppCompatActivity() {
    lateinit var binding: ActivityUpdateTopicBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore
    private val IMAGE_PICK_REQUEST = 100
    lateinit var images: ImageView
    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateTopicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imgeUri = intent.getStringExtra("imageUri").toString()
        val title = intent.getStringExtra("title").toString()
        DwnloadImage(imgeUri, binding.image)
        binding.titlesEt.setText(title)
        db = Firebase.firestore
        binding.apply {
            images = binding.image
            var check = false
            images.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK_REQUEST)
                check = true

            }




            update.setOnClickListener {

                if (titlesEt.text.toString().isNotEmpty()) {
                    if (check)
                        update(url, titlesEt.text.toString() ,title)
                    else
                        update(imgeUri, titlesEt.text.toString(),title)

                } else
                    Toast.makeText(
                        this@UpdateTopic,
                        "أدخل العنوان الجديد من فضلك",
                        Toast.LENGTH_SHORT
                    ).show()


            }


        }
    }

    fun update(uri: String, name: String , oldName : String) {
        val tag = name.replace(" ", "s")
        val data = hashMapOf<String, Any>(
            "name" to name,
            "uri" to uri,
            "tag" to tag
        )
        db.collection("Topic").whereEqualTo("name", oldName).get().addOnSuccessListener {
            for (doc in it) {
                val id = doc.id

                db.collection("Topic").document(id).update(data).addOnSuccessListener {
                    Toast.makeText(
                        this@UpdateTopic,
                        "تم التعديل بنجاح",
                        Toast.LENGTH_LONG
                    ).show()
                    val i = Intent(this@UpdateTopic, Home::class.java)
                    startActivity(i)
                    finish()
                }
            }

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
            try {
                images.setImageURI(selectedImageUri)
                data.data?.let { uploadImage(it) }
            } catch (e: Exception) {
                Log.d("Ecoption", e.message.toString())
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this@UpdateTopic, Home::class.java)
        startActivity(i)
        finish()
    }


    private fun DwnloadImage(uri: String, imageView2: ImageView) {
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