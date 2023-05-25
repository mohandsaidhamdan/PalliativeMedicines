package com.iug.palliativemedicine.ui.subscription

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.R
import com.iug.palliativemedicine.databinding.ActivityFavoriteBinding
import com.iug.palliativemedicine.model.Topic
import com.squareup.picasso.Picasso


class SubscriptionFragment : Fragment() {
    private lateinit var binding: ActivityFavoriteBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    var Adapter: FirestoreRecyclerAdapter<Topic, topicItem>? = null
    private lateinit var analytics: FirebaseAnalytics
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        val root: View = binding.root

        analytics = Firebase.analytics
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "SubscriptionFragment")
        }

        val sheard =  requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val typeAcount = sheard.getString("typeAccount", "").toString()

//        if (typeAcount == "doctor") {
//            startActivity(Intent( requireActivity(), Home::class.java))
//            requireActivity().finish()
//        }
        binding.save.setOnClickListener {
            requireContext().getSharedPreferences("SelectionFavorite", AppCompatActivity.MODE_PRIVATE).edit().putBoolean("che", true)
                .apply()
            startActivity(Intent( requireActivity(), Home::class.java))
            requireActivity().finish()
        }

        recyclerView = binding.recyclerViewTopic


        db = Firebase.firestore

        val query = db.collection("Topic")

        val option =
            FirestoreRecyclerOptions.Builder<Topic>().setQuery(query, Topic::class.java).build()
        apabter(option)


//        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
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

        return root
    }

    class topicItem(view: View) : RecyclerView.ViewHolder(view) {
        val Itemname = itemView.findViewById<TextView>(R.id.titleTopic)
        val ItemImage = itemView.findViewById<ImageView>(R.id.imageTopic)
        var check = itemView.findViewById<CheckBox>(R.id.check)
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
                var view = LayoutInflater.from(context)
                    .inflate(R.layout.item_selection_topic, parent, false)
                return topicItem(view)
            }

            override fun onBindViewHolder(holder: topicItem, position: Int, model: Topic) {
                val name = model.topicName
                val Image = model.uri


                holder.Itemname.text = name
                DwnloadImage(model.uri, holder.ItemImage)

                // check box
                // check box
                holder.check.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // Checkbox is checked, perform desired actions
                        val db = Firebase.firestore
                        val email = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE).getString("email", "email").toString()

                        /// Add the user document to the "users" collection
                        val IdnewDocRef = db.collection("users").document(email).collection("Favorite").document().id

                        val newFavorite = hashMapOf(
                            "topicId" to model.topicId,
                            "topicName" to model.topicName,
                            "topicTag" to IdnewDocRef
                        )

                        db.collection("users").document(email).collection("Favorite").document(IdnewDocRef).set(newFavorite)


                        FirebaseMessaging.getInstance().subscribeToTopic(IdnewDocRef)
                            .addOnCompleteListener { task ->
                                var msg = "Subscribed"
                                if (!task.isSuccessful) {
                                    Log.d(
                                        ContentValues.TAG,
                                        task.exception?.message.toString()
                                    )
                                    msg = "Subscribe failed"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                    }
                    else {
                        // Checkbox is checked, perform desired actions
                        val db = FirebaseFirestore.getInstance()

                        val email =
                            requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE).getString("email", "email")
                                .toString()
                        db.collection("users").document(email).collection("Favorite").whereEqualTo("topicId" , model.topicId).get().addOnSuccessListener {
                            for (doc in it) {
                                val id = doc.id
                                val tag = doc.get("topicTag").toString()
                                FirebaseMessaging.getInstance().unsubscribeFromTopic(tag)
                                    .addOnCompleteListener { task ->
                                        var msg = "Unsubscribed"
                                        if (!task.isSuccessful) {
                                        Log.d(
                                            ContentValues.TAG,
                                            task.exception?.message.toString()
                                        )
                                        msg = "Unsubscribe failed"        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                db.collection("users").document(email).collection("Favorite").document(id).delete()
                            }
                        }


                    }

                }



                        }

                    }

            }

        }
