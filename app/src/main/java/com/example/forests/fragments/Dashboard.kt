package com.example.forests.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.daasuu.cat.CountAnimationTextView
import com.example.forests.ForestData
import com.example.forests.R
import com.example.forests.Userdata
import com.example.forests.data.airQualityDataService
import com.example.forests.data.airQualityResponse.Data
import com.example.forests.data.revGeoCodingService
import com.example.forests.data.stateForestDataResponse.stateForestData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.RoundingMode
import kotlin.math.roundToInt


class Dashboard : Fragment() {
    private lateinit var v : View
    private lateinit var lattitude:String
    private lateinit var longitude:String
    private var targertrees = 0
    private var normalizedscore=0
    private var plantedtrees=0
    private  lateinit var rating:String
    private  lateinit var state:String
    private  lateinit var airQualityData: List<Data>
    private  lateinit var addressLocationData: List<Data>
    var firstTime = true;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        lattitude = sharedPreferences.getString("lat", " ").toString()
        longitude = sharedPreferences.getString("lon", " ").toString()
        state = sharedPreferences.getString("state", " ").toString()

        Log.i("Lattitude", lattitude.toString())
        Log.i("Longitude", longitude.toString())



    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_dashboard, container, false)


        val apiService = airQualityDataService()

        GlobalScope.launch(Dispatchers.Main) {
            val response = apiService?.getTreesByCoordinates(lattitude, longitude)?.await()
            if (response != null && firstTime) {
                airQualityData = response.data
                val aqi= airQualityData[0].aqi.toInt()
                val co= airQualityData[0].co.toInt()
                val so2= airQualityData[0].so2.toInt()
                val no2= airQualityData[0].no2.toInt()
                val o3= airQualityData[0].o3.toInt()
                val pm10= airQualityData[0].pm10.toInt()
                val pm25= airQualityData[0].pm25.toInt()



                Log.i("AirQualityAPIresponse", response.data.toString())

                val twForestDensity = v.findViewById<TextView>(R.id.forestDensityData)
              v.findViewById<CountAnimationTextView>(R.id.airQualityData).setAnimationDuration(1000).countAnimation(0,aqi)
                 v.findViewById<CountAnimationTextView>(R.id.coTextView).setAnimationDuration(1000).countAnimation(0,co)
                v.findViewById<CountAnimationTextView>(R.id.so2TextView).setAnimationDuration(1000).countAnimation(0,so2)
                v.findViewById<CountAnimationTextView>(R.id.no2TextView).setAnimationDuration(1000).countAnimation(0,no2)
                v.findViewById<CountAnimationTextView>(R.id.o3TextView).setAnimationDuration(1000).countAnimation(0,o3)
                v.findViewById<CountAnimationTextView>(R.id.pm10TextView).setAnimationDuration(1000).countAnimation(0,pm10)
                v.findViewById<CountAnimationTextView>(R.id.pm25TextView).setAnimationDuration(1000).countAnimation(0,pm25)
                getForestData(state)

            }

            firstTime=false
        }
            return v;
    }

    private fun getForestData(state: String){

        val ref = FirebaseDatabase.getInstance().getReference("/stateForestData/Haryana")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val forestData = p0.getValue(ForestData::class.java)
                Log.d("LatestMessages","Current User ${forestData}")
                val totalforestcover = (forestData?.actualforestcover?.toInt()
                    ?.plus(forestData.openforest?.toInt()))!!.times(100).toDouble()
                val totalArea = forestData?.geoarea?.toInt()
                var forestDensity = totalforestcover?.div(Math.max(1, totalArea!!
                ))!!


                val roundedForestDensity:Double = String.format("%.2f", forestDensity).toDouble()



                Log.d("LatestMessages","Current User ${roundedForestDensity}")

                val circularProgressBar = v.findViewById<CircularProgressBar>(R.id.circularProgressBar)
                circularProgressBar.apply {
                    setProgressWithAnimation(165f, 4000) // =1s
                    // Set Progress Max
                    progressMax = 200f
                    // Set ProgressBar Color
                    progressBarColor = Color.BLACK
                    // Set background ProgressBar Color
                    backgroundProgressBarColor = Color.WHITE
                    progressBarWidth = 4f // in DP
                    // Other
                    roundBorder = true
                    startAngle = 0f
                    progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT

                }

                v.findViewById<CountAnimationTextView>(R.id.forestDensityData).text = roundedForestDensity.toString()
                if (forestData != null) {
                    initializeUserData(forestData)
                }
            }

        })
    }


    private fun initializeUserData(forestData:ForestData){

        val aqi = airQualityData[0].aqi.toInt()
        val totalforestcover =(forestData.actualforestcover.toInt() + forestData.openforest.toInt())*10
        val totalArea = forestData.geoarea.toInt()
        val rating = "Rookie"
        Log.d("LatestMessages","totalforestcover $totalforestcover")
        Log.d("LatestMessages","forestData ${forestData.geoarea.toInt()}")

        var normalizedscore = aqi.div(Math.max(1,totalforestcover.div(Math.max(1,totalArea))))
        Log.d("LatestMessages","normalizedscore ${normalizedscore}")

        var plantedtrees =0;

            if(normalizedscore <500){
                targertrees = 4
                v.findViewById<CountAnimationTextView>(R.id.treePlantingData).setAnimationDuration(3000).countAnimation(0,targertrees)

            }else{
                targertrees = Math.ceil(((normalizedscore/100).toDouble())).roundToInt()
                v.findViewById<CountAnimationTextView>(R.id.treePlantingData).setAnimationDuration(3000).countAnimation(0,targertrees)
            }
        writeFirebaseData(lattitude,longitude,targertrees,normalizedscore,plantedtrees,rating)
    }

    private fun writeFirebaseData(lattitude:String,  longitude:String,  targertrees:Int,  normalizedscore:Int,  plantedtrees:Int,  rating:String){
        val userdata = Userdata(lattitude, longitude, targertrees, normalizedscore, plantedtrees, rating)
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/userdata/$uid")
        ref.setValue(userdata)
    }



    @UiThread
    suspend fun makeNetworkRequest() {
        val geoCodingService = revGeoCodingService()

        val response1 = geoCodingService?.getAddress(lattitude, longitude)

        if (response1 != null) {

            Log.i("RevGeoCodingAPIresponse", response1.children.toString())

        }
    }


}

