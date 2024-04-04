package com.changeworld.demo.advertise.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.changeworld.demo.advertise.viewmodel.AdvertiseViewModel
import com.changeworld.demo.base.ui.BaseFragment
import com.changeworld.demo.base.viewmodel.BaseViewModel

class AdvertiseFragment : BaseFragment() {

    private val viewmodel by viewModels<AdvertiseViewModel>()
    override fun provideViewModel(): BaseViewModel {
        return viewmodel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.operationSwitch.text = "Advertise"
    }
}