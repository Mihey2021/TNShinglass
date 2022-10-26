package ru.tn.shinglass.db.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.tn.shinglass.dao.room.*
import ru.tn.shinglass.entity.*

@Database(entities = [OptionsEntity::class, WarehousesEntity::class, TableScanEntity::class, DivisionsEntity::class, PhysicalPersonEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun optionsDao(): OptionsDao
    abstract fun warehousesDao(): WarehousesDao
    abstract fun tableScanDao(): TableScanDao
    abstract fun divisionsDao(): DivisionsDao
    abstract fun physicalPersonDao(): PhysicalPersonDao

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