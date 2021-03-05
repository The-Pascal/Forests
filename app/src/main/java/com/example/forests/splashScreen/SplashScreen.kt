package com.example.forests.splashScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat.postDelayed
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.forests.Main
import com.example.forests.R
import com.example.forests.onboarding.Onboarding
import com.google.firebase.auth.FirebaseAuth


class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed(
            {
                verifyUserIsLoggedIn()
            }, 2000)
    }

    private fun verifyUserIsLoggedIn(){

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is signed in
            val intent = Intent(this, Main::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        else
        {
            val intent = Intent(this, Onboarding::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Animatoo.animateFade(this)
        }
    }
}