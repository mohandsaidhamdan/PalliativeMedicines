package com.iug.palliativemedicine.model


 class ChatMessage{
    var messageId : String? = null
    var message : String? = null
    var senderId : String? = null
    var reciverId : String? = null
    var ImageId : String? = null
    var timeStamp : Long = 0

 constructor()
 constructor(
     message : String?,
     senderId : String?,
     reciverId : String?,
     timeStamp : Long
 )
 {
     this.message =message
     this.senderId =senderId
     this.reciverId =reciverId
     this.timeStamp =timeStamp
 }
 }
