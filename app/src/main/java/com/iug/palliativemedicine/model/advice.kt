package com.iug.palliativemedicine.model

import java.util.Date

data class advice (var topic  : String = "", var uri : String = "", var title : String = "",var uriViedo : String = "",
              var description : String = "" , var date : Date = Date() , var hidden : Boolean = false)
