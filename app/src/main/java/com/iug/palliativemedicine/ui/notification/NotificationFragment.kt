package com.iug.palliativemedicine.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.iug.palliativemedicine.databinding.FragmentNotificationsBinding

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var url: String
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }

}