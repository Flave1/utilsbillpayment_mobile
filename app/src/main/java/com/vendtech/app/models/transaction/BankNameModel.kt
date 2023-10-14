package com.vendtech.app.models.transaction


import com.google.gson.annotations.SerializedName

data class BankNameModel(
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<Result>,
    @SerializedName("status") val status: String,
    @SerializedName("totalCount") val totalCount: Any
) {
    data class Result(
        @SerializedName("selected") val selected: Boolean,
        @SerializedName("text") val text: String,
        @SerializedName("value") val value: String
    )
}