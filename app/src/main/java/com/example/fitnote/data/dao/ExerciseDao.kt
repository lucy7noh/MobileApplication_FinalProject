package com.example.fitnote.data.dao

import androidx.room.*
import com.example.fitnote.data.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    // 목록 (Flow)
    @Query("SELECT * FROM exercise ORDER BY id DESC")
    fun getAll(): Flow<List<ExerciseEntity>>

    // 단발 조회 (기존 코드 유지)
    @Query("SELECT * FROM exercise")
    suspend fun getAllOnce(): List<ExerciseEntity>

    // id로 단건 조회
    @Query("SELECT * FROM exercise WHERE id = :id")
    suspend fun getById(id: Int): ExerciseEntity?

    // 추가
    @Insert
    suspend fun insert(exercise: ExerciseEntity)

    // 수정
    @Update
    suspend fun update(exercise: ExerciseEntity)

    // 삭제 (id 기준) ⭐ 추가
    @Query("DELETE FROM exercise WHERE id = :id")
    suspend fun deleteById(id: Int)
}
