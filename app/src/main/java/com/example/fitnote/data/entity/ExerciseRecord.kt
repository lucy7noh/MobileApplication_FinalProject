package com.example.fitnote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExerciseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val durationMin: Int,
    val calories: Int
)
