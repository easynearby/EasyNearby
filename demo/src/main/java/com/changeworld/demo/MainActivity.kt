package com.changeworld.demo

import android.os.Bundle
import com.changeworld.demo.databinding.ActivityMainBinding

class MainActivity : PermissionsActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}