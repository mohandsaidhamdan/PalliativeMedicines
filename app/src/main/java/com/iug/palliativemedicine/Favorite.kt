package com.iug.palliativemedicine

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.databinding.ActivityFavoriteBinding
import com.iug.palliativemedicine.model.topic
import com.squareup.picasso.Picasso

class Favorite : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView

    var Adapter: FirestoreRecyclerAdapter<topic, topicItem>? = null

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
            FirestoreRecyclerOptions.Builder<topic>().setQuery(query, topic::class.java).build()
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
            FirestoreRecyclerOptions.Builder<topic>().setQuery(query, topic::class.java).build()
        apabter(option)
        Adapter!!.startListening()
        recyclerView.adapter = Adapter
        Adapter!!.notifyDataSetChanged()
    }

    fun apabter(option: FirestoreRecyclerOptions<topic>) {
        Adapter = object : FirestoreRecyclerAdapter<topic, topicItem>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): topicItem {
                var view = LayoutInflater.from(this@Favorite)
                    .inflate(R.layout.item_selection_topic, parent, false)
                return topicItem(view)
            }

            override fun onBindViewHolder(holder: topicItem, position: Int, model: topic) {
                val name = model.name
                val Image = model.uri


                holder.Itemname.text = name
                DwnloadImage(model.uri, holder.ItemImage)

                 // check box
                holder.check.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // Checkbox is checked, perform desired actions
                        val db = FirebaseFirestore.getInstance()
                        val usersCollection = db.collection("Favorite")
                        val email = getSharedPreferences("user", MODE_PRIVATE).getString("email", "email").toString()

                        // Create a new user document
                        val newUser = hashMapOf("favored" to "ListFavored")
                        // Add the user document to the "users" collection
                        usersCollection.document(email).set(newUser).addOnSuccessListener { documentReference ->
                            val id = email
                            val newFavorite = hashMapOf(
                                "topic" to model.name
                            )
                            // Create a new favored document within the "posts" subcollection
                                usersCollection.document(id).collection("favored").add(newFavorite)


                        }

                    }
                    else {
                        // Checkbox is checked, perform desired actions
                        val db = FirebaseFirestore.getInstance()

                        val email =
                            getSharedPreferences("user", MODE_PRIVATE).getString("email", "email")
                                .toString()


                        // get the id user document to the "Favorite" collection
                        db.collection("Favorite").get()
                            .addOnSuccessListener { documentReference ->
                                for (doc in documentReference) {
                                    val id = doc.id
                                    if(id.toString() == email){
                                        val postsSubcollection = db.collection("Favorite").document(id).collection("favored").
                                                whereEqualTo("topic" , model.name)
                                        postsSubcollection.get()
                                            .addOnSuccessListener { querySnapshot ->
                                                val batch = db.batch()
                                                //delete in favorite
                                                for (document in querySnapshot) {
                                                    batch.delete(document.reference)
                                                }

                                                batch.commit()

                                            }
                                    }




                                }

                            }
                    }

                }

            }

        }

    }
}