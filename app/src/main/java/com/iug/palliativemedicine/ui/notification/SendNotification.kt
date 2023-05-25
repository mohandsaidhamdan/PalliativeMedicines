package com.iug.palliativemedicine.ui.notification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.iug.palliativemedicine.databinding.ActivitySendNotificationBinding

class SendNotification : AppCompatActivity() {
    lateinit var binding : ActivitySendNotificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnSend.setOnClickListener {
            val data = hashMapOf<String , Any>(
                "title" to " titletitletitle"
                , "body" to binding.textNotification.text.toString(),
                "time" to Timestamp.now()
            )

            val db = Firebase.firestore
            db.collection("GeneralNotifications").add(data)

        }

    }
}