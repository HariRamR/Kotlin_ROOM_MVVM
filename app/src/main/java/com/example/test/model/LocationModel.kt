package com.example.test.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locationMaster")
data class LocationModel(

    @PrimaryKey(autoGenerate = true)
    val id1: Int = 0,
    @ColumnInfo(name = "lat")
    val latitude: String = "",
    @ColumnInfo(name = "long")
    val longitude: String = ""
)
