package com.vendtech.app.models.meter


import com.google.gson.annotations.SerializedName

data class PosResultModel(
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<Result>,
    @SerializedName("status") val status: String,
    @SerializedName("totalCount") val totalCount: Any
) {
    data class Result(
        @SerializedName("balance") val balance: String,
        @SerializedName("posId") val posId: Int,
        @SerializedName("serialNumber") val serialNumber: String,
        @SerializedName("percentage") val percentage: Double
    )
}