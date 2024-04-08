package io.github.easynearby.demo

import android.os.Bundle
import io.github.easynearby.demo.databinding.ActivityMainBinding

class MainActivity : PermissionsActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}