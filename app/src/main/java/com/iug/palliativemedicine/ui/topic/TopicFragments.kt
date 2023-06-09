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
import com.iug.palliativemedicine.databinding.FragmentAdviceBinding
import com.iug.palliativemedicine.topic.TopicDetailsAdvice
import com.iug.palliativemedicine.model.Topic
import com.iug.palliativemedicine.topic.AddTopic
import com.iug.palliativemedicine.topic.UpdateTopic
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
    private lateinit var filterList: ArrayList<Topic>
    var Adapter: FirestoreRecyclerAdapter<Topic, topicItem>? = null
    lateinit var binding: FragmentAdviceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdviceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        db = Firebase.firestore
        recyclerView = binding.recyclerViewTopic
        listview()

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
        val Itemname = itemView.findViewById<TextView>(R.id.itemName)
        val ItemImage = itemView.findViewById<ImageView>(R.id.itemImage)
        val btn_update = itemView.findViewById<ImageView>(R.id.btn_update)
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

                    val name = document.get("topicName")
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
        val i = Intent(context, TopicDetailsAdvice::class.java)
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
                    .inflate(R.layout.item_topic, parent, false)
                return topicItem(view)
            }

            override fun onBindViewHolder(holder: topicItem, position: Int, model: Topic) {
                val name = model.topicName

                holder.root.setOnLongClickListener { _ ->

                    val dilalog = AlertDialog.Builder(context)
                    dilalog.setTitle("delete Topic")
                    dilalog.setMessage("Are you sure to delete the Topic??")
                    dilalog.setPositiveButton("delete") { dialog, which ->
                        // Do something when the positive button is clicked
                        Toast.makeText(context, "تم الحذف بنجاح", Toast.LENGTH_SHORT).show()
                        getidDelete(model.topicName.toString())
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
                val sheard =
                    activity!!.getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
                val typeAcount = sheard.getString("typeAccount", "").toString()
//
                if (typeAcount == "doctor") {
                    holder.btn_update.visibility = View.VISIBLE
                } else {
                    holder.btn_update.visibility = View.GONE
                }
                holder.root.setOnClickListener {
                    getIntent(name)
                }
                holder.btn_update.setOnClickListener {
                    val i = Intent(requireContext(), UpdateTopic::class.java)
                    i.putExtra("imageUri", model.uri)
                    i.putExtra("title", model.topicName)
                    startActivity(i)

                }

            }


        }

    }

    fun listview() {


        val sheard = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val typeAcount = sheard.getString("typeAccount", "").toString()

        if (typeAcount == "doctor") {
            binding.fab.visibility = View.VISIBLE
        }
        binding.fab.setOnClickListener {
            val i = Intent(context, AddTopic::class.java)
            startActivity(i)

        }



        val query = db.collection("Topic")

        val option =
            FirestoreRecyclerOptions.Builder<Topic>().setQuery(query, Topic::class.java).build()
        apabter(option)
Adapter!!.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
//        listview()

    }


}