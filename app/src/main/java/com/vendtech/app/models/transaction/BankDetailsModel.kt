package com.vendtech.app.models.transaction

class BankDetailsModel (var status:String,var message:String,var result:MutableList<BankDetailResult>)

data class BankDetailResult(var bankName:String,var accountName:String,var accountNumber:String,var bban:String)