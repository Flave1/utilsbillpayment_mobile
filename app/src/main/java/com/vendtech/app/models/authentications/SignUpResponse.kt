package com.vendtech.app.models.authentications

class SignUpResponse (var status:String, var message:String,var result:ResultSignUp)

data class ResultSignUp (var userId:String,var accountStatus:String)