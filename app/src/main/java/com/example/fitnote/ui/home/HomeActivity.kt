package com.example.fitnote.ui.home

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitnote.R
import com.example.fitnote.data.db.AppDatabase
import com.example.fitnote.ui.exercise.ExerciseListActivity
import com.example.fitnote.ui.map.MapActivity
import com.example.fitnote.ui.profile.ProfileActivity
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPref = getSharedPreferences("profile", MODE_PRIVATE)
        db = AppDatabase.getInstance(this)

        // 권장 칼로리 표시
        displayRecommendedCalorie()
        
        // 운동 통계 표시
        displayExerciseStats()

        // 프로필 수정 버튼
        findViewById<Button>(R.id.btnProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // 운동 기록
        findViewById<Button>(R.id.btnExercise).setOnClickListener {
            startActivity(Intent(this, ExerciseListActivity::class.java))
        }

        // 지도
        findViewById<Button>(R.id.btnMap).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // 프로필 수정 후 돌아왔을 때 칼로리 업데이트
        displayRecommendedCalorie()
        // 운동 통계 업데이트
        displayExerciseStats()
    }

    private fun displayRecommendedCalorie() {
        val recommendedCalorie = sharedPref.getFloat("recommendedCalorie", 0f)
        val tvCalorie = findViewById<TextView>(R.id.tvRecommendedCalorie)
        
        if (recommendedCalorie > 0) {
            tvCalorie?.text = "권장 칼로리: ${recommendedCalorie.toInt()} kcal"
            tvCalorie?.visibility = android.view.View.VISIBLE
        } else {
            tvCalorie?.visibility = android.view.View.GONE
        }
    }
    
    private fun displayExerciseStats() {
        lifecycleScope.launch {
            try {
                val allExercises = db.exerciseDao().getAllOnce()
                val totalTime = allExercises.sumOf { it.time }
                val totalCalorie = allExercises.sumOf { it.calorie }
                
                val tvStats = findViewById<TextView>(R.id.tvExerciseStats)
                val tvAchievementRate = findViewById<TextView>(R.id.tvAchievementRate)
                
                if (totalTime > 0 || totalCalorie > 0) {
                    tvStats?.text = "총 운동 시간: ${totalTime}분 | 총 소모 칼로리: ${totalCalorie}kcal"
                    tvStats?.visibility = android.view.View.VISIBLE
                    
                    // 목표 칼로리 달성률 계산
                    val recommendedCalorie = sharedPref.getFloat("recommendedCalorie", 0f)
                    if (recommendedCalorie > 0 && totalCalorie > 0) {
                        val achievementRate = ((totalCalorie.toDouble() / recommendedCalorie.toDouble()) * 100).toInt()
                        tvAchievementRate?.text = "목표 달성률: ${achievementRate}%"
                        tvAchievementRate?.visibility = android.view.View.VISIBLE
                    } else {
                        tvAchievementRate?.visibility = android.view.View.GONE
                    }
                } else {
                    tvStats?.visibility = android.view.View.GONE
                    tvAchievementRate?.visibility = android.view.View.GONE
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeActivity", "통계 계산 실패", e)
            }
        }
    }
}
