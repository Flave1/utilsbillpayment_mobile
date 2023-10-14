package com.vendtech.app.models.meter


import com.google.gson.annotations.SerializedName

data class PosListModel(
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<Result>,
    @SerializedName("status") val status: String,
    @SerializedName("totalCount") val totalCount: Int
) {
    data class Result(
        @SerializedName("balance") val balance: Double,
        @SerializedName("enabled") val enabled: Boolean,
        @SerializedName("phone") val phone: String,
        @SerializedName("posId") val posId: Int,
        @SerializedName("serialNumber") val serialNumber: String,
        @SerializedName("vendorName") val vendorName: String,
        @SerializedName("vendorType") val vendorType: String
    )
}