package com.example.forests

import android.content.SharedPreferences
import android.util.Log
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.daasuu.cat.CountAnimationTextView
import com.example.forests.data.airQualityDataService
import com.example.forests.data.airQualityResponse.airQualityData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FetchAPI( val lattitude:String, val longitude:String, val state: String) {


    val apiService = airQualityDataService()



     fun initializeDashboardData(myCallback: (result:DashboardData)-> Unit){

        getAirData(){
            if(it.normalizedscore >500){
                it.recommendedTarget = 4
            }else{
                it.recommendedTarget = Math.ceil((((1000-it.normalizedscore)/100).toDouble())).roundToInt()
            }
            myCallback.invoke(it)
        }

    }



    fun getAirData(myCallback: (result:DashboardData)-> Unit){
        GlobalScope.launch(Dispatchers.Main) {
            val response = apiService?.getTreesByCoordinates(lattitude, longitude)?.await()
            if (response != null ) {
                var dashboardData = DashboardData()
                var airQualityData = response.data
                dashboardData.aqi = airQualityData[0].aqi.toInt()
                dashboardData.so2 = airQualityData[0].so2.toInt()
                dashboardData.co = airQualityData[0].co.toInt()
                dashboardData.no2 = airQualityData[0].no2.toInt()
                dashboardData.o3 = airQualityData[0].o3.toInt()

                Log.v("FetchAPI", response.data.toString())

                getForestData(){
                    result ->
                    dashboardData.forestDensity = result.getValue("forestDensity")
                    dashboardData.actualForest = result.getValue("actualForest")
                    dashboardData.openForest = result.getValue("openForest")
                    dashboardData.noForest = result.getValue("noForest")
                    myCallback.invoke(dashboardData)
                }

            }
        }
    }


    private fun getForestData(myCallback: (result:Map<String,Int>)-> Unit){

        val ref = FirebaseDatabase.getInstance().getReference("/stateForestData/$state")

        ref.get().addOnSuccessListener {
            var forestData = it.getValue(ForestData::class.java)!!

            val totalArea = forestData?.geoarea?.toInt()
            var actualForest = forestData.actualforestcover.toInt()
            var openForest = forestData.openforest.toInt()
            var noForest = totalArea - ( actualForest + openForest)
            val totalforestcover = (forestData?.actualforestcover?.toInt()?.plus(forestData.openforest?.toInt()))?.times(100)?.toDouble()
            var forestDensity = totalArea?.let { Math.max(1, it) }?.let { totalforestcover?.div(it) }!!
            val roundedForestDensity:Double = String.format("%.2f", forestDensity).toDouble()

            Log.d("FetchAPI","Fetched forest data ${forestData}")


            //Log.d("LatestMessages","Current User ${roundedForestDensity}")
//            val totalcover : Double = (forestData.actualforestcover.toLong() + forestData.openforest.toLong() + forestData.noforest.toLong()).toDouble()
//                actualForestCover_progressView.progress = (( forestData?.actualforestcover.toFloat() / totalcover.toFloat() ) * 100)
//                openForest_progessView.progress = (( forestData?.openforest.toFloat() / totalcover.toFloat() ) * 100)
//                noForest_progressView.progress = (( forestData?.noforest.toFloat() / totalcover.toFloat() ) * 100)



            myCallback.invoke(mapOf("forestDensity" to roundedForestDensity.toInt(),
                                    "actualForest" to actualForest,
                                    "openForest" to openForest,
                                    "noForest" to noForest))
        }.addOnFailureListener{
            Log.v("FetchAPI", "Error getting forest data")

            myCallback.invoke(mapOf("forestDensity" to 0,
                "actualForest" to 0,
                "openForest" to 0,
                "noForest" to 0))
        }

    }





}