package com.vendtech.app.models.transaction

import java.io.Serializable

data class RechargeTransactionDetails(var status:String,var message:String,var result:RechargeTransactionDetailResult):Serializable

data class RechargeTransactionDetailResult(var vendorName:String,var vendorId:Int,var meterNumber:String,var amount:String,var createdAt:String,var status:String,var transactionId:String,var posId:String,val rechargePin:String,val meterId:String, val platformId: Int):Serializable

