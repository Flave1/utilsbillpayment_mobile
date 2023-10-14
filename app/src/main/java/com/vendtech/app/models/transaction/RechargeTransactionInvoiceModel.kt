package com.vendtech.app.models.transaction

class RechargeTransactionInvoiceModel (var status:String,var message:String,var result: RechargeTransactionInvoiceResult)

data class RechargeTransactionInvoiceResult(var path:String)