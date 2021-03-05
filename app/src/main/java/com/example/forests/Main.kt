package com.example.forests

import android.content.Intent
import com.example.forests.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.forests.data.stateForestApiService
import com.example.forests.registerLogin.RegistrationPage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.android.material.bottomnavigation.BottomNavigationView

class Main : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        var lat = intent.getStringExtra("lat")
        var lon = intent.getStringExtra("lon")

        navController = Navigation.findNavController(this,R.id.myNavHostFragment)
        bottomNavigation.setupWithNavController(navController)

        /* val apiService = stateForestApiService()
         GlobalScope.launch(Dispatchers.Main){
             val response = apiService?.getTrees("json",0,1,"Bihar")?.await()
             val responseText = findViewById<TextView>(R.id.textView2)
             responseText.text = response!!.records.toString();
         }*/
        //val intent = Intent(this, Dashboard::class.java)
       // startActivity(intent)
    }
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.new_messages_menu ->{
//                startActivity(Intent(this, newmessageActivity::class.java))
            }
            R.id.profile -> {
//                startActivity(Intent(this, Profile::class.java))
            }
            R.id.sign_out_menu ->{
                val uid = FirebaseAuth.getInstance().uid
                FirebaseDatabase.getInstance().getReference("/Users/$uid/active")
                    .setValue(false)

                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegistrationPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
        }
        return super.onOptionsItemSelected(item)
    }

}