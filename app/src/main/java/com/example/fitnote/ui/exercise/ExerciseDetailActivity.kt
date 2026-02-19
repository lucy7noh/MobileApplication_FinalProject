package com.example.fitnote.ui.exercise

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitnote.R
import com.example.fitnote.data.db.AppDatabase
import com.example.fitnote.data.entity.ExerciseEntity
import kotlinx.coroutines.launch

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var exerciseId: Int = -1

    // ✅ 사진 URI (Entity 기준)
    private var selectedPhotoUri: String? = null

    // 갤러리 런처 - OpenDocument를 사용하여 지속적인 URI 권한 획득
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                // URI에 대한 지속적인 접근 권한 부여 (Android 10+)
                try {
                    contentResolver.takePersistableUriPermission(
                        it,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    // 권한 부여 실패 시에도 계속 진행 (일부 경우에는 이미 권한이 있을 수 있음)
                    android.util.Log.w("ExerciseDetail", "URI 권한 부여 실패: ${e.message}")
                }
                
                selectedPhotoUri = it.toString()
                val imagePreview = findViewById<ImageView>(R.id.imagePreview)
                val btnAddPhoto = findViewById<Button>(R.id.btnAddPhoto)
                val btnChangePhoto = findViewById<Button>(R.id.btnChangePhoto)
                val btnRemovePhoto = findViewById<Button>(R.id.btnRemovePhoto)
                
                // Glide를 사용하여 이미지 로딩 (Android 10+ 호환성)
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imagePreview)
                
                // 이미지 표시 및 버튼 상태 변경
                imagePreview.visibility = View.VISIBLE
                btnAddPhoto.visibility = View.GONE
                btnChangePhoto.visibility = View.VISIBLE
                btnRemovePhoto.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_detail)

        db = AppDatabase.getInstance(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etTime = findViewById<EditText>(R.id.etTime)
        val etCal = findViewById<EditText>(R.id.etCal)

        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        val btnAddPhoto = findViewById<Button>(R.id.btnAddPhoto)
        val btnChangePhoto = findViewById<Button>(R.id.btnChangePhoto)
        val btnRemovePhoto = findViewById<Button>(R.id.btnRemovePhoto)
        val imagePreview = findViewById<ImageView>(R.id.imagePreview)

        // 사진 추가 버튼
        btnAddPhoto.setOnClickListener {
            galleryLauncher.launch(arrayOf("image/*"))
        }

        // 사진 변경 버튼
        btnChangePhoto.setOnClickListener {
            galleryLauncher.launch(arrayOf("image/*"))
        }

        // 사진 삭제 버튼
        btnRemovePhoto.setOnClickListener {
            selectedPhotoUri = null
            imagePreview.visibility = View.GONE
            btnAddPhoto.visibility = View.VISIBLE
            btnChangePhoto.visibility = View.GONE
            btnRemovePhoto.visibility = View.GONE
        }

        exerciseId = intent.getIntExtra("exerciseId", -1)

        // =========================
        // 🟢 추가 모드
        // =========================
        if (exerciseId == -1) {
            btnUpdate.visibility = View.GONE
            btnDelete.visibility = View.GONE

            btnAdd.setOnClickListener {
                val name = etName.text.toString()
                val time = etTime.text.toString().toIntOrNull()
                val calorie = etCal.text.toString().toIntOrNull()

                if (name.isBlank() || time == null || calorie == null) {
                    android.widget.Toast.makeText(
                        this,
                        "모든 항목을 입력해주세요.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    try {
                        db.exerciseDao().insert(
                            ExerciseEntity(
                                name = name,
                                time = time,
                                calorie = calorie,
                                imageUri = selectedPhotoUri
                            )
                        )
                        android.widget.Toast.makeText(
                            this@ExerciseDetailActivity,
                            "운동 기록이 추가되었습니다.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } catch (e: Exception) {
                        android.util.Log.e("ExerciseDetail", "추가 실패", e)
                        android.widget.Toast.makeText(
                            this@ExerciseDetailActivity,
                            "추가 중 오류가 발생했습니다.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // =========================
        // 🔵 수정 / 삭제 모드
        // =========================
        else {
            btnAdd.visibility = View.GONE

            lifecycleScope.launch {
                val exercise = db.exerciseDao().getById(exerciseId)
                exercise?.let {
                    etName.setText(it.name)
                    etTime.setText(it.time.toString())
                    etCal.setText(it.calorie.toString())

                    it.imageUri?.let { uriString ->
                        selectedPhotoUri = uriString
                        val uri = Uri.parse(uriString)
                        
                        // Glide를 사용하여 이미지 로딩
                        Glide.with(this@ExerciseDetailActivity)
                            .load(uri)
                            .centerCrop()
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(imagePreview)
                        
                        // 이미지 표시 및 버튼 상태 변경
                        imagePreview.visibility = View.VISIBLE
                        btnAddPhoto.visibility = View.GONE
                        btnChangePhoto.visibility = View.VISIBLE
                        btnRemovePhoto.visibility = View.VISIBLE
                    } ?: run {
                        // 이미지가 없는 경우
                        imagePreview.visibility = View.GONE
                        btnAddPhoto.visibility = View.VISIBLE
                        btnChangePhoto.visibility = View.GONE
                        btnRemovePhoto.visibility = View.GONE
                    }
                }
            }

            btnUpdate.setOnClickListener {
                val name = etName.text.toString()
                val time = etTime.text.toString().toIntOrNull()
                val calorie = etCal.text.toString().toIntOrNull()

                if (name.isBlank() || time == null || calorie == null) {
                    android.widget.Toast.makeText(
                        this,
                        "모든 항목을 입력해주세요.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // 수정 확인 다이얼로그
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("운동 기록 수정")
                    .setMessage("운동 기록을 수정하시겠습니까?")
                    .setPositiveButton("수정") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                db.exerciseDao().update(
                                    ExerciseEntity(
                                        id = exerciseId,
                                        name = name,
                                        time = time,
                                        calorie = calorie,
                                        imageUri = selectedPhotoUri
                                    )
                                )
                                android.widget.Toast.makeText(
                                    this@ExerciseDetailActivity,
                                    "운동 기록이 수정되었습니다.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } catch (e: Exception) {
                                android.util.Log.e("ExerciseDetail", "수정 실패", e)
                                android.widget.Toast.makeText(
                                    this@ExerciseDetailActivity,
                                    "수정 중 오류가 발생했습니다.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }

            btnDelete.setOnClickListener {
                // 삭제 확인 다이얼로그
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("운동 기록 삭제")
                    .setMessage("정말로 이 운동 기록을 삭제하시겠습니까?\n삭제된 기록은 복구할 수 없습니다.")
                    .setPositiveButton("삭제") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                db.exerciseDao().deleteById(exerciseId)
                                android.widget.Toast.makeText(
                                    this@ExerciseDetailActivity,
                                    "운동 기록이 삭제되었습니다.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } catch (e: Exception) {
                                android.util.Log.e("ExerciseDetail", "삭제 실패", e)
                                android.widget.Toast.makeText(
                                    this@ExerciseDetailActivity,
                                    "삭제 중 오류가 발생했습니다.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
    }
}
