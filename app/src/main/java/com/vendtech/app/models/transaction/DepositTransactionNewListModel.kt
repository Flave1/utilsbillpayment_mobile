package com.vendtech.app.models.transaction


import com.google.gson.annotations.SerializedName

data class DepositTransactionNewListModel(
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<Result>,
    @SerializedName("status") val status: String,
    @SerializedName("totalCount") val totalCount: Int
) {
    data class Result(
        @SerializedName("amount") val amount: Double,
        @SerializedName("balance") val balance: Double,
        @SerializedName("bank") val bank: String,
        @SerializedName("chkNoOrSlipId") val chkNoOrSlipId: String,
        @SerializedName("comments") val comments: Any,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("depositId") val depositId: Int,
        @SerializedName("newBalance") val newBalance: Double,
        @SerializedName("percentageAmount") val percentageAmount: Double,
        @SerializedName("posNumber") val posNumber: String,
        @SerializedName("status") val status: String,
        @SerializedName("transactionId") val transactionId: String,
        @SerializedName("type") val type: String,
        @SerializedName("userName") val userName: String,
        @SerializedName("vendorName") val vendorName: String
    )
}