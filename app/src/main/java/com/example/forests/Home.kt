package com.example.forests

import android.content.Intent
import com.example.forests.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.forests.registerLogin.RegistrationPage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
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