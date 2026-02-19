package com.example.fitnote.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {

    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int = 1500,
        @Query("keyword") keyword: String,
        @Query("key") apiKey: String
    ): PlacesResponse
}
