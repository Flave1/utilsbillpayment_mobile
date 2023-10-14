package com.vendtech.app.models.profile

class GetWalletModel (var status:String,var message:String,var result:ResultWallet)

data class ResultWallet(var balance:String,var unReadNotifications:String,var accountStatus:String, var stringBalance:String)