package com.example.test.db

import androidx.room.*
import com.example.test.model.LocationModel
import com.example.test.model.UserModel

@Dao
interface DAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userModel: UserModel): Long
    @Insert
    fun insertLocation(locationModel: LocationModel)
    @Query("UPDATE userMaster SET isCurrentUser = 1 WHERE email = :email")
    fun update(email: String)
    @Delete
    fun deleteUser(userModel: UserModel)
    @Query("DELETE FROM userMaster WHERE isCurrentUser = 1")
    fun deleteCurrentUser()
    @Query("SELECT * FROM userMaster")
    fun getAllUsers(): List<UserModel>
    @Query("SELECT * FROM locationMaster LIMIT 1")
    fun getLocation(): LocationModel
    @Query("SELECT * FROM userMaster WHERE isCurrentUser = 1")
    fun getCurrentUser(): UserModel
    @Query("UPDATE userMaster SET isCurrentUser = 0")
    fun updateCurrentUserToNone()
}