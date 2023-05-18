package com.iug.palliativemedicine.auth

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

class UserDataPreferences(activity: Activity) {
    val sheard = activity.getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)

    var name: String = sheard.getString("name", "").toString()
    var email = sheard.getString("email", "").toString()
    var typeAccount = sheard.getString("typeAccount", "").toString()
    var title = sheard.getString("title", "").toString()
    var phone = sheard.getString("phone", "").toString()

}