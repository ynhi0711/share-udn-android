package com.nhi.datn.share_udn_android.models

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("status_code")
    val statusCode: Int? = null,
    val messages: String? = null
)