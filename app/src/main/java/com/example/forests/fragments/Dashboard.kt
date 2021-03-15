package com.example.forests.fragments

import android.content.Context
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
import com.airbnb.lottie.L
import com.daasuu.cat.CountAnimationTextView
import com.example.forests.*
import com.example.forests.R
import com.example.forests.actionsActivities.FiveTreesPlant

import com.example.forests.actionsActivities.SendReferral
import com.example.forests.data.airQualityDataService
import com.example.forests.data.airQualityResponse.Data
import com.example.forests.data.revGeoCodingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikhaellopez.circularprogressbar.CircularProgressBar
//import com.skydoves.progressview.ProgressView
//import com.skydoves.progressview.progressView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import kotlinx.android.synthetic.main.recommended_cards1.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.Copy
import kotlin.math.absoluteValue
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
    private var dashboardData = DashboardData()
    var firstTime = true;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        lattitude = sharedPreferences.getString("lat", " ").toString()
        longitude = sharedPreferences.getString("lon", " ").toString()
        state = sharedPreferences.getString("state", " ").toString()

        FetchAPI(lattitude,longitude,state).initializeDashboardData {
            dashboardData = it
            Log.i("Dashboard", "Dashboard data ${it}")

        }

        //getForestData(state)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v= inflater.inflate(R.layout.fragment_dashboard, container, false)



//
//
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
//        if(sharedPreferences.contains("firstTimeUserData")){
//            val uid = FirebaseAuth.getInstance().uid
//            val ref = FirebaseDatabase.getInstance().getReference("/userdata/$uid")
//
//            ref.get().addOnSuccessListener {
//                getUserData()
//            }.addOnFailureListener{
//                Log.e("firebase", "Error getting data", it)
//            }
//
//        }else{
//            getForestData(state)
//            var editor: SharedPreferences.Editor? = sharedPreferences.edit()
//            editor?.putString("firstTimeUserData", true.toString())
//            editor?.apply()
//        }
//









//        getForestData(state)
//
//        val apiService = airQualityDataService()
//        GlobalScope.launch(Dispatchers.Main) {
//            val response = apiService?.getTreesByCoordinates(lattitude, longitude)?.await()
//            if (response != null && firstTime) {
//                airQualityData = response.data
//                val aqi= airQualityData[0].aqi.toInt()
//                Log.i("AirQualityAPIresponse", response.data.toString())
//                if(firstTime) {
//                    v.findViewById<CountAnimationTextView>(R.id.airQualityData)
//                        .setAnimationDuration(1000).countAnimation(0, aqi)
//                    circularloader(500 - aqi.toFloat(), 500f, circularProgressBar_airQuality)
//                    Log.d("airQuality", "${airQualityData[0].co.toFloat()}")
////                    co_airQuality_progressView.progress = airQualityData[0].co.toFloat()
//
//                }
//                firstTime=false
//                getForestData(state)
//
//            }
//        }



        return v;
    }


    private fun initializeUserData(forestData:ForestData){

        val aqi = airQualityData[0].aqi.toInt()
        val totalforestcover =(forestData.actualforestcover.toInt() + forestData.openforest.toInt())*10
        val totalArea = forestData.geoarea.toInt()
        val rating = "Rookie"
        var normalizedscore = 1000- aqi.div(Math.max(1,totalforestcover.div(Math.max(1,totalArea))))
        normal = normalizedscore
        circularloader(normal.toFloat(), 1000f, circularProgressBar)
        normalizedScoreData.text = normal.toString()
        Log.d("LatestMessages","normalizedscore $normalizedscore")
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

    private fun writeFirebaseData(lattitude:String,  longitude:String,  targertrees:Int,  normalizedscore:Int,  plantedtrees:Int,  rating:String){

        val remainingaction = listOf<Int>(1, 2, 3, 4)

        val userdata = Userdata(
            normalizedscore,
            lattitude,
            longitude,
            plantedtrees,
            0,
            listOf<Int>(0),
            listOf<Int>(0),
            remainingaction,
            0,
            rating,
            targertrees,
            0,
            0,
            0,
            0
        )

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


    private fun getUserData(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/userdata/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                userdata = p0.getValue(Userdata::class.java)!!
                addItemsRecyclerView(v, userdata)
            }
        })
    }

    private fun addItemsRecyclerView(view: View, userdata: Userdata){

        val adapter = GroupAdapter<ViewHolder>()
        view.recommended_recyclerView.adapter = adapter

        userdata.remainingaction.forEach {
            adapter.add(AddRecycleItemRecommended(it))
        }

        adapter.setOnItemClickListener{item, view ->
            val userItem = item as AddRecycleItemRecommended
            if(userItem.a == 4){
                val intent= Intent(view.context , SendReferral::class.java)
                startActivity(intent)
            }
            else
            {
                val intent= Intent(view.context , FiveTreesPlant::class.java)
                startActivity(intent)
            }
        }

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
                circularloader(roundedForestDensity.toFloat(),100f, circularProgressBar_forestDensity)

                val totalcover : Double = (forestData.actualforestcover.toLong() + forestData.openforest.toLong() + forestData.noforest.toLong()).toDouble()
//                actualForestCover_progressView.progress = (( forestData?.actualforestcover.toFloat() / totalcover.toFloat() ) * 100)
//                openForest_progessView.progress = (( forestData?.openforest.toFloat() / totalcover.toFloat() ) * 100)
//                noForest_progressView.progress = (( forestData?.noforest.toFloat() / totalcover.toFloat() ) * 100)

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

    private fun makeProgressView(){
    }


    private fun circularloader(data: Float, max:Float, circularProgressBar : CircularProgressBar){
        circularProgressBar.apply {
            Log.e("normal", normal.toString())
            setProgressWithAnimation(data, 3000) // =1s
            progressMax = max
            roundBorder = true
            startAngle = 180f
//            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
        }
    }

}


class AddRecycleItemRecommended(val a: Int): Item<ViewHolder>(){

    //Create instances of all actions
    private val action1 = AllTasks(1, "Plant five Trees", "Let's begin a new journey!", 5)
    private val action2 = AllTasks(2, "Plant ten Trees", "Let's begin a new journey!", 10)
    private val action3 = AllTasks(3, "Use Public Transport", "Let's begin a new journey!", 15)
    private val action4 = AllTasks(4, "Refer a friend", "Let's begin a new journey!", 5)

    override fun getLayout(): Int {
        return R.layout.recommended_cards1
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        var action = AllTasks()
        action = when(a)
        {
            1->action1
            2->action2
            3->action3
            4->action4
            else->action1
        }
        viewHolder.itemView.action_textView.text = action.topic
        viewHolder.itemView.action_textView_subtext.text = action.subtopic
    }

}