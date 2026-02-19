package com.example.fitnote.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitnote.R
import com.example.fitnote.ui.home.HomeActivity
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPref = getSharedPreferences("profile", MODE_PRIVATE)

        val etAge = findViewById<EditText>(R.id.etAge)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val spActivity = findViewById<Spinner>(R.id.spActivity)
        val etTargetWeight = findViewById<EditText>(R.id.etTargetWeight)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // 활동량 스피너 설정
        val activityLevels = arrayOf(
            "거의 활동 없음 (sedentary)",
            "가벼운 활동 (light)",
            "보통 활동 (moderate)",
            "활발한 활동 (active)",
            "매우 활발한 활동 (very_active)"
        )
        val activityValues = arrayOf("sedentary", "light", "moderate", "active", "very_active")
        
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            activityLevels
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spActivity.adapter = adapter

        btnSave.setOnClickListener {
            val ageText = etAge.text.toString()
            val heightText = etHeight.text.toString()
            val weightText = etWeight.text.toString()
            val targetText = etTargetWeight.text.toString()

            if (ageText.isBlank() || heightText.isBlank() || weightText.isBlank() || targetText.isBlank()) {
                Toast.makeText(this, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageText.toIntOrNull()
            val height = heightText.toDoubleOrNull()
            val weight = weightText.toDoubleOrNull()
            val targetWeight = targetText.toDoubleOrNull()

            if (age == null || height == null || weight == null || targetWeight == null) {
                Toast.makeText(this, "올바른 숫자를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 입력값 검증
            val validationError = com.example.fitnote.data.api.CalorieCalculator.validateInputs(age, height, weight)
            if (validationError != null) {
                Toast.makeText(this, validationError, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val selectedGenderId = rgGender.checkedRadioButtonId
            val gender = if (selectedGenderId == R.id.rbMale) "male" else "female"
            val activityLevel = activityValues[spActivity.selectedItemPosition]

            // 칼로리 계산 (로컬 계산 사용)
            lifecycleScope.launch {
                try {
                    val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                    btnSave.isEnabled = false
                    btnSave.text = "계산 중..."
                    progressBar.visibility = android.view.View.VISIBLE

                    // 로컬 계산으로 BMR 및 TDEE 계산
                    val calculator = com.example.fitnote.data.api.CalorieCalculator
                    val bmr = calculator.calculateBMR(age, gender, height, weight)
                    val tdee = calculator.calculateTDEE(bmr, activityLevel)

                    // 계산된 권장 칼로리를 SharedPreferences에 저장
                    sharedPref.edit().apply {
                        putBoolean("hasProfile", true)
                        putInt("age", age)
                        putString("gender", gender)
                        putFloat("height", height.toFloat())
                        putFloat("weight", weight.toFloat())
                        putFloat("targetWeight", targetWeight.toFloat())
                        putString("activityLevel", activityLevel)
                        putFloat("recommendedCalorie", tdee.toFloat())
                        putFloat("bmr", bmr.toFloat())
                        apply()
                    }

                    // 계산 결과 상세 정보 표시
                    val bmrText = "기초대사량(BMR): ${bmr.toInt()} kcal"
                    val tdeeText = "권장 칼로리(TDEE): ${tdee.toInt()} kcal"
                    
                    android.util.Log.d("ProfileActivity", "계산 결과 - $bmrText, $tdeeText")
                    
                    Toast.makeText(
                        this@ProfileActivity,
                        "$tdeeText\n$bmrText",
                        Toast.LENGTH_LONG
                    ).show()

                    startActivity(Intent(this@ProfileActivity, HomeActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    // 오류 처리
                    val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                    progressBar.visibility = android.view.View.GONE
                    Toast.makeText(
                        this@ProfileActivity,
                        "계산 중 오류가 발생했습니다: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnSave.isEnabled = true
                    btnSave.text = "입력 완료"
                }
            }
        }
    }
}
