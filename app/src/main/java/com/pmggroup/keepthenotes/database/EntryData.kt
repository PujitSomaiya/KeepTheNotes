package com.pmggroup.keepthenotes.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = MyDatabase.TABLE_NAME_ENTRY)
class EntryData : Serializable {

    @PrimaryKey(autoGenerate = true)
    var entry_id = 0

    var lastUpdatedDate: String? = null

    var description: String? = null

    var title: String? = null

    var bgDarkColor: Int? = null

    var bgLightColor: Int? = null

    var isPrivate: Boolean = false


}