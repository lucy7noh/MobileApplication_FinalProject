package com.example.fitnote.ui.exercise

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnote.R
import com.example.fitnote.data.db.AppDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ExerciseListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_list)

        val btnAdd = findViewById<Button>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            startActivity(Intent(this, ExerciseDetailActivity::class.java))
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvExercise)
        val tvEmptyMessage = findViewById<TextView>(R.id.tvEmptyMessage)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = ExerciseAdapter(emptyList()) { exercise ->
            val intent = Intent(this, ExerciseDetailActivity::class.java)
            intent.putExtra("exerciseId", exercise.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val db = AppDatabase.getInstance(this)

        lifecycleScope.launch {
            try {
                db.exerciseDao().getAll().collect { list ->
                    adapter.submitList(list)
                    // 빈 목록 메시지 표시/숨김
                    if (list.isEmpty()) {
                        tvEmptyMessage.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvEmptyMessage.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            } catch (e: CancellationException) {
                // 코루틴이 정상적으로 취소된 경우 (Activity 종료 시) - 무시
                throw e // CancellationException은 다시 throw해야 함
            } catch (e: Exception) {
                // 실제 오류인 경우에만 사용자에게 알림
                android.util.Log.e("ExerciseList", "데이터 로딩 실패", e)
                // Activity가 종료 중이 아닐 때만 메시지 표시
                if (!isFinishing && !isDestroyed) {
                    android.widget.Toast.makeText(
                        this@ExerciseListActivity,
                        "운동 기록을 불러오는 중 오류가 발생했습니다.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
