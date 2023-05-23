package com.iug.palliativemedicine.topic

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.databinding.ActivityAdddetailsBinding
import java.util.*
import kotlin.collections.ArrayList

class AddAdvice : AppCompatActivity() {
    lateinit var binding : ActivityAdddetailsBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore
    private val Video_PICK_REQUEST = 200
    private val IMAGE_PICK_REQUEST = 100
    lateinit var images: ImageView
    lateinit var url: String
     var uriViedo: String = ""
    lateinit var topic: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdddetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        topic = ArrayList<String>()
        getTopic()
        val adpter: ArrayAdapter<String> = ArrayAdapter(
            this@AddAdvice,
            R.layout.support_simple_spinner_dropdown_item,
            topic
        )
        binding.TopicEdt.setAdapter(adpter)

        binding.apply {

            images = binding.image

            var check = false
            var checkV = false
            images.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK_REQUEST)
                check = true

            }

            video.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "video/*"
                startActivityForResult(intent, Video_PICK_REQUEST)
                checkV = true
            }
            add.setOnClickListener {
                if (check) {
                    if (titlesEt.text.toString().isNotEmpty() && detelisEt.text.toString().isNotEmpty()) {

                        createdetails(url,  binding.TopicEdt.text.toString() , titlesEt.text.toString() ,detelisEt.text.toString() ,  getCurrentDate() , uriViedo )
                        Toast.makeText(this@AddAdvice, "New advice added successfully", Toast.LENGTH_LONG).show()
                        val i = Intent(this@AddAdvice , Home::class.java)
                        startActivity(i)
                        finish()
                    } else
                        Toast.makeText(this@AddAdvice, "All fields are required", Toast.LENGTH_SHORT).show()

                } else
                    Toast.makeText(this@AddAdvice, "Add a photo, please", Toast.LENGTH_SHORT).show()


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
    private fun uploadVideo(ViedoUri: Uri) {
        val storageRef = storage.reference
        uriViedo = "Video/${ViedoUri.lastPathSegment}"
        val imageRef = storageRef.child(uriViedo)
        val uploadTask = imageRef.putFile(ViedoUri)

        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val downloadUrl = task.result?.storage?.downloadUrl
            } else {
                // Image upload failed
                // Handle the failure
                Toast.makeText(this, "failed upload Video", Toast.LENGTH_SHORT).show()

            }
        }
    }








    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Video_PICK_REQUEST){
            val selectedVideoUri = data!!.data
//            videos.setVideoURI(selectedVideoUri)
            Toast.makeText(this@AddAdvice, "New video added successfully", Toast.LENGTH_SHORT).show()

            selectedVideoUri?.let { uploadVideo(it) }
        }

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


    private fun createdetails(uri: String, topic: String, title: String, description: String, date : Date, uriViedo : String) {
        val Topic = hashMapOf(
            "uri" to uri,
            "topic" to topic ,
            "title" to title ,
            "description" to description ,
            "date" to date ,
            "uriViedo" to uriViedo ,
            "hidden" to false
        )
//          DwnloadImage(uri , images)

        val db = Firebase.firestore
        // Add a new document with a generated ID
        db.collection("advice")
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
        val i = Intent(this@AddAdvice , Home::class.java)
        startActivity(i)
        finish()
    }

          fun getTopic(){
              db = Firebase.firestore
              db.collection("Topic").get().addOnSuccessListener {
                  for(doc in it){
                      val name = doc.get("name").toString()
                      topic.add(name)
                  }
              }
          }
}