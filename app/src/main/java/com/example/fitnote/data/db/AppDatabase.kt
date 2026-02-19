package com.example.fitnote.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fitnote.data.dao.ExerciseDao
import com.example.fitnote.data.entity.ExerciseEntity

@Database(entities = [ExerciseEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitnote.db"
                )
                    .fallbackToDestructiveMigration() // ⭐ 핵심 한 줄
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
