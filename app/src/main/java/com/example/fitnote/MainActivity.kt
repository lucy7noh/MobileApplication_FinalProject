package com.example.fitnote

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnote.ui.home.HomeActivity
import com.example.fitnote.ui.profile.ProfileActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = getSharedPreferences("profile", MODE_PRIVATE)
        val hasProfile = pref.getBoolean("hasProfile", false)

        if (hasProfile) {
            // 프로필 있음 → 홈
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // 최초 실행 → 프로필 입력
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        finish() // MainActivity는 화면 안 씀
    }
}
