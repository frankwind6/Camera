package com.fjk.camera.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = NetCameraDbConfig.VERSION, entities = [NetCamera::class], exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun netCameraDao(): NetCameraDao
    companion object {
        private var instance: AppDatabase? = null

        //返回一个数据库实例，单利模式，如果没创建过，就新创建一个名字为app_lang_database的数据库
        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                NetCameraDbConfig.DB_NAME
            ).allowMainThreadQueries()
                .build().apply {
                    instance = this
                }
        }

        //写一个双重校验的单利模式
        fun getDatabaseSingleton(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class.java) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            NetCameraDbConfig.DB_NAME
                        ).allowMainThreadQueries().build()
                            .apply { instance = this }
                    }
                }
            }
            return instance!!
        }
    }
}