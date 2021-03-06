package com.example.forests.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.preference.PreferenceManager
import com.daasuu.cat.CountAnimationTextView
import com.example.forests.R
import com.example.forests.Userdata
import com.example.forests.registerLogin.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class Profile : Fragment() {
    private lateinit var userdata: Userdata
    private lateinit var v : View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        getUserData()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v = inflater.inflate(R.layout.fragment_profile, container, false)

        fetchCurrentUser(v)

        // circular progress
        circularProgress(v)
        getUserData()

        return v
    }


    private fun addItemsRecyclerView(view: View, completed:List<Int>){
        val adapter = GroupAdapter<ViewHolder>()
        view.recycler_view_profile_achievements.adapter = adapter

        for(i in completed){
            adapter.add(AddRecycleItem(i));
        }
    }


    private fun fetchCurrentUser(view: View){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
        ref.keepSynced(true)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val currentUser = p0.getValue(Users::class.java)
                view.number_trees_planted_textView8.text = currentUser?.treesPlanted.toString()
                view.achievement_name_textView.text = currentUser?.username
                view.email_profile.text = currentUser?.email
                Picasso.get().load(currentUser?.imageUrl).into(view.userprofile_imageView)
            }

        })
    }

    private fun circularProgress(view: View) {
        val circularProgressBar = view.circularProgressBar
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
    }
    private fun getUserData(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/userdata/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                userdata = p0.getValue(Userdata::class.java)!!
                Log.d("LatestMessages", "Profile  ${userdata}")

                v.findViewById<CountAnimationTextView>(R.id.number_trees_planted_textView)
                    .setAnimationDuration(1000).countAnimation(0, userdata.plantedtrees)
                v.findViewById<CountAnimationTextView>(R.id.number_trees_referred_textView)
                    .setAnimationDuration(1000).countAnimation(0, userdata.treesreferred)

                addItemsRecyclerView(v, userdata.completedaction)

            }
        })
    }


    private fun share( a:Int){
        var message= ""
        when(a){
            0 -> "Hey!! I achieve Environment enthusiast status on joining Forest App. You can be the same and work for the sustainable future! "
            1 -> "Hey!! I planted 5 trees after joining Forest App. You can do the same and work for the sustainable future! "
            2 -> "Hey!! I planted 10 trees after joining Forest App. You can do the same and work for the sustainable future! "
            3-> "Hey!! I helped spreading awareness regarding climate change after joining Forest App. You can do the same and work for the sustainable future! "
            4-> "Hey!! I'm using Public Transport after joining Forest App. You can do the same and work for the sustainable future! "
            else -> " "
        }
            val intent= Intent()
            intent.action=Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT,message)
            intent.type="text/plain"
            startActivity(Intent.createChooser(intent,"Share To:"))

    }
}

class AddRecycleItem(var a:Int): Item<ViewHolder>(){
    override fun getLayout(): Int {

        return when(a){
            1 -> R.layout.achievement_recycler_items1
            2 -> R.layout.achievement_recycler_items2
            3-> R.layout.achievement_recycler_items3
            4-> R.layout.achievement_recycler_items4
            else -> R.layout.achievement_recycler_items0
        }
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

}