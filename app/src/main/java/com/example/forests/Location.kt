package com.example.forests

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton

class Location : AppCompatActivity(), LocationListener  {
    private lateinit var locationManager: LocationManager
    //private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2
    private lateinit var PREF_LAT:String
    private lateinit var PREF_LON:String
    var gpsStatus: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        locationEnabled()

        val button = findViewById<CircularProgressButton>(R.id.button)
        val cityEditText = findViewById<EditText>(R.id.editTextLocationName)

        if(!gpsStatus){
            Toast.makeText(this, "Please turn on your location ", Toast.LENGTH_SHORT).show()
        }

        button.setOnClickListener {

            button.startAnimation()
            if(!gpsStatus){
                Toast.makeText(this, "Please turn on your location ", Toast.LENGTH_SHORT).show()
                button.revertAnimation();
            }else{
                if(!cityEditText.text.isEmpty()){
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                    var editor: SharedPreferences.Editor? = sharedPreferences.edit()
                    var flag=1;
                    val city  = cityEditText.text.toString()
                    for(i in city){
                        if(!i.isLetter()){
                            Toast.makeText(this, "Please Enter correct city without any digit and special caracters", Toast.LENGTH_SHORT).show()
                            button.revertAnimation();
                            flag=0;
                        }
                    }
                    locationEnabled()
                    if (gpsStatus){
                        if(flag==1 && city!="City"){
                            editor?.putString("city", city )
                            getLocation()
                        }else{
                            Toast.makeText(this, "Please Enter correct city without any digit and special caracters", Toast.LENGTH_SHORT).show()
                            button.revertAnimation();
                        }
                    }else{
                        Toast.makeText(this, "Please turn on your location ", Toast.LENGTH_SHORT).show()
                        button.revertAnimation();

                    }



                }else{
                    Toast.makeText(this, "Please Enter your city", Toast.LENGTH_SHORT).show()
                    button.revertAnimation();
                }
            }

        }



        //tvGpsLocation = findViewById(R.id.tvGpsLocation)


    }

    private fun locationEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }
    override fun onLocationChanged(p0: Location) {
        //tvGpsLocation.text = "Latitude:  ${p0.latitude}    Longitude:   ${p0.longitude}"
        println("--------------->$p0")

        val intent = Intent(this@Location, Main::class.java)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var editor: SharedPreferences.Editor? = sharedPreferences.edit()
        editor?.putString("lat", p0.latitude.toString())
        editor?.putString("lon", p0.longitude.toString())
        editor?.apply()
        intent.putExtra("lat",p0.latitude)
        intent.putExtra("lon", p0.longitude)
        startActivity(intent)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

data class locationData(val city: String, var lat: Int, var lon: Int)