package com.pmggroup.keepthenotes.database


import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [EntryData::class], version = 2, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun daoAccess(): DaoAccess?

    companion object {
        const val DB_NAME = "notes_db"
        const val TABLE_NAME_ENTRY = "notes"
    }
}

