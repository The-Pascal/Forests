package com.example.forests.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build.USER
import android.os.Bundle
import android.os.FileObserver
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import com.daasuu.cat.CountAnimationTextView
import com.example.forests.ForestData
import com.example.forests.Main
import com.example.forests.R
import com.example.forests.Userdata

import com.example.forests.actionsActivities.SendReferral
import com.example.forests.data.airQualityDataService
import com.example.forests.data.airQualityResponse.Data
import com.example.forests.data.revGeoCodingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class Dashboard : Fragment() {
    private lateinit var v : View
    private lateinit var lattitude:String
    private lateinit var longitude:String
    private var targertrees = 0
    var normal: Int = 0
    private var plantedtrees=0
    private  var rating:String = "Rookie"
    private  lateinit var state:String
    private lateinit var userdata: Userdata
    private lateinit var forestData: ForestData
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

        getForestData(state)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        v= inflater.inflate(R.layout.fragment_dashboard, container, false)

        getForestData(state)

        val apiService = airQualityDataService()
        Log.d("LatestMessages","Current User ${state}")

        GlobalScope.launch(Dispatchers.Main) {
            val response = apiService?.getTreesByCoordinates(lattitude, longitude)?.await()
            if (response != null && firstTime) {
                airQualityData = response.data
                val aqi= airQualityData[0].aqi.toInt()

                Log.i("AirQualityAPIresponse", response.data.toString())
                if(firstTime) {
                    v.findViewById<CountAnimationTextView>(R.id.airQualityData)
                        .setAnimationDuration(1000).countAnimation(0, aqi)
                }
                firstTime=false

                getForestData(state)

            }
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        if(sharedPreferences.contains("firstTimeUserData")){
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/userdata/$uid")

            ref.get().addOnSuccessListener {
                getUserData()

            }.addOnFailureListener{
                initializeUserData(forestData)
                Log.e("firebase", "Error getting data", it)
            }

        }else{
            getForestData(state)
            var editor: SharedPreferences.Editor? = sharedPreferences.edit()
            editor?.putString("firstTimeUserData", true.toString())
            editor?.apply()
        }
        circularloader(v)

        return v;
    }

    private fun circularloader(view: View){
        val circularProgressBar = view.findViewById<CircularProgressBar>(R.id.circularProgressBar)
        circularProgressBar.apply {
            Log.e("normal", normal.toString())
            setProgressWithAnimation(216f, 4000) // =1s
            progressMax = 1000f
            roundBorder = true
            startAngle = 180f
//            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT


        }
    }

    private fun initializeUserData(forestData:ForestData){

        val aqi = airQualityData[0].aqi.toInt()
        val totalforestcover =(forestData.actualforestcover.toInt() + forestData.openforest.toInt())*10
        val totalArea = forestData.geoarea.toInt()
        val rating = "Rookie"
        Log.d("LatestMessages","totalforestcover $totalforestcover")
        Log.d("LatestMessages","forestData ${forestData.geoarea.toInt()}")

        var normalizedscore = 1000- aqi.div(Math.max(1,totalforestcover.div(Math.max(1,totalArea))))
        normal = normalizedscore
        Log.d("LatestMessages","normalizedscore ${normalizedscore}")

        var plantedtrees =0;

        if(normalizedscore >500){
            targertrees = 4
            v.findViewById<CountAnimationTextView>(R.id.normalizedScoreData).setAnimationDuration(3000).countAnimation(0,targertrees)
        }else{
            targertrees = Math.ceil((((1000-normalizedscore)/100).toDouble())).roundToInt()
            v.findViewById<CountAnimationTextView>(R.id.normalizedScoreData).setAnimationDuration(3000).countAnimation(0,targertrees)
            v.findViewById<TextView>(R.id.statusText).statusText.text = "Critical! environment"
        }
        writeFirebaseData(lattitude,longitude,targertrees,normalizedscore,plantedtrees,rating)
    }

    private fun getUserData(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/userdata/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                userdata = p0.getValue(Userdata::class.java)!!
                Log.d("LatestMessages","Current User ${userdata}")
                var layoutId = when(userdata.presentaction){
                    1 -> R.layout.ongoing_cards1
                    2 -> R.layout.ongoing_cards2
                    3-> R.layout.ongoing_cards3
                    4-> R.layout.ongoing_cards4
                    else -> R.layout.recommended_cards
                }
                var cardLayout = layoutInflater.inflate(layoutId,null)
                v.findViewById<FrameLayout>(R.id.currentActionFrameLayout).addView(cardLayout)

                addItemsRecyclerView(v, userdata.presentaction,userdata.ongoingaction)
            }
        })


    }

    private fun getForestData(state: String){
        Log.d("LatestMessages","Current User ${state}")

        val ref = FirebaseDatabase.getInstance().getReference("/stateForestData/$state")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                forestData = p0.getValue(ForestData::class.java)!!

                Log.d("LatestMessages","Current User ${forestData}")

                val totalforestcover = (forestData?.actualforestcover?.toInt()?.plus(forestData.openforest?.toInt()))?.times(100)?.toDouble()
                val totalArea = forestData?.geoarea?.toInt()
                var forestDensity = totalArea?.let { Math.max(1, it) }?.let { totalforestcover?.div(it) }!!
                val roundedForestDensity:Double = String.format("%.2f", forestDensity).toDouble()


                Log.d("LatestMessages","Current User ${roundedForestDensity}")

                v.findViewById<CountAnimationTextView>(R.id.forestDensityData).text = roundedForestDensity.toString()

                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
                if(!sharedPreferences.contains("firstTimeUserData")){
                    initializeUserData(forestData)
                    var editor: SharedPreferences.Editor? = sharedPreferences.edit()
                    editor?.putString("firstTimeUserData", true.toString())
                    editor?.apply()
                }
            }

        })
    }


    private fun writeFirebaseData(lattitude:String,  longitude:String,  targertrees:Int,  normalizedscore:Int,  plantedtrees:Int,  rating:String){
        val userdata = Userdata(listOf<Int>(0),lattitude, longitude,  normalizedscore,listOf<Int>(0), plantedtrees,0, rating,targertrees,
            0,0,0,0,0)
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

    private fun addItemsRecyclerView(view: View, presentaction:Int, ongoingaction:List<Int>){


        val adapter = GroupAdapter<ViewHolder>()
        view.recommended_recyclerView.adapter = adapter

        val i:Int=1
        for(i in 1..4){
            if (i!=userdata.presentaction){
                adapter.add(AddRecycleItemRecommended(i))
            }
        }
        adapter.setOnItemClickListener{item, view ->
            val userItem = item as AddRecycleItemRecommended
            val intent= Intent(view.context , Main::class.java)
            startActivity(intent)
        }


    }

}



class AddRecycleItemRecommended(val a: Int): Item<ViewHolder>(){

    override fun getLayout(): Int {
        return when(a){
            1 -> R.layout.recommended_cards1
            2 -> R.layout.recommended_cards2
            3-> R.layout.recommended_cards3
            4-> R.layout.recommended_cards4
            else -> R.layout.recommended_cards
        }

    }

    override fun getItem(position: Int): Item<*> {
        return super.getItem(position)
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

}