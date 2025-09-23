package com.azzahid.hof.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.azzahid.hof.data.dao.RouteDao
import com.azzahid.hof.data.entity.RouteEntity
import com.azzahid.hof.domain.model.HttpRequestLog

@Database(
    entities = [RouteEntity::class, HttpRequestLog::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun RouteDao(): RouteDao
    abstract fun httpRequestLogDao(): HttpRequestLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "http_on_fire_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}