package com.iug.palliativemedicine

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.databinding.ActivityFavoriteBinding
import com.iug.palliativemedicine.model.Topic
import com.squareup.picasso.Picasso

class Favorite : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView

    var Adapter: FirestoreRecyclerAdapter<Topic, topicItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sheard = getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val typeAcount = sheard.getString("typeAccount", "").toString()

        if (typeAcount == "doctor") {
            startActivity(Intent(this, Home::class.java))
            finish()
        }
        binding.save.setOnClickListener {
            getSharedPreferences("SelectionFavorite", MODE_PRIVATE).edit().putBoolean("che", true)
                .apply()
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        recyclerView = binding.recyclerViewTopic


        db = Firebase.firestore

        val query = db.collection("Topic")

        val option =
            FirestoreRecyclerOptions.Builder<Topic>().setQuery(query, Topic::class.java).build()
        apabter(option)


//        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = Adapter

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {

                searchList(p0.toString())
                return true
            }

        })


    }


    class topicItem(view: View) : RecyclerView.ViewHolder(view) {
        val Itemname = itemView.findViewById<TextView>(R.id.titleTopic)
        val ItemImage = itemView.findViewById<ImageView>(R.id.imageTopic)
        val check = itemView.findViewById<CheckBox>(R.id.check)
        val root = itemView.rootView

    }

    override fun onStart() {
        super.onStart()
        Adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        Adapter!!.stopListening()
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


    @SuppressLint("NotifyDataSetChanged")
    fun searchList(text: String) {
        val query = db.collection("Topic").orderBy("name").startAt(text).endAt(text + "\ufaff")
        val option =
            FirestoreRecyclerOptions.Builder<Topic>().setQuery(query, Topic::class.java).build()
        apabter(option)
        Adapter!!.startListening()
        recyclerView.adapter = Adapter
        Adapter!!.notifyDataSetChanged()
    }

    fun apabter(option: FirestoreRecyclerOptions<Topic>) {
        Adapter = object : FirestoreRecyclerAdapter<Topic, topicItem>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): topicItem {
                var view = LayoutInflater.from(this@Favorite)
                    .inflate(R.layout.item_selection_topic, parent, false)
                return topicItem(view)
            }

            override fun onBindViewHolder(holder: topicItem, position: Int, model: Topic) {
                val name = model.topicName
                holder.Itemname.text = name
                DwnloadImage(model.uri, holder.ItemImage)

                 // check box
                holder.check.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // Checkbox is checked, perform desired actions
                        val db = Firebase.firestore
                        val email = getSharedPreferences("user", MODE_PRIVATE).getString("email", "email").toString()
                        val newFavorite = hashMapOf(
                            "topicId" to model.topicId,
                            "topicName" to model.topicName,
                            "topicTag" to model.topicTag
                        )
                        /// Add the user document to the "users" collection
                        db.collection("users").document(email).collection("Favorite").add(newFavorite)

                        FirebaseMessaging.getInstance().subscribeToTopic(model.topicTag)
                            .addOnCompleteListener { task ->
                                var msg = "Subscribed"
                                if (!task.isSuccessful) {
                                    Log.d(
                                        ContentValues.TAG,
                                        task.exception?.message.toString()
                                    )
                                    msg = "Subscribe failed"
                                }
                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            }
                    }
                    else {
                        // Checkbox is checked, perform desired actions
                        val db = FirebaseFirestore.getInstance()

                        val email =
                            getSharedPreferences("user", MODE_PRIVATE).getString("email", "email")
                                .toString()
                        db.collection("users").document(email).collection("Favorite").whereEqualTo("topicId" , model.topicId).get().addOnSuccessListener {
                            for (doc in it) {
                                val id = doc.id
                                db.collection("users").document(email).collection("Favorite").document(id).delete()
                            }
                        }
                    }

                }

            }

        }

    }
}