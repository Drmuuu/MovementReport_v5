package com.railway.movementreport.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.railway.movementreport.data.dao.MovementEntryDao
import com.railway.movementreport.data.dao.NightDutyDao
import com.railway.movementreport.data.entity.MovementEntry
import com.railway.movementreport.data.entity.NightDutyEntry

@Database(
    entities = [MovementEntry::class, NightDutyEntry::class],
    version = 3,
    exportSchema = false
)
abstract class MovementDatabase : RoomDatabase() {
    abstract fun movementEntryDao(): MovementEntryDao
    abstract fun nightDutyDao(): NightDutyDao

    companion object {
        @Volatile private var INSTANCE: MovementDatabase? = null

        fun getDatabase(context: Context): MovementDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MovementDatabase::class.java,
                    "movement_database"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
