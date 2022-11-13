package com.example.test.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userMaster")
data class UserModel(

    @ColumnInfo(name = "name")
    var name: String,
    @PrimaryKey
    @ColumnInfo(name = "email")
    var email: String,
    @ColumnInfo(name = "password")
    var password: String,
    @ColumnInfo(name = "isCurrentUser")
    var isCurrentUser: Int = 0
)