package com.example.test.db

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.test.model.LocationModel
import com.example.test.model.UserModel

@androidx.room.Database(entities = [UserModel::class, LocationModel::class], version = 2)
abstract class Database : RoomDatabase() {

    abstract fun getDAO(): DAO

    companion object{
        private var database: Database?=null

        fun getInstance(application: Application): Database{

            if (database == null){
                database = Room.databaseBuilder(application.applicationContext, Database::class.java,
                "local_db").fallbackToDestructiveMigration().build()
            }
            return database!!
        }
    }
}