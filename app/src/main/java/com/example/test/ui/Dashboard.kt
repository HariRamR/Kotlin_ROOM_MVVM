package com.example.test.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.adapter.UserAdapter
import com.example.test.model.LocationModel
import com.example.test.view_model.UserViewModel
import kotlinx.coroutines.runBlocking
import java.util.*

class Dashboard : AppCompatActivity() {

    private val permissionId = 1001
    private var viewModel: UserViewModel?= null
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        title = "Dashboard"

        val recycler = findViewById<RecyclerView>(R.id.recycler_dashboard)
        recycler.layoutManager = LinearLayoutManager(this)
        val adapter = UserAdapter()
        recycler.adapter = adapter

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar_dashboard)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        viewModel = UserViewModel(application)
        viewModel!!.getUserData().observe(this, Observer {
            it.let {
                adapter.setUserData(it, this)
            }
        })

        viewModel!!.isLoading().observe(this, Observer {
            if(it){
                progressBar.visibility = View.VISIBLE
            }else progressBar.visibility = View.GONE
        })

        getLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.location -> {

            runBlocking {
                val locationModel = viewModel!!.getLocation()

                if (locationModel == null){

                    getLocation()
                }else{

                    buildCheckInDialog("Your check in location is \n Latitude: ${locationModel.latitude} \n Longitude: ${locationModel.longitude}")
                }
            }

            true
        }
        R.id.logout ->{

            viewModel!!.updateCurrentUserNone()
            fetchLocation(alertDialog = null, isFromLogOut = true)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                val alertBuilder = AlertDialog.Builder(this, R.style.progress_dialog)
                alertBuilder.setTitle("Fetching Location!!")
                    .setMessage("Please wait while we fetch your current location")
                val alertDialog = alertBuilder.create()
                alertDialog.show()
                alertDialog.setCancelable(false)
                fetchLocation(alertDialog, false)
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation(alertDialog: AlertDialog?, isFromLogOut: Boolean = false){

        try {
            runBlocking {

                val loc = viewModel!!.getLocation()
                if(loc == null){

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, getLocationListner(isFromLogOut, alertDialog))
                }else if(isFromLogOut) {
                    viewModel!!.deleteLocation()
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, getLocationListner(isFromLogOut, alertDialog))
                }else{
                    if(alertDialog != null && alertDialog.isShowing){
                        alertDialog.dismiss()
                    }
                }
            }
        } catch (ex:SecurityException) {
            Log.e("LocationManager", "fetchLocation: Failed")
        }
    }

    private fun getLocationListner(isFromLogOut: Boolean, alertDialog: AlertDialog?): LocationListener{

        val locationListener: LocationListener = object : LocationListener{

            override fun onLocationChanged(location: Location) {

                locationManager.removeUpdates(this)
                if (isFromLogOut){

                    buildCheckOutDialog(location.latitude.toString(), location.longitude.toString())
                }else{

                    runBlocking {

                        viewModel!!.insertLocation(LocationModel(latitude = location.latitude.toString(), longitude = location.longitude.toString()))
                    }

                    runOnUiThread {
                        buildCheckInDialog("Your check in location is \n" +
                                " Latitude: ${location.latitude} \n" +
                                " Longitude: ${location.longitude}")
                    }
                }

                alertDialog?.dismiss()
            }
        }
        return locationListener
    }

    private fun buildCheckInDialog(msg: String){

        val builder = AlertDialog.Builder(this)
        val customLayout: View = layoutInflater.inflate(R.layout.custom_dialog, null)
        builder.setView(customLayout)
        val titleTv = customLayout
            .findViewById<TextView>(
                R.id.title_custom_dialog
            )
        val msgTv = customLayout
            .findViewById<TextView>(
                R.id.msg_custom_dialog
            )
        titleTv.text = getString(R.string.location)
        msgTv.text = msg

        builder
            .setPositiveButton(
                "OK"
            ) { dialog, _ ->

                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    fun buildCheckOutDialog(latitude: String, longitude: String){
        val builder = AlertDialog.Builder(this@Dashboard)
        val customLayout: View = layoutInflater.inflate(R.layout.custom_dialog, null)
        builder.setView(customLayout)
        val titleTv = customLayout
            .findViewById<TextView>(
                R.id.title_custom_dialog
            )
        val msgTv = customLayout
            .findViewById<TextView>(
                R.id.msg_custom_dialog
            )
        titleTv.text = getString(R.string.location)
        msgTv.text = String.format("%s", "Your check out location is \n Latitude: ${latitude} \n Longitude: ${longitude}")

        builder
            .setPositiveButton(
                "OK"
            ) { dialog, _ ->

                dialog.dismiss()
                startActivity(Intent(this@Dashboard, Login::class.java))
                finish()
            }
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }
}