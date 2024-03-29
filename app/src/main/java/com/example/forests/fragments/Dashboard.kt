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
import androidx.appcompat.widget.PopupMenu
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
    var normal: Int = 0
    private  var rating:String = "Rookie"
    private  lateinit var state:String
    private lateinit var userdata: Userdata
    private var dashboardData = DashboardData()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v= inflater.inflate(R.layout.fragment_dashboard, container, false)
        v.airmore?.setOnClickListener {
            context?.let {
                val popupMenu = PopupMenu(it, airmore)
                popupMenu.menuInflater.inflate(R.menu.cardsmenu,popupMenu.menu)
                popupMenu.setOnMenuItemClickListener({
                    if (it.itemId == R.id.Refresh) {
                        Log.d("SignOut","Signout pressed")
                    }else if (it.itemId== R.id.more){
                        Log.d("Not clicked","Nothing happened")
                    }
                    true
                })

                popupMenu.show()
            }

        }
        v.forestmore?.setOnClickListener {
            context?.let {
                val popupMenu = PopupMenu(it, forestmore)
                popupMenu.menuInflater.inflate(R.menu.cardsmenu,popupMenu.menu)
                popupMenu.setOnMenuItemClickListener({
                    if (it.itemId == R.id.Refresh) {
                        Log.d("SignOut","Signout pressed")
                    }else if (it.itemId== R.id.more){
                        Log.d("Not clicked","Nothing happened")
                    }
                    true
                })

                popupMenu.show()
            }

        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        lattitude = sharedPreferences.getString("lat", " ").toString()
        longitude = sharedPreferences.getString("lon", " ").toString()
        state = sharedPreferences.getString("state", " ").toString()

        FetchAPI(lattitude,longitude,state).initializeDashboardData {
            dashboardData = it
            Log.i("Dashboard", "Dashboard data ${it}")
            addDataToView(dashboardData)
            getUserData()
            if(!sharedPreferences.contains("firstTimeUserData")) {
                initializeUserData(dashboardData, lattitude, longitude)
            }
            else
            {
                var editor: SharedPreferences.Editor? = sharedPreferences.edit()
                editor?.putString("firstTimeUserData", true.toString())
                editor?.apply()
            }
        }
        return v;
    }

    private fun addDataToView(dashboardData: DashboardData)
    {
        val normalizedscore = dashboardData.normalizedscore
        val airQuality = dashboardData.aqi
        val forestDensity = dashboardData.forestDensity
        var so2:Int = dashboardData.so2
        var co:Int = dashboardData.co
        var no2:Int = dashboardData.no2
        var o3: Int = dashboardData.o3
        var totalArea: Int = dashboardData.totalArea
        var actualForest:Int = dashboardData.actualForest
        var openForest:Int = dashboardData.openForest
        var noForest:Int = dashboardData.noForest
        var recommendedTarget:Int = dashboardData.recommendedTarget

        normalizedScoreData.setAnimationDuration(3000).countAnimation(0, dashboardData.normalizedscore)
        circularloader(normalizedscore.toFloat(), 1000f, circularProgressBar)

        airQualityData.setAnimationDuration(3000).countAnimation(0, airQuality)
        circularloader(airQuality.toFloat(), 500f, circularProgressBar_airQuality)

        forestDensityData.setAnimationDuration(3000).countAnimation(0, forestDensity)
        circularloader(forestDensity.toFloat(), 100f, circularProgressBar_forestDensity)

        val totalSum = actualForest + openForest + noForest
        Log.i("Dashboard", "Total Sum ${totalSum.toFloat()}")
        val actualForestPercentage = ( actualForest.toFloat() /totalSum.toFloat() ) * 100
        val openForestPercentage = ( openForest.toFloat() /totalSum.toFloat() ) * 100
        val noForestPercentage = ( noForest.toFloat() /totalSum.toFloat() ) * 100

        actualForestCover_progressView.progress = actualForestPercentage
        openForest_progessView.progress = openForestPercentage
        noForest_progressView.progress = noForestPercentage

        co_airQuality_progressView.progress = 52f
        so2_airQuality_progressbar.progress = 34f
        o3_airQuality_progressView.progress = 48f
        no2_airQuality_progressView.progress = 72f




    }


    private fun initializeUserData(dashboard: DashboardData, lattitude: String, longitude: String){

        val aqi = dashboard.aqi
        val totalforestcover = dashboard.actualForest
        val rating = "Rookie"
        var normalizedscore = dashboard.normalizedscore
        var plantedtrees =0;
        var targettrees = dashboard.recommendedTarget
        writeFirebaseData(lattitude,longitude,targettrees,normalizedscore,plantedtrees,rating)

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

    private fun circularloader(data: Float, max:Float, circularProgressBar : CircularProgressBar){
        circularProgressBar.apply {
            Log.e("normal", normal.toString())
            setProgressWithAnimation(data, 3000) // =1s
            progressMax = max
            roundBorder = true
            startAngle = 180f
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