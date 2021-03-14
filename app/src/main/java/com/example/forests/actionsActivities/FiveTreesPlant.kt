package com.example.forests.actionsActivities

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.forests.R
import com.example.forests.registerLogin.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_trees_planted.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.single_tree.view.*
import java.text.SimpleDateFormat
import java.util.*

class FiveTreesPlant : AppCompatActivity() {

    private val uid = FirebaseAuth.getInstance().uid
    var treesPlanted : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trees_planted)

//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarFiveTrees)
//        toolbar.title = "Plant Five Trees"
//        setSupportActionBar(toolbarFiveTrees)

        //image selection click listener
        add_pic_five_Trees.setOnClickListener{
            uploadImageDataTodevice()
        }

        fetchCurrentUser()

        fetchTrees()

        circularProgress()

    }

    private fun uploadImageDataTodevice()
    {
        add_pic_five_Trees.startAnimation()
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,0)

    }

    var selectedPhotoUri : Uri?= null

    var imageUrl: String ? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode== Activity.RESULT_OK && data!= null)
        {
            selectedPhotoUri = data.data

            val deepColor = Color.parseColor("#27E1EF")
            val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.blue_tick)
            add_pic_five_Trees.doneLoadingAnimation(deepColor, largeIcon)

            post_add_pic_five_Trees.visibility = View.VISIBLE
            post_add_pic_five_Trees.setOnClickListener {
                post_add_pic_five_Trees.startAnimation()
                uploadPhotoToFirebase()
            }

        }
    }

    private fun uploadPhotoToFirebase() {
        if (selectedPhotoUri == null){
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images-send-by-users/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.e("imageshare", "Photo uploaded successfully: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    imageUrl = it.toString()
                    Log.e("imageshare", "image downloaded url : $imageUrl")

                    val timestamp = System.currentTimeMillis()
                    val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid/fiveTrees/$timestamp")
                    treesPlanted += 1
                    val tree = Trees(
                        imageUrl.toString(),
                        System.currentTimeMillis()
                    )


                    ref.setValue(tree)
                        .addOnSuccessListener {
                            val handler = Handler()
                            handler.postDelayed({
                                add_pic_five_Trees.revertAnimation()
                                post_add_pic_five_Trees.revertAnimation()
                                fetchTrees()
                                FirebaseDatabase.getInstance().getReference("/Users/$uid").child("treesPlanted").setValue(treesPlanted)
                                add_pic_five_Trees.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rounded_button_login_register, 0, 0, 0);
                                post_add_pic_five_Trees.visibility = View.INVISIBLE
                            },1000)
                            selectedPhotoUri = null
                        }
                }
            }
    }

    private fun fetchTrees(){
        numberofTreesPlanted.text = "00"
        val ref =FirebaseDatabase.getInstance().getReference("/Users/$uid/fiveTrees")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            var i = 0;
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                all_trees_recyclerView.adapter = adapter
                p0.children.forEach{
                    val tree = it.getValue(Trees::class.java)
                    adapter.add(AddRecycleItemTrees(tree!!))
                    i += 1;
                    if(i>=5)
                        linearLayout.visibility = View.VISIBLE
                    numberofTreesPlanted.text = "0" + i.toString()
                }
            }

        })


    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
        ref.keepSynced(true)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val currentUser = p0.getValue(Users::class.java)
                treesPlanted = currentUser?.treesPlanted!!
                achievement_name_textView.text = currentUser?.username
                Picasso.get().load(currentUser?.imageUrl).into(userprofile_imageView)
            }

        })
    }

    private fun circularProgress() {
        val circularProgressBar = circularProgressBar
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


class AddRecycleItemTrees(private val tree: Trees): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.single_tree
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val sfd = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        viewHolder.itemView.plant_date.text = sfd.format(Date(tree.timestamp))
        Picasso.get().load(tree.treeImage).into(viewHolder.itemView.single_tree_image)
    }
}

@Parcelize
class Trees(val treeImage: String ,val timestamp: Long):
    Parcelable {
    constructor(): this("",-1)
}