package com.vendtech.app.models.authentications


data class ResultDeleteProfile(var email:String)
class DeleteProfileModel (var status:String,var message:String,var result: ResultDeleteProfile)