package com.vendtech.app.models.authentications

class SignInResponse(var status: String, var message: String, var result: ResultSignIn)

data class ResultSignIn(var userId: String, var firstName: String, var lastName: String, var email: String, var phone: String, var token: String, var userType: String, var accountStatus: String, var posNumber: String, var minVend: String, var percentage: String, var vendor: String)