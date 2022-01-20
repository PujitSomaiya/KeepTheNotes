package com.pmggroup.keepthenotes.database

import androidx.room.*


@Dao
interface DaoAccess {

    @Insert
    fun insertEntry(entry: EntryData): Long

    @Insert
    fun insertEntryList(entryList: List<EntryData?>)

    @Query("SELECT * FROM " + MyDatabase.TABLE_NAME_ENTRY)
    fun fetchAllEntries(): List<EntryData>

    @Query("SELECT * FROM " + MyDatabase.TABLE_NAME_ENTRY + " WHERE lastUpdatedDate = :date")
    fun fetchEntriesListByDate(date: String): List<EntryData>

    @Query("SELECT * FROM " + MyDatabase.TABLE_NAME_ENTRY + " WHERE entry_id = :entryId")
    fun fetchEntryListById(entryId: Int): EntryData

    @Delete
    fun deleteEntry(entry: EntryData): Int

    @Update
    fun updateEntry(entry: EntryData): Int

}