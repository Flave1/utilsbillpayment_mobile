package com.vendtech.app.models.airtime

import java.io.Serializable

data class AirtimeRechargeModel (var status:String?, var message:String?,var result:Result, val currency: String):Serializable

data class Result(
    val receiptNo:String?,
    val accountNo:String?,
    val amount:String?,
    val phone: String?,
    val pos:String?,
    val serialNo:String?,
    val transactionDate:String,
    val edsaSerial:String?,
    val vtechSerial:String?,
    val shouldShowSmsButton:Boolean,
    val mobileShowSmsButton: Boolean,
    val mobileShowPrintButton: Boolean,
    val receiptStatus: ReceiptStatus?,
    val receiptTitle: String?,
    val isNewRecharge: Boolean):Serializable
data class ReceiptStatus(val status:String,val message:String):Serializable