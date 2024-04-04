package com.changeworld.demo.discover.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.changeworld.demo.base.ui.BaseFragment
import com.changeworld.demo.base.viewmodel.BaseViewModel
import com.changeworld.demo.discover.viewmodel.DiscoverViewModel

class DiscoverFragment : BaseFragment() {

    private val viewModel by viewModels<DiscoverViewModel>()

    override fun provideViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.operationSwitch.text = "Discover"
    }
}