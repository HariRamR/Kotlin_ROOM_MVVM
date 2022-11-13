package com.example.test.view_model

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.test.db.DAO
import com.example.test.db.Database
import com.example.test.model.LocationModel
import com.example.test.model.UserModel
import kotlinx.coroutines.*

class UserViewModel(): ViewModel() {

    private var userDetails: MutableLiveData<List<UserModel>>? = MutableLiveData<List<UserModel>>()
    private var database: Database?= null
    private var dao: DAO?= null
    private var isLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    private var application: Application?= null

    constructor(application: Application) : this() {

        if (database == null){
            this.application = application
            isLoading.value = true
            database = Database.getInstance(application)
            dao = database!!.getDAO()
            runBlocking {

                userDetails!!.value = getAllUsers()
            }
            isLoading.value = false
        }
    }

    fun insertUser(userModel: UserModel){

        GlobalScope.async {
            dao!!.updateCurrentUserToNone()
            dao!!.insertUser(userModel)
        }
    }
    fun updateUser(email: String){

        GlobalScope.async {
            dao!!.updateCurrentUserToNone()
            dao!!.update(email)
        }
    }

    fun updateCurrentUserNone(){
        GlobalScope.async {
            dao!!.updateCurrentUserToNone()
        }
    }

    fun insertLocation(locationModel: LocationModel){

        GlobalScope.async {
            dao!!.insertLocation(locationModel.latitude, locationModel.longitude)
        }
    }

    fun deleteLocation(){

        GlobalScope.async {
            dao!!.deleteLocation()
        }
    }

    fun getUserData(): LiveData<List<UserModel>>{
        return if(userDetails == null) MutableLiveData(listOf()) else userDetails!!
    }

    suspend fun getLocation(): LocationModel{

        val list = ArrayList<Deferred<LocationModel>>()
        list.add(GlobalScope.async {
           dao!!.getLocation()
        })
        val result = list.awaitAll()
        return result[0]
    }

    private suspend fun getAllUsers(): List<UserModel>{

        val list = ArrayList<Deferred<List<UserModel>>>()
        list.add(GlobalScope.async {
           dao!!.getAllUsers()
        })
        val result = list.awaitAll()
        return result[0]
    }

    fun isLoading(): LiveData<Boolean>{
        return isLoading
    }
}