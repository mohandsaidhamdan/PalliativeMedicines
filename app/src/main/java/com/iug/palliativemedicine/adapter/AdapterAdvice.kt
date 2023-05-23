package com.iug.palliativemedicine.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.R
import com.iug.palliativemedicine.model.advice
import com.iug.palliativemedicine.topic.Advice
import com.iug.palliativemedicine.topic.UpdateAdvice
import com.squareup.picasso.Picasso
import java.security.cert.Extension

class AdapterAdvice : RecyclerView.Adapter<AdapterAdvice.MyViewHolder>, Filterable {


    var activity: Activity
    var data: ArrayList<advice>
    private var filterList: ArrayList<advice>
    lateinit var db: FirebaseFirestore

    constructor(activity: Activity, data: ArrayList<advice>) : super() {
        this.activity = activity
        this.data = data
        this.filterList = data
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ItemTitle = itemView.findViewById<TextView>(R.id.itemTitlediv)
        val itemImagediv = itemView.findViewById<ImageView>(R.id.itemImagediv)
        val itemDatediv = itemView.findViewById<TextView>(R.id.itemDatediv)
        val itemTopicdiv = itemView.findViewById<TextView>(R.id.itemTopicdiv)
        val btnHidden = itemView.findViewById<CardView>(R.id.btnHidden)
        val imageHidden = itemView.findViewById<ImageView>(R.id.imageHidden)
        val btnUpdate = itemView.findViewById<CardView>(R.id.btnUpdateAdvice)
        val root = itemView.rootView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val root = LayoutInflater.from(activity).inflate(R.layout.item_advice, parent, false)
        //val root = LayoutInflater.from(activity).inflate(R.layout.activity_item_view_book_favorite, parent, false)
        return MyViewHolder(root)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val title = data[position].title
        val date = data[position].date

        holder.root.setOnLongClickListener { _ ->

            val dilalog = AlertDialog.Builder(activity)
            dilalog.setTitle("delete advice")
            dilalog.setMessage("Are you sure to delete the advice??")
            dilalog.setPositiveButton("delete") { dialog, which ->
                notifyDataSetChanged()
                // Do something when the positive button is clicked
                Toast.makeText(activity, "delete Successful", Toast.LENGTH_SHORT).show()
                getidDelete(data[position].title.toString())

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
        holder.itemTopicdiv.text = data[position].topic

        if (data[position].hidden) {
            holder.imageHidden.setImageResource(R.drawable.hidden)
        } else {
            holder.imageHidden.setImageResource(R.drawable.show)
        }
        holder.btnHidden.setOnClickListener {
            if (data[position].hidden) {
                getidUpdate(data[position].title.toString(), data[position].date.toString(), false)

            } else {
                getidUpdate(data[position].title.toString(), data[position].date.toString(), true)
            }


        }

        val sheard =
            activity.getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val typeAcount = sheard.getString("typeAccount", "").toString()

        if (typeAcount == "doctor") {
            holder.btnHidden.visibility = View.VISIBLE
            holder.btnUpdate.visibility = View.VISIBLE
        } else {
            holder.btnHidden.visibility = View.GONE
            holder.btnUpdate.visibility = View.GONE
        }
        DwnloadImage(data[position].uri, holder.itemImagediv)
        holder.btnUpdate.setOnClickListener {
            val i = Intent(activity, UpdateAdvice::class.java)
            i.putExtra("title", data[position].title)
            i.putExtra("description", data[position].description)
            i.putExtra("uri", data[position].uri)
            i.putExtra("topic", data[position].topic)
            i.putExtra("uriViedo", data[position].uriViedo)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            activity.startActivity(i)
        }
        holder.root.setOnClickListener {
            val i = Intent(activity, Advice::class.java)
            val sheard = activity.getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
            val email = sheard.getString("email", "").toString()
            i.putExtra("title", data[position].title)
            i.putExtra("description", data[position].description)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            activity.startActivity(i)
        }
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (p0 == null || p0.length < 0) {
                    filterResults.count = filterList.size
                    filterResults.values = filterList
                } else {
                    var searchchar = p0.toString().toLowerCase()
                    val itemModel = ArrayList<advice>()
                    for (item in filterList) {
                        if (item.title.contains(searchchar)) {
                            itemModel.add(item)
                        }
                    }
                    filterResults.count = itemModel.size
                    filterResults.values = itemModel
                }

                return filterResults

            }

            override fun publishResults(p0: CharSequence?, filterResults: FilterResults?) {
                data =
                    filterResults!!.values as ArrayList<advice> /* = java.util.ArrayList<com.example.vicky.contactreader.ContactDTO> */
                notifyDataSetChanged()
            }

        }
    }

    // get id item Delete
    fun getidDelete(titlediv: String) {
        val db = Firebase.firestore
        db.collection("advice")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val title = document.get("title").toString()
                    if (title == titlediv) {
                        val id = document.id
                        db.collection("advice").document(id).delete()

                        val intent = Intent(activity, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        activity.startActivity(intent)
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    // get Image in firebase and set Image View
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


    // get id item  update hidden or show
    fun getidUpdate(titlediv: String, datediv: String, hidden: Boolean) {
        val db = Firebase.firestore
        db.collection("advice")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val title = document.get("title").toString()
                    val date = document.get("date").toString()
                    if (title == titlediv) {
                        val id = document.id
                        db.collection("advice").document(id).update("hidden", hidden)

                        val intent = Intent(activity, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        activity.startActivity(intent)
//                        activity.finish()
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

}


