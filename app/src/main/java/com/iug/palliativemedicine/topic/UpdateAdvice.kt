package com.iug.palliativemedicine.topic

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.databinding.ActivityAdddetailsBinding
import com.iug.palliativemedicine.databinding.ActivityUpdateAdviceBinding
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class UpdateAdvice : AppCompatActivity() {
    lateinit var binding: ActivityUpdateAdviceBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore
    private val Video_PICK_REQUEST = 200
    private val IMAGE_PICK_REQUEST = 100
    lateinit var images: ImageView
    lateinit var urlImageNew: String
    var uriViedo: String = ""
    lateinit var player: SimpleExoPlayer
    lateinit var playerView: PlayerView
    lateinit var topics: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateAdviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.firestore

        images = binding.image
        val oldTitle = intent.getStringExtra("title").toString()
        val description = intent.getStringExtra("description").toString()
        val uriImageOld = intent.getStringExtra("uri").toString()
        val topic = intent.getStringExtra("topic").toString()
        var uriViedoOld = ""
        topics = ArrayList<String>()
        getTopic()
        val adpters: ArrayAdapter<String> = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            topics
        )




        binding.UpdateTopic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.isNullOrEmpty()) {
                    binding.UpdateTopic.setAdapter(adpters)
                }
            }
        })











        db.collection("advice").whereEqualTo("title", oldTitle).get().addOnSuccessListener {
            for (doc in it) {
                uriViedoOld = doc.get("uriViedo").toString()
                if (uriViedo.isNullOrEmpty() || uriViedo == "") {
                    DwnloadVideo(uriViedo)
                } else {
                    binding.playerView.visibility = View.GONE
                }

            }
        }

        if (uriImageOld.isNotEmpty()) {
            DwnloadImage(uriImageOld, binding.image)
        }
        binding.updateTitles.setText(title)
        binding.UpdateTopic.setText(topic)
        binding.updateDesc.setText(description)

        var checkImageUpdate = false
        binding.image.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_REQUEST)
            checkImageUpdate = true
        }
        var checkUpdateViedo = false
        binding.video.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "video/*"
            startActivityForResult(intent, Video_PICK_REQUEST)
            checkUpdateViedo = true
        }
        binding.btnUpdate.setOnClickListener {

            if (checkImageUpdate) {
                update(
                    urlImageNew,
                    binding.UpdateTopic.text.toString(),
                    binding.updateTitles.text.toString(),
                    binding.updateDesc.text.toString(),
                    getCurrentDate(),
                    uriViedo,
                    oldTitle
                )
            } else {
                if (checkUpdateViedo) {
                    update(
                        uriImageOld,
                        binding.UpdateTopic.text.toString(),
                        binding.updateTitles.text.toString(),
                        binding.updateDesc.text.toString(),
                        getCurrentDate(),
                        uriViedo,
                        oldTitle
                    )
                } else {
                    update(
                        uriImageOld,
                        binding.UpdateTopic.text.toString(),
                        binding.updateTitles.text.toString(),
                        binding.updateDesc.text.toString(),
                        getCurrentDate(),
                        uriViedoOld,
                        oldTitle
                    )
                }
            }
        }


    }

    private fun uploadImage(imageUri: Uri) {
        val storageRef = storage.reference
        urlImageNew = "images/${imageUri.lastPathSegment}"
        val imageRef = storageRef.child(urlImageNew)
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
        if (requestCode == Video_PICK_REQUEST) {
            val selectedVideoUri = data!!.data
//            videos.setVideoURI(selectedVideoUri)
            Toast.makeText(this@UpdateAdvice, "New video added successfully", Toast.LENGTH_SHORT)
                .show()

            selectedVideoUri?.let { uploadVideo(it) }
        }

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

    private fun DwnloadImage(uri: String, imageView2: ImageView) {
        val storage = FirebaseStorage.getInstance()
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

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this@UpdateAdvice, Home::class.java)
        startActivity(i)
        finish()
    }

    private fun DwnloadVideo(uri: String) {
        if (uri.isEmpty() || uri == "") {
            return
        } else {


            val storageRef =
                storage.reference.child(uri) // Replace "images/image.jpg" with your actual image path in Firebase Storage

            storageRef.downloadUrl.addOnSuccessListener { uri ->
                playerView = binding.playerView
                val ViedoUrl = uri.toString()
                player = SimpleExoPlayer.Builder(this).build()
                playerView.player = player
                val mediaItem = MediaItem.fromUri(ViedoUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
//            player.play()
                // Use the imageUrl as needed (e.g., display the image, store it in a database, etc.)
            }.addOnFailureListener { exception ->
                // Handle any errors that occurred while retrieving the download URL
            }
        }
    }


    fun update(
        uri: String,
        topic: String,
        title: String,
        description: String,
        date: Date,
        uriViedo: String,
        oldTitle: String
    ) {
        val data = hashMapOf<String, Any>(
            "uri" to uri,
            "topic" to topic,
            "title" to title,
            "description" to description,
            "date" to date,
            "uriViedo" to uriViedo,
            "hidden" to false
        )
        db.collection("advice").whereEqualTo("title", oldTitle).get().addOnSuccessListener {
            for (doc in it) {
                val id = doc.id
                db.collection("advice").document(id).update(data).addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "تم التعديل بنجاح",
                        Toast.LENGTH_LONG
                    ).show()
                    val i = Intent(this, Home::class.java)
                    startActivity(i)
                    finish()
                }
            }

        }
    }


    fun getCurrentDate(): Date {
        return Date()
    }

    fun getTopic() {
        db = Firebase.firestore
        db.collection("Topic").get().addOnSuccessListener {
            for (doc in it) {
                val name = doc.get("name").toString()
                topics.add(name)
            }
        }
    }
}