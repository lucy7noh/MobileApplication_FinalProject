package com.example.fitnote.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesNearbyApi {

    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchNearby(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("keyword")     keyword: String?,   // ✅ nullable

        @Query("key") apiKey: String
    ): PlacesResponse
}
