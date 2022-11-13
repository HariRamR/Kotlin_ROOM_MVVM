package com.example.test.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.runBlocking
import java.util.*

class Dashboard : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 1001
    private var viewModel: UserViewModel?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        title = "Dashboard"

        val recycler = findViewById<RecyclerView>(R.id.recycler_dashboard)
        recycler.layoutManager = LinearLayoutManager(this)
        val adapter = UserAdapter()
        recycler.adapter = adapter

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar_dashboard)

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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
    private fun fetchLocation(alertDialog: AlertDialog?, isFromLogOut: Boolean = false): List<Address>{

        var addressList: List<Address> ?= listOf()
        mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            val location: Location? = task.result
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())

                addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (isFromLogOut){

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
                    msgTv.text = String.format("%s", "Your check out location is \n Latitude: ${addressList!![0].latitude} \n Longitude: ${addressList!!.get(0).longitude}")

                    builder
                        .setPositiveButton(
                            "OK"
                        ) { dialog, _ ->

                            dialog.dismiss()
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        }
                    builder.setCancelable(false)
                    val dialog = builder.create()
                    dialog.show()
                }else{

                    viewModel!!.insertLocation(LocationModel(1, addressList!![0].latitude.toString(), addressList!![0].longitude.toString()))

                    runOnUiThread {
                        buildCheckInDialog("Your check in location is \n" +
                                " Latitude: ${addressList!![0].latitude} \n" +
                                " Longitude: ${addressList!![0].longitude}")
                    }
                }
            }
            alertDialog?.dismiss()
        }
        return addressList!!
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
}