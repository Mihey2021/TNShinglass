package ru.tn.shinglass.db.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.tn.shinglass.dao.room.OptionsDao
import ru.tn.shinglass.dao.room.WarehousesDao
import ru.tn.shinglass.entity.OptionsEntity
import ru.tn.shinglass.entity.WarehousesEntity

@Database(entities = [OptionsEntity::class, WarehousesEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun optionsDao(): OptionsDao
    abstract fun warehousesDao(): WarehousesDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }
}