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
import com.iug.palliativemedicine.model.Notification
import com.iug.palliativemedicine.topic.Advice
import com.iug.palliativemedicine.topic.UpdateAdvice
import com.squareup.picasso.Picasso

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.MyViewHolder>, Filterable {


    var activity: Activity
    var data: ArrayList<Notification>
    private var filterList: ArrayList<Notification>
    lateinit var db: FirebaseFirestore

    constructor(activity: Activity, data: ArrayList<Notification>) : super() {
        this.activity = activity
        this.data = data
        this.filterList = data
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.item_title_Notification)
        val time = itemView.findViewById<TextView>(R.id.item_time_Notification)
        val body = itemView.findViewById<TextView>(R.id.item_body_Notification)
        val root = itemView.rootView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val root = LayoutInflater.from(activity).inflate(R.layout.item_notification, parent, false)
        //val root = LayoutInflater.from(activity).inflate(R.layout.activity_item_view_book_favorite, parent, false)
        return MyViewHolder(root)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val title = data[position].title
        val date = data[position].time
        val body = data[position].body

        holder.time.text = date
        holder.title.text = title
        holder.body.text = body




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
                    val itemModel = ArrayList<Notification>()
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
                    filterResults!!.values as ArrayList<Notification> /* = java.util.ArrayList<com.example.vicky.contactreader.ContactDTO> */
                notifyDataSetChanged()
            }

        }
    }


}


