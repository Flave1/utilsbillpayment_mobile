package com.vendtech.app.models.profile

class UpdateProfileModel (var status:String,var message:String,var result:ResultUser)

data class ResultUser(var user:ResultUpdateProfile)

data class ResultUpdateProfile(var email:String,var userName:String,var name:String,var address:String,var surName:String,var profilePic:String,var userId:String,var city:String,var country:String,var countryCode:String,var phone:String)