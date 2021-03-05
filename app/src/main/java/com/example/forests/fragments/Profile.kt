package com.example.forests.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.forests.R
import com.example.forests.registerLogin.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class Profile : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        fetchCurrentUser(view)

        // circular progress
        circularProgress(view)


//        val adapter = GroupAdapter<ViewHolder>()
//        recycler_view_profile_achievements.adapter = adapter
//
//
//        adapter.add(AddRecycleItem());
//        adapter.add(AddRecycleItem());
//        adapter.add(AddRecycleItem());
//        adapter.add(AddRecycleItem());

        return view
    }

    private fun fetchCurrentUser(view: View){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val currentUser = p0.getValue(Users::class.java)
                view.achievement_name_textView.text = currentUser?.username
                view.email_profile.text = currentUser?.email
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

}

class AddRecycleItem(): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.achievement_recycler_items
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

}