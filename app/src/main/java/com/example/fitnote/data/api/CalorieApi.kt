package com.example.fitnote.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface CalorieApi {
    
    @GET("calculate")
    suspend fun calculateRecommendedCalorie(
        @Query("age") age: Int,
        @Query("gender") gender: String, // "male" or "female"
        @Query("height") height: Double, // cm
        @Query("weight") weight: Double, // kg
        @Query("activity") activityLevel: String // "sedentary", "light", "moderate", "active", "very_active"
    ): CalorieResponse
}

