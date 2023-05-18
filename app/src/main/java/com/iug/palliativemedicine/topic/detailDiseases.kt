package com.iug.palliativemedicine.topic

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.R
import com.iug.palliativemedicine.databinding.ActivityDetailDiseasesBinding
import com.iug.palliativemedicine.model.advice
import com.squareup.picasso.Picasso

class detailDiseases : AppCompatActivity() {
    lateinit var binding : ActivityDetailDiseasesBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore

    lateinit var lottieAnimationView: LottieAnimationView
    lateinit var recyclerView: RecyclerView


    var Adapter: FirestoreRecyclerAdapter<advice, topicItem>? = null


    private val IMAGE_PICK_REQUEST = 100
    lateinit var imageViewDialogs: ImageView
    lateinit var topicEt: EditText
    lateinit var url: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailDiseasesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name").toString()

        val sheard = getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val typeAcount = sheard.getString("typeAccount", "").toString()
        lottieAnimationView = binding.lottie
        lottieAnimationView.animate().translationY((0).toFloat()).setDuration(4000).setStartDelay(1500).withEndAction {

        }

        if (typeAcount == "doctor") {
            binding.fab.visibility = View.VISIBLE
        }
        recyclerView = binding.recyclerViewTopic

        binding.fab.setOnClickListener {
            val i = Intent(this@detailDiseases, AddAdvice::class.java)
            startActivity(i)
        }

        db = Firebase.firestore
        val sheard2 = getSharedPreferences("user" , AppCompatActivity.MODE_PRIVATE)
        val typeAcount2 = sheard2.getString("typeAccount" , "").toString()
        var query = db.collection("advice").whereEqualTo("hidden", false)

        if (typeAcount2 == "doctor"){
            query = db.collection("advice").whereEqualTo("topic" , name)
        }else{
            query = db.collection("advice").whereEqualTo("hidden", false).whereEqualTo("topic" , name)
        }


        val option =
            FirestoreRecyclerOptions.Builder<advice>().setQuery(query, advice::class.java).build()
        apabter(option)
        recyclerView.layoutManager = GridLayoutManager(this@detailDiseases, 1)
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

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@detailDiseases , Home::class.java))
    }

    class topicItem(view: View) : RecyclerView.ViewHolder(view) {
        val ItemTitle = itemView.findViewById<TextView>(R.id.itemTitlediv)
        val itemImagediv = itemView.findViewById<ImageView>(R.id.itemImagediv)
        val itemDatediv = itemView.findViewById<TextView>(R.id.itemDatediv)
        val itemTopicdiv = itemView.findViewById<TextView>(R.id.itemTopicdiv)
        val btnHidden = itemView.findViewById<CardView>(R.id.btnHidden)
        val imageHidden = itemView.findViewById<ImageView>(R.id.imageHidden)
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


    fun getidDelete(titlediv: String) {
        db.collection("advice")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val title = document.get("title").toString()
                    if (title == titlediv ) {
                        val id = document.id
                        db.collection("advice").document(id).delete()
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
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


    fun getidUpdate(titlediv: String, datediv: String , hidden : Boolean) {
        db.collection("advice")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val title = document.get("title").toString()
                    val date = document.get("date").toString()
                    if (title == titlediv ) {
                        val id = document.id
                        db.collection("advice").document(id).update("hidden", hidden)
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun searchList(text: String) {
        val name = intent.getStringExtra("name").toString()
        val query = db.collection("advice")
            .whereEqualTo("hidden", false)
            .whereEqualTo("topic" , name)
            .orderBy("title")
            .startAt(text)
            .endAt(text + "\ufaff")
        val option =
            FirestoreRecyclerOptions.Builder<advice>().setQuery(query, advice::class.java).build()
        apabter(option)
        Adapter!!.startListening()
        recyclerView.adapter = Adapter
        Adapter!!.notifyDataSetChanged()
    }

    fun apabter(option: FirestoreRecyclerOptions<advice>) {
        Adapter = object : FirestoreRecyclerAdapter<advice, topicItem>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): topicItem {
                var view = LayoutInflater.from(this@detailDiseases)
                    .inflate(R.layout.itemadvice, parent, false)
                lottieAnimationView.visibility = View.GONE
                return topicItem(view)
            }

            override fun onBindViewHolder(holder: topicItem, position: Int, model: advice) {
                val title = model.title
                val date = model.date
                val Image = model.uri

                holder.root.setOnLongClickListener { _ ->

                    val dilalog = AlertDialog.Builder(this@detailDiseases)
                    dilalog.setTitle("delete advice")
                    dilalog.setMessage("Are you sure to delete the advice??")
                    dilalog.setPositiveButton("delete") { dialog, which ->
                        // Do something when the positive button is clicked
                        Toast.makeText(this@detailDiseases, "delete Successful", Toast.LENGTH_SHORT).show()
                        getidDelete(model.title.toString())
                        dialog.dismiss()

                    }
                    dilalog.setNegativeButton("cancel") { dialog, which ->
                        // Do something when the negative button is clicked
                        dialog.dismiss()

                    }
                    dilalog.show()

                    true
                }
                val dates = date.toString().substring(0, 13)
                holder.itemDatediv.text = dates
                holder.ItemTitle.text = title
                holder.itemTopicdiv.text = model.topic

                if (model.hidden){
                    holder.imageHidden.setImageResource(R.drawable.hidden)
                }else{
                    holder.imageHidden.setImageResource(R.drawable.show)
                }
                holder.btnHidden.setOnClickListener {
                    if (model.hidden){
                        getidUpdate(model.title.toString(), model.date.toString() , false)
                    }else{
                        getidUpdate(model.title.toString(), model.date.toString() , true)
                    }

                }

                val sheard =getSharedPreferences("user" , AppCompatActivity.MODE_PRIVATE)
                val typeAcount = sheard.getString("typeAccount" , "").toString()

                if (typeAcount == "doctor"){
                    holder.btnHidden.visibility = View.VISIBLE
                }else{
                    holder.btnHidden.visibility = View.GONE

                }
                DwnloadImage(model.uri, holder.itemImagediv)

                holder.root.setOnClickListener {
                    val i = Intent(this@detailDiseases, Advice::class.java)
                    i.putExtra("title" , model.title)
                    startActivity(i)
                }
            }

        }
    }
    fun dialog(){
        val dilalog = AlertDialog.Builder(this)
        dilalog.setTitle("delete advice")
        dilalog.setMessage("Are you sure to delete the advice??")
        dilalog.setPositiveButton("delete") { dialog, which ->
            // Do something when the positive button is clicked
            Toast.makeText(this, "delete Successful", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

        }
        dilalog.setNegativeButton("cancel") { dialog, which ->
            // Do something when the negative button is clicked
            dialog.dismiss()

        }
        dilalog.show()
    }

}