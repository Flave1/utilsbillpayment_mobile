package com.vendtech.app.models.transaction

class DepositTransactionDetails (var status:String,var message:String,var result:DepositTransactionResults)

data class DepositTransactionResults(var userName:String,var amount:String, var chkNoOrSlipId:String,var comments:String?,var createdAt:String,var depositId:String,var transactionId:String,var status:String="",var type:String,var vendorName:String)