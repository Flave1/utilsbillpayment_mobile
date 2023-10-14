package com.vendtech.app.models.meter

import java.io.Serializable

data class RechargeMeterModel (var status:String, var message:String,val result:Result):Serializable

data class Result(val receiptNo:String,val customerName:String,val accountNo:String,val address:String,val deviceNumber:String,val amount:String,val charges:String,val discount:String,val commission:String,val unitCost:String,val unit:String,val tarrif:String,val terminalID:String,val serialNo:String,val pos:String,val vendorId:String,val debitRecovery:String,val pin1:String,val pin2:String,val pin3:String,val tax:String,val transactionDate:String,val edsaSerial:String,val vtechSerial:String,val shouldShowSmsButton:String, val mobileShowSmsButton: Boolean, val mobileShowPrintButton: Boolean,val receiptStatus:ReceiptStatus):Serializable
data class ReceiptStatus(val status:String,val message:String):Serializable