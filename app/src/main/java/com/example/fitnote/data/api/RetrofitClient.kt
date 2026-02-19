package com.example.fitnote.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://maps.googleapis.com/"

    // Google Maps Places API
    val placesNearbyApi: PlacesNearbyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlacesNearbyApi::class.java)
    }
    
    // 칼로리 계산은 로컬 계산 사용 (CalorieCalculator 참고)
    // 필요 시 실제 API 엔드포인트로 변경 가능
    // val calorieApi: CalorieApi by lazy { ... }
}
