package com.example.tricycledispatchandzoneapp.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database as Database1

@Database1(
    entities = [DriverEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TODADatabase : RoomDatabase() {

    abstract fun driverDao(): DriverDao

    companion object {

        @Volatile
        private var INSTANCE: TODADatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    "ALTER TABLE drivers ADD COLUMN bodyNumber TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE drivers ADD COLUMN zoneCode TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getDatabase(context: Context): TODADatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TODADatabase::class.java,
                    "toda_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}