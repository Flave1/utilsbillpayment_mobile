package com.vendtech.app.models.transaction

class DepositTransactionInvoiceModel (var status:String,var message:String,var result: DepositTransactionInvoiceResult)

data class DepositTransactionInvoiceResult(var path:String)