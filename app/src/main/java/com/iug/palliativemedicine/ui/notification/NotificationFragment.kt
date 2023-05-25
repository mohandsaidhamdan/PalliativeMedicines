package com.iug.palliativemedicine.ui.notification

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.adapter.AdapterAdvice
import com.iug.palliativemedicine.adapter.NotificationAdapter
import com.iug.palliativemedicine.databinding.FragmentNotificationsBinding
import com.iug.palliativemedicine.model.Notification
import com.iug.palliativemedicine.model.Topic

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var url: String
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
     lateinit var ArrayList : ArrayList<Notification>
     lateinit var ArrayListTopic : ArrayList<Notification>
    lateinit var myAdaoter: NotificationAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        recyclerView = binding.RecyclerViewNotification
        db= Firebase.firestore
        ArrayList = ArrayList()
        ArrayListTopic = ArrayList()

        val email = requireActivity().getSharedPreferences("user" , MODE_PRIVATE).getString("email","").toString()

        db.collection("GeneralNotifications").get().addOnSuccessListener {
            for (doc in it){
                val title = doc.get("title").toString();
                val body = doc.get("body").toString();
                val time = doc.get("time").toString();
                ArrayList.add(Notification(title,body,time))
                myAdaoter.notifyDataSetChanged()
            }
        }
        db.collection("users").document(email).collection("Favorite").get().addOnSuccessListener {
           // ArrayListTopic.clear()
            for (doc in it){
                val topicId = doc.get("topicId").toString()
                db.collection("Topic").document(topicId).collection("TopicNotifications").get().addOnSuccessListener{
                    for (doc in it){
                        val title = doc.get("title").toString();
                        val body = doc.get("body").toString();
                        val time = doc.get("time").toString();
                        ArrayList.add(Notification(title,body,time))
                        myAdaoter.notifyDataSetChanged()
                    }

                }

            }

        }
        binding.btnSendNotification.setOnClickListener {
            val i = Intent(context , SendNotification::class.java)
            startActivity(i)
        }
//        ArrayList.addAll(ArrayListTopic)

        recyclerView.layoutManager = LinearLayoutManager(context)
        myAdaoter = NotificationAdapter(requireActivity(), ArrayList)
        recyclerView.adapter = myAdaoter

        return root
    }

}