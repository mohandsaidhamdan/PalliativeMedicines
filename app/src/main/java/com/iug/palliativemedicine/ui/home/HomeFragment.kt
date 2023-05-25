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
import com.denzcoskun.imageslider.constants.AnimationTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.model.AdviceModel
import com.iug.palliativemedicine.topic.AddAdvice
import com.iug.palliativemedicine.adapter.AdapterAdvice
import com.iug.palliativemedicine.ads.ads
import com.iug.palliativemedicine.auth.login
import com.iug.palliativemedicine.databinding.FragmentHomeBinding
import com.iug.palliativemedicine.model.Topic
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    val topic = ArrayList<String>()
    val data = ArrayList<AdviceModel>()
    lateinit var myAdaoter: AdapterAdvice
    val storage = FirebaseStorage.getInstance()

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
            binding.fabAds.visibility = View.VISIBLE
        }

        // btn add Advice
        binding.fab.setOnClickListener {
            val i = Intent(context, AddAdvice::class.java)
            startActivity(i)
        }

        binding.fabAds.setOnClickListener {
            startActivity(Intent(context, ads::class.java))
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

        sliderImage()
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
        db.collection("advice").get()
            .addOnSuccessListener {
                for (doc in it) {
                    data.add(
                        AdviceModel(
                            doc.getString("topicName").toString(),
                            doc.getString("uri").toString(),
                            doc.getString("title").toString(),
                            doc.getString("uriViedo").toString(),
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
                                        AdviceModel(
                                            doc.getString("topicName").toString(),
                                            doc.getString("uri").toString(),
                                            doc.getString("title").toString(),
                                            doc.getString("uriViedo").toString(),
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


    @SuppressLint("SuspiciousIndentation")
    fun sliderImage() {
        var title = ""
        val imageSlider = binding.imageSlider
        val imageList = ArrayList<SlideModel>()
        db.collection("ads").orderBy("date", Query.Direction.ASCENDING).limit(5).get()
            .addOnSuccessListener {
                for (doc in it) {
                    var imageUrl = doc.get("uri").toString()
                    title = doc.get("title").toString()

                    val storageRef =
                        storage.reference.child(imageUrl)
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        var Url = uri.toString()
                        imageList.add(SlideModel(Url, title))
                        imageSlider.setImageList(imageList)
                        imageSlider.setSlideAnimation(AnimationTypes.ROTATE_DOWN)
                    }
                }
            }


        imageSlider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                startActivity(Intent(context, ads::class.java))
            }

            override fun doubleClick(position: Int) {
                // Do not use onItemSelected if you are using a double click listener at the same time.
                // Its just added for specific cases.
                // Listen for clicks under 250 milliseconds.
            }
        })
    }

}

