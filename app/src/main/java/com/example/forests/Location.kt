package com.example.forests

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_location.*

class Location : AppCompatActivity(), LocationListener  {
    private lateinit var locationManager: LocationManager
    //private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2
    private lateinit var lattitude:String
    private lateinit var longitude:String
    private lateinit var state:String
    var gpsStatus: Boolean = false
    var stateList:List<String> = listOf("andhra pradesh", "arunachal pradesh","assam","bihar","chhattisgarh","delhi","new delhi","goa","gujarat","haryana","himachal pradesh","jammu and kashmir","jharkhand","karnataka", "kerala","madhya pradesh","maharashtra","meghalaya","manipur","mizoram","nagaland","orissa","punjab","rajasthan","sikkim","tamil nadu","telangana","tripura","uttar pradesh","uttarakhand","west bengal","andaman and nicobar","chandigarh","dadar nagar haveli","daman and diu","lakshadweep","puducherry")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        locationEnabled()

        val button = findViewById<CircularProgressButton>(R.id.button)
        val cityEditText = findViewById<EditText>(R.id.editTextLocationName)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        lattitude = sharedPreferences.getString("lat", " ").toString()
        longitude = sharedPreferences.getString("lon", " ").toString()
        if(lattitude.length>2 && longitude.length>2){
            val intent = Intent(this@Location, Main::class.java)
            startActivity(intent)
        }else{
            locationEnabled()
            if(!gpsStatus){
                Toast.makeText(this, "Please turn on your location ", Toast.LENGTH_SHORT).show()
            }
        }


        button.setOnClickListener {
            locationEnabled()
            button.startAnimation()
            if(!gpsStatus){
                Toast.makeText(this, "Please turn on your location ", Toast.LENGTH_SHORT).show()
                button.revertAnimation();
            }else{
                if(!cityEditText.text.isEmpty()){
                    var flag=1;
                    var temp  = cityEditText.text.toString()

                    for(i in temp){
                        if(!i.isLetter()){
                            if(!i.isWhitespace()){
                                Toast.makeText(
                                    this,
                                    "Please Enter correct city without any digit and special caracters",
                                    Toast.LENGTH_SHORT
                                ).show()
                                button.revertAnimation();
                                flag=0;
                            }

                        }
                    }

                    var state = ""

                    for (c in temp) {
                        state += when {
                            c.isUpperCase() -> c.toLowerCase()
                            c.isWhitespace() -> " "
                            else -> c
                        }
                    }
                    Log.d("state", state)
                    if(state in stateList) {

                        locationEnabled()
                        if (gpsStatus){
                            if(flag==1 && state!="State"){

                                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                                    this
                                )
                                var editor: SharedPreferences.Editor? = sharedPreferences.edit()
                                editor?.putString("state", state)
                                editor?.apply()
                                getLocation()
                            }else{
                                Toast.makeText(
                                    this,
                                    "Please Enter correct city without any digit and special caracters",
                                    Toast.LENGTH_SHORT
                                ).show()
                                button.revertAnimation();
                            }
                        }else{
                            Toast.makeText(
                                this,
                                "Please turn on your location ",
                                Toast.LENGTH_SHORT
                            ).show()
                            button.revertAnimation();

                        }

                    }

                }else{
                    Toast.makeText(this, "Please Enter your city", Toast.LENGTH_SHORT).show()
                    button.revertAnimation();
                }
            }

        }

    }

    private fun locationEnabled() {
         locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }
    }

    override fun onLocationChanged(p0: Location) {
        //tvGpsLocation.text = "Latitude:  ${p0.latitude}    Longitude:   ${p0.longitude}"
        Log.d("LatestMessages","Location Changed --------------->")

        val intent = Intent(this@Location, Main::class.java)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var editor: SharedPreferences.Editor? = sharedPreferences.edit()
        editor?.putString("lat", p0.latitude.toString())
        editor?.putString("lon", p0.longitude.toString())
        editor?.apply()
        intent.putExtra("lat", p0.latitude)
        intent.putExtra("lon", p0.longitude)
        writeFirebaseData(lattitude,  longitude)

        startActivity(intent)
    }
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@Location, Main::class.java)
                        writeFirebaseData(lattitude,  longitude)
                            startActivity(intent)
                        }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
    private fun writeFirebaseData(lattitude:String,  longitude:String){
        val remainingaction = listOf<Int>(1, 2, 3, 4)
        val userdata = Userdata(
            0,
            lattitude,
            longitude,
            0,
            0,
            listOf<Int>(0),
            listOf<Int>(0),
            remainingaction,
            0,
            "Rookie",
            0,
            0,
            0,
            0,
            0
        )
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/userdata/$uid")
        ref.setValue(userdata)
    }

}

data class locationData(val city: String, var lat: Int, var lon: Int)