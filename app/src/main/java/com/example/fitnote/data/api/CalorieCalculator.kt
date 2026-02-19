package com.example.fitnote.data.api

import android.util.Log

/**
 * 칼로리 계산 유틸리티 클래스
 * 실제 API가 없을 경우를 대비한 로컬 계산 로직
 */
object CalorieCalculator {
    
    private const val TAG = "CalorieCalculator"
    
    /**
     * 입력값 검증
     */
    fun validateInputs(age: Int, height: Double, weight: Double): String? {
        if (age < 10 || age > 120) {
            return "나이는 10세 이상 120세 이하여야 합니다"
        }
        if (height < 100 || height > 250) {
            return "키는 100cm 이상 250cm 이하여야 합니다"
        }
        if (weight < 20 || weight > 300) {
            return "몸무게는 20kg 이상 300kg 이하여야 합니다"
        }
        return null
    }
    
    /**
     * BMR (기초대사량) 계산 - Mifflin-St Jeor 공식
     * 공식: 
     * - 남성: BMR = 10 × 체중(kg) + 6.25 × 키(cm) - 5 × 나이 + 5
     * - 여성: BMR = 10 × 체중(kg) + 6.25 × 키(cm) - 5 × 나이 - 161
     */
    fun calculateBMR(age: Int, gender: String, height: Double, weight: Double): Double {
        val bmr = when (gender.lowercase()) {
            "male" -> 10 * weight + 6.25 * height - 5 * age + 5
            "female" -> 10 * weight + 6.25 * height - 5 * age - 161
            else -> 10 * weight + 6.25 * height - 5 * age - 161
        }
        
        Log.d(TAG, "BMR 계산: 성별=$gender, 나이=$age, 키=${height}cm, 몸무게=${weight}kg, BMR=${bmr}kcal")
        
        return bmr
    }
    
    /**
     * TDEE (총 소모 칼로리) 계산
     * TDEE = BMR × 활동계수
     */
    fun calculateTDEE(bmr: Double, activityLevel: String): Double {
        val activityMultiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2      // 거의 활동 없음
            "light" -> 1.375         // 가벼운 활동
            "moderate" -> 1.55       // 보통 활동
            "active" -> 1.725        // 활발한 활동
            "very_active" -> 1.9     // 매우 활발한 활동
            else -> 1.2
        }
        val tdee = bmr * activityMultiplier
        
        Log.d(TAG, "TDEE 계산: 활동량=$activityLevel, 계수=$activityMultiplier, TDEE=${tdee}kcal")
        
        return tdee
    }
}

