package com.vendtech.app.models.profile

import java.util.*

class NotificationListModel (var status:String,var message:String,var result:Result)

data class Result(var result1:MutableList<NotificationListResult>,var result2:MutableList<NotificationListResult>)
data class NotificationListResult(var message:String,var title:String,var type:String,var userName:String,var sentOn:String,var id:String,var rechargeId:String,var meterNumber:String,val productShortName:String,var rechargePin:String,var posId:String,var vendorName:String,var vendorId:String,var amount:String,var createdAt:String,var status:String,var transactionId:String,var meterRechargeId:String,var chkNoOrSlipId:String,var comments:String,var bank:String,var posNumber:String,var balance:String,var newBalance:String,var percentageAmount:String,var depositId:String,var payer:String,var issuingBank:String,var valueDate:String,var date:Date, var notType: String, var platformId: Number)