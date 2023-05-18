package com.iug.palliativemedicine.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.iug.palliativemedicine.model.advice
import com.iug.palliativemedicine.topic.AddAdvice
import com.iug.palliativemedicine.adapter.AdapterAdvice
import com.iug.palliativemedicine.auth.login
import com.iug.palliativemedicine.databinding.FragmentHomeBinding
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    val topic = ArrayList<String>()
    val data = ArrayList<advice>()
    lateinit var myAdaoter: AdapterAdvice

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        db = Firebase.firestore
        // get type Account
        val sheard = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        var typeAcount = sheard.getString("typeAccount", "").toString()

        // if type Account equalises doctor
        if (typeAcount == "doctor") {
            binding.fab.visibility = View.VISIBLE
        }


        // btn add Advice
        binding.fab.setOnClickListener {
            val i = Intent(context, AddAdvice::class.java)
            startActivity(i)
        }





        topic.add("News and articles")

        if (typeAcount == "doctor") {
            ListViewDector()
        } else {
            ListView()
        }


        // get recycler View
        recyclerView = binding.recyclerViewTopic
        recyclerView.layoutManager = LinearLayoutManager(context)
        myAdaoter = AdapterAdvice(requireActivity(), data)
        recyclerView.adapter = myAdaoter


        // btn search View
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                myAdaoter.filter.filter(p0)
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                myAdaoter.filter.filter(p0)
                return true
            }

        })
        binding.apply {

            // Sign out of the account
            btnSignout.setOnClickListener {
                startActivity(Intent(context, login::class.java))
                requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
                    .edit().putBoolean("che", false).apply()
                requireActivity().finish()
            }


        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    // fun Adapter firebase
    fun ListViewDector() {
        val sheard = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val email = sheard.getString("email", "").toString()

        // Continue with any additional logic using the "topic" list
//        db.collection("Favorite").document(email).collection("favored")
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                for (document in querySnapshot) {
//                    val topicItem = document.getString("topic")
//                    if (topicItem != null) {
                        db.collection("advice")
                            .get()
                            .addOnSuccessListener {
                                for (doc in it) {
                                    data.add(
                                        advice(
                                            doc.getString("topic").toString(),
                                            doc.getString("uri").toString(),
                                            doc.getString("title").toString(),
                                            doc.getString("description").toString(),
                                            doc.getTimestamp("date")!!.toDate(),
                                            doc.getBoolean("hidden")!!
                                        )


                                    )
                                }
                                myAdaoter.notifyDataSetChanged()
                                if (data.size == 0) {
                                    binding.textviewNonTopic.visibility = View.VISIBLE
                                }
                            }
//                    }
//                }
//            }

    }


    // fun Adapter firebase
    fun ListView() {
        val sheard = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        val email = sheard.getString("email", "").toString()

        // Continue with any additional logic using the "topic" list
        db.collection("Favorite").document(email).collection("favored")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val topicItem = document.getString("topic")
                    if (topicItem != null) {
                        db.collection("advice").whereEqualTo("topic", topicItem)
                            .whereEqualTo("hidden", false)
                            .get()
                            .addOnSuccessListener {
                                for (doc in it) {
                                    data.add(
                                        advice(
                                            doc.getString("topic").toString(),
                                            doc.getString("uri").toString(),
                                            doc.getString("title").toString(),
                                            doc.getString("description").toString(),
                                            doc.getTimestamp("date")!!.toDate(),
                                            doc.getBoolean("hidden")!!
                                        )


                                    )
                                }
                                myAdaoter.notifyDataSetChanged()
                                if (data.size == 0) {
                                    binding.textviewNonTopic.visibility = View.VISIBLE
                                }
                            }
                    }
                }
            }
    }
}

