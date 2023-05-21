package com.iug.palliativemedicine.ui.topic


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.R
import com.iug.palliativemedicine.chat.ListUserChat
import com.iug.palliativemedicine.databinding.FragmentAdviceBinding
import com.iug.palliativemedicine.topic.detailDiseases
import com.iug.palliativemedicine.model.topic
import com.iug.palliativemedicine.topic.AddTopic
import com.squareup.picasso.Picasso
import java.util.*


class TopicFragments : Fragment() {
    // Initialize FirebaseStorage instance
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    private val IMAGE_PICK_REQUEST = 100
    lateinit var imageViewDialog: ImageView
    lateinit var url: String
    private lateinit var filterList: ArrayList<topic>
    var Adapter: FirestoreRecyclerAdapter<topic, topicItem>? = null
    lateinit var binding: FragmentAdviceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdviceBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val sheard = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val typeAcount = sheard.getString("typeAccount", "").toString()

        if (typeAcount == "doctor") {
            binding.fab.visibility = View.VISIBLE
        }

        recyclerView = binding.recyclerViewTopic




        binding.fab.setOnClickListener {
            val i = Intent(context , AddTopic::class.java)
            startActivity(i)

        }

        db = Firebase.firestore

        val query = db.collection("Topic")

        val option =
            FirestoreRecyclerOptions.Builder<topic>().setQuery(query, topic::class.java).build()
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



        binding.btnChat.setOnClickListener {
            val i = Intent(context, ListUserChat::class.java)
            startActivity(i)
        }



        return root
    }




    class topicItem(view: View) : RecyclerView.ViewHolder(view) {
        val Itemname = itemView.findViewById<TextView>(R.id.itemName)
        val ItemImage = itemView.findViewById<ImageView>(R.id.itemImage)
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


    fun getidDelete(nameTopic: String) {
        db.collection("Topic")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val name = document.get("name")
                    if (name == nameTopic) {
                        val id = document.id
                        delete(id)
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    fun delete(id: String) {
        db.collection("Topic").document(id).delete()

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

    fun getIntent(name: String) {
        val i = Intent(context, detailDiseases::class.java)
        i.putExtra("name", name)
        startActivity(i)
        requireActivity().finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            try {
                imageViewDialog.setImageURI(selectedImageUri)
                data.data?.let { uploadImage(it) }
            } catch (_: Exception) {

            }
            // Perform the image upload

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
                Toast.makeText(context, "failed upload image", Toast.LENGTH_SHORT).show()

            }
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
        Adapter = object : FirestoreRecyclerAdapter<topic, topicItem>(option)  {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): topicItem {
                var view = LayoutInflater.from(context)
                    .inflate(R.layout.item_topic, parent, false)
                return topicItem(view)
            }

            override fun onBindViewHolder(holder: topicItem, position: Int, model: topic) {
                val name = model.name
                val Image = model.uri

                holder.root.setOnLongClickListener { _ ->

                    val dilalog = AlertDialog.Builder(context)
                    dilalog.setTitle("delete Topic")
                    dilalog.setMessage("Are you sure to delete the Topic??")
                    dilalog.setPositiveButton("delete") { dialog, which ->
                        // Do something when the positive button is clicked
                        Toast.makeText(context, "تم الحذف بنجاح", Toast.LENGTH_SHORT).show()
                        getidDelete(model.name.toString())
                        dialog.dismiss()

                    }
                    dilalog.setNegativeButton("cancel") { dialog, which ->
                        // Do something when the negative button is clicked
                        dialog.dismiss()

                    }
                    dilalog.show()

                    true
                }
                holder.Itemname.text = name
                DwnloadImage(model.uri, holder.ItemImage)

                holder.root.setOnClickListener {
                    getIntent(name)
                }


            }



        }

    }




}