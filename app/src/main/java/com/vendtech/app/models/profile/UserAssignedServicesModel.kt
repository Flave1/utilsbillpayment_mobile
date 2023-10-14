package com.vendtech.app.models.profile

class UserAssignedServicesModel (var status:String,var message:String,var result:MutableList<UserServicesResult>)

data class UserServicesResult(var platformId:String,var title:String, var disablePlatform: Boolean, var diabledPlaformMessage: String)