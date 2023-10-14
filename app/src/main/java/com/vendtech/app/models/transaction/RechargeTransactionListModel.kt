package com.vendtech.app.models.transaction

class RechargeTransactionModel (var status:String, var message:String,var result:MutableList<RechargeTransactionResult>)

data class RechargeTransactionResult(var meterNumber:String,var createdAt:String,var transactionId:String,var status:String,var rechargeId:String,var amount:String)
