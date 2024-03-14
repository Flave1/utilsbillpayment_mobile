package com.vendtech.app.models.profile

class GetWalletModel (var status:String,var message:String,var result:ResultWallet)

data class ResultWallet(var balance:String,var unReadNotifications:String,var accountStatus:String, var stringBalance:String, var isDepositPending: Boolean, var pendingDepositBalance: String)

class GetPendingModel (var status:String,var message:String,var result:PendingBalance)

data class PendingBalance(var isDepositPending: Boolean, var pendingDepositBalance: String)