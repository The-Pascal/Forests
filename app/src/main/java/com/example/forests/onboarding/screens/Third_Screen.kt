package com.example.forests.onboarding.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.forests.R
import com.example.forests.registerLogin.RegistrationPage
import kotlinx.android.synthetic.main.fragment_third__screen.view.*


class Third_Screen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_third__screen, container, false)

        view.get_started.setOnClickListener {
            val intent = Intent(activity, RegistrationPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or ( Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        return view
    }

}