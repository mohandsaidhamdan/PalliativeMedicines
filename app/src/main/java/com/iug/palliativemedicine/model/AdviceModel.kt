package com.iug.palliativemedicine.model

import java.util.Date

data class AdviceModel(
    var topicName: String = "",
    var uri: String = "",
    var title: String = "",
    var uriViedo: String = "",
    var description: String = "",
    var date: Date = Date(),
    var hidden: Boolean = false,
    var topicTag: String = "",
    var topicId: String = ""
)
