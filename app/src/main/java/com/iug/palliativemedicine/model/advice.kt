package com.iug.palliativemedicine.model

import java.util.Date

class advice (var topic  : String = "", var uri : String = "", var title : String = "",
              var description : String = "" , var date : Date = Date() , var hidden : Boolean = false)
