package com.vendtech.app.models.transaction


import com.google.gson.annotations.SerializedName

data class RechargeTransactionNewListModel(
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<Result>,
    @SerializedName("status") val status: String,
    @SerializedName("totalCount") val totalCount: Int
) {
    data class Result(
        @SerializedName("amount") val amount: Double,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("meterNumber") val meterNumber: String,
        @SerializedName("posId") val posId: String,
        @SerializedName("rechargeId") val rechargeId: Int,
        @SerializedName("status") val status: String,
        @SerializedName("transactionId") val transactionId: String,
        @SerializedName("userName") val userName: String,
        @SerializedName("vendorName") val vendorName: String,
        @SerializedName("platformId") val platformId: Int
    )
}