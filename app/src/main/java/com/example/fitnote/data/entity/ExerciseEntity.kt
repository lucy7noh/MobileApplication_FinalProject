package com.example.fitnote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val time: Int,
    val calorie: Int,

    val imageUri: String? = null   // ✅ 추가된 컬럼

)
