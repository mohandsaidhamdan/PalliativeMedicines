package com.iug.palliativemedicine.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.R
import com.iug.palliativemedicine.chat.Chat
import com.iug.palliativemedicine.databinding.ActivityListUserChatBinding
import com.iug.palliativemedicine.model.UserMessage
import com.squareup.picasso.Picasso


class chatFragment : Fragment() {
 lateinit var binding : ActivityListUserChatBinding

    val storage = FirebaseStorage.getInstance()
    lateinit var url: String
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    var Adapter: FirestoreRecyclerAdapter<UserMessage, ChatItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = ActivityListUserChatBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerViewTopic
        listView()



//        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = LinearLayoutManager(context)
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
    class ChatItem(view: View) : RecyclerView.ViewHolder(view) {
        val Itemname = itemView.findViewById<TextView>(R.id.itemNamechat)
        val ItemImage = itemView.findViewById<ImageView>(R.id.itemImagechat)
        val itemOnline = itemView.findViewById<TextView>(R.id.itemOnline)
        val itemDate = itemView.findViewById<TextView>(R.id.itemDate)
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


    @SuppressLint("NotifyDataSetChanged")
    fun searchList(text: String) {
        val query = db.collection("users").whereEqualTo("typeAcount", "doctor").orderBy("name")
            .startAt(text).endAt(text + "\ufaff")
        val option =
            FirestoreRecyclerOptions.Builder<UserMessage>()
                .setQuery(query, UserMessage::class.java).build()
        apabter(option)
        Adapter!!.startListening()
        recyclerView.adapter = Adapter
        Adapter!!.notifyDataSetChanged()
    }

    fun apabter(option: FirestoreRecyclerOptions<UserMessage>) {
        Adapter = object : FirestoreRecyclerAdapter<UserMessage, ChatItem>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItem {
                var view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat, parent, false)
                return ChatItem(view)
            }

            override fun onBindViewHolder(holder: ChatItem, position: Int, model: UserMessage) {
                val name = model.name
                val Image = model.ImageUrl

                holder.root.setOnLongClickListener { _ ->

                    true
                }
                holder.Itemname.text = name
                if (Image.isNotEmpty()) {
                    DwnloadImage(model.ImageUrl, holder.ItemImage)
                }
//
                holder.root.setOnClickListener {
                    val i = Intent(context, Chat::class.java)
                    i.putExtra("email", model.email)
                    i.putExtra("name", model.name)
                    startActivity(i)

                }

            }

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

    fun listView(){
        val typeAcount = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE).getString("typeAccount", "").toString()
        val name = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE).getString("name", "").toString()
        db = Firebase.firestore

        if (typeAcount == "doctor") {
            val query = db.collection("users").whereNotEqualTo("name", name)
//            .orderBy("email")

            val option =
                FirestoreRecyclerOptions.Builder<UserMessage>().setQuery(query, UserMessage::class.java)
                    .build()
            apabter(option)
        }else{
            val query = db.collection("users").whereEqualTo("typeAcount", "doctor")
//            .orderBy("email")

            val option =
                FirestoreRecyclerOptions.Builder<UserMessage>().setQuery(query, UserMessage::class.java)
                    .build()
            apabter(option)
        }
    }

    override fun onResume() {
        super.onResume()
        listView()
    }
}