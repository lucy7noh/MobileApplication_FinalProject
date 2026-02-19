package com.example.fitnote.data.api

import com.google.gson.annotations.SerializedName

data class CalorieResponse(
    @SerializedName("bmr")
    val bmr: Double, // 기초대사량
    
    @SerializedName("tdee")
    val tdee: Double, // 총 소모 칼로리 (권장 칼로리)
    
    @SerializedName("message")
    val message: String
)

