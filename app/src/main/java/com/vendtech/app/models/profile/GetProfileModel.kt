package com.vendtech.app.models.profile

class GetProfileModel (var status:String,var message:String,var result:ResultProfile)


data class ResultProfile(var email:String,var name:String,var surName:String,var profilePic:String,var userId:String,var dob:String,
                         var city:String,var country:String,var phone:String,var userName:String?,var address:String?,var accountStatus:String, var balance:String?, var vendor: String)