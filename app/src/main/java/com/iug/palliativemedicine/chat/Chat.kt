package com.iug.palliativemedicine.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.protobuf.Value
import com.iug.palliativemedicine.Home
import com.iug.palliativemedicine.adapter.ChatAdapter
import com.iug.palliativemedicine.databinding.ActivityChatBinding
import com.iug.palliativemedicine.model.ChatMessage
import java.util.Date
import com.iug.palliativemedicine.R
import com.iug.palliativemedicine.model.Status

class Chat : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    val storage = FirebaseStorage.getInstance()
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    lateinit var dataBase: FirebaseDatabase
    var chatList = ArrayList<ChatMessage>()
    var topic = ""
    var status = ""
    lateinit var chatAdapter: ChatAdapter

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataBase = FirebaseDatabase.getInstance()
        recyclerView = binding.recyclerViewChat
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)


        val sender = getSharedPreferences("user", MODE_PRIVATE).getString("email", "").toString()
        val receiver = intent.getStringExtra("email").toString()




        binding.textName.text = intent.getStringExtra("name").toString()

        readMessage(sender, receiver)

        readStatue(sender, receiver)


        var check = false


        binding.buttonSend.setOnClickListener {
            if (check) {
                // Image resource is set to "send" drawable
                val messageEditTExt = binding.editTextMessage.text.toString()
                binding.editTextMessage.setText("")
                sendMessage(sender, receiver, messageEditTExt)
                binding.buttonSend.setImageResource(R.drawable.send_offline)
                val lastItemPosition = chatAdapter.itemCount - 1
                recyclerView.scrollToPosition(lastItemPosition)
            }
        }

        binding.editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.buttonSend.setImageResource(R.drawable.ic_send)
                status = "يكتب"
                writeStatus(sender, receiver, status)
                readStatue(sender, receiver)
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.isNullOrEmpty()) {
                    status = "متصل"
                    binding.buttonSend.setImageResource(R.drawable.send_offline)
                    writeStatus(sender, receiver, status)
                    readStatue(sender, sender)
                    check = false
                } else {
                    check = true
                }
            }

        })


    }


    override fun onBackPressed() {
        startActivity(Intent(this, ListUserChat::class.java))
        finish()
    }

    override fun onPause() {
        super.onPause()
        val sender = getSharedPreferences("user", MODE_PRIVATE).getString("email", "").toString()
        val receiver = intent.getStringExtra("email").toString()

        writeStatus(sender, receiver, "غير متصل")
    }

    fun sendMessage(sender: String, receiver: String, message: String) {
        val chatMessage = ChatMessage(message, sender, receiver, Date().time)
        dataBase.reference.child("chats").push().setValue(chatMessage)
    }

    fun readMessage(senderId: String, receiverId: String) {
        FirebaseDatabase.getInstance().getReference("chats")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    chatList.clear()
                    for (dataSnapShot: DataSnapshot in snapshot.children) {
                        val chat = dataSnapShot.getValue(ChatMessage::class.java)

                        if (chat!!.senderId.equals(senderId) && chat.reciverId.equals(receiverId) ||
                            chat.senderId.equals(receiverId) && chat.reciverId.equals(senderId)
                        ) {
                            chatList.add(chat)
                        }
                    }

                    chatAdapter = ChatAdapter(this@Chat, chatList)

                    recyclerView.adapter = chatAdapter

                    val lastItemPosition = chatAdapter.itemCount - 1
                    recyclerView.scrollToPosition(lastItemPosition)
                    readStatue(senderId, receiverId)
                }
            })

    }

    fun writeStatus(sender: String, receiver: String, status: String) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("status")

        val data = Status(sender, receiver, status)
        val senderReplacedString = sender.replace(Regex("[.#\\[\\]\$]"), "_")
        val receiverReplacedString = receiver.replace(Regex("[.#\\[\\]\$]"), "_")
        val childReference = reference.child(senderReplacedString).child(receiverReplacedString)
        childReference.setValue(data)
    }

    fun readStatue(sender: String, receiver: String) {

        val reference = FirebaseDatabase.getInstance().getReference("status")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Process the retrieved data here
                binding.textStatue.text = ""
                var status = ""
                for (dataSnapShot in snapshot.children) {
                    for (data in dataSnapShot.children) {
                        val value = data.getValue(Status::class.java)
                        // Use the retrieved data as needed
                        if (value!!.sender.equals(receiver) && value.receiver.equals(sender))
                            status = value.status

                    }
                }

                binding.textStatue.setText(status)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val sender = getSharedPreferences("user", MODE_PRIVATE).getString("email", "").toString()
        val receiver = intent.getStringExtra("email").toString()
        writeStatus(sender, receiver, "متصل")
    }
}

