package com.iug.palliativemedicine.topic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.R
import com.iug.palliativemedicine.databinding.ActivityDateilsBinding
import com.iug.palliativemedicine.databinding.ActivityHomeBinding
import com.squareup.picasso.Picasso

class Advice : AppCompatActivity() {
    private lateinit var binding: ActivityDateilsBinding
    lateinit var player : SimpleExoPlayer
    lateinit var playerView : PlayerView
    val storage = FirebaseStorage.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

       val title =  intent.getStringExtra("title").toString()
        playerView = binding.playerView


        val db = Firebase.firestore
        db.collection("advice").whereEqualTo("title" , title)
            .get()
            .addOnSuccessListener {
                for (doc in it){
                    val des = doc.getString("description")
                    val topic = doc.getString("topic")
                    val title = doc.getString("title")
                    val uri = doc.getString("uri")
                    val Viedo = doc.getString("uriViedo")
                    val date = doc.get("date")

                    binding.topic.text = topic.toString()
                    binding.dec.text = des.toString()
                    binding.title.text = title.toString()
//                    binding.date.text = date.toString()

                    DwnloadImage(uri.toString(),binding.imgeView)
                    if(Viedo.toString().isNotEmpty())
                    DwnloadVideo(Viedo.toString())
                    else
                     playerView.visibility = View.GONE
                }
            }

    }


    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this@Advice , Home::class.java)
        startActivity(i)
        finish()
    }
    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }




    private fun DwnloadVideo(uri: String  ) {
        val storageRef =
            storage.reference.child(uri) // Replace "images/image.jpg" with your actual image path in Firebase Storage

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val ViedoUrl = uri.toString()
            player = SimpleExoPlayer.Builder(this).build()
            playerView.player = player
            val mediaItem = MediaItem.fromUri(ViedoUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            // Use the imageUrl as needed (e.g., display the image, store it in a database, etc.)
        }.addOnFailureListener { exception ->
            // Handle any errors that occurred while retrieving the download URL
        }
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