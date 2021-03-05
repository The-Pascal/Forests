package com.example.forests.onboarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Adapter
import com.cuberto.liquid_swipe.LiquidPager
import com.example.forests.R
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory

class Onboarding : AppCompatActivity() {

    lateinit var pager: LiquidPager
    lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        adapter = ViewPagerAdapter(supportFragmentManager)
        pager = findViewById<LiquidPager>(R.id.pager)
        pager.adapter = adapter
    }
}