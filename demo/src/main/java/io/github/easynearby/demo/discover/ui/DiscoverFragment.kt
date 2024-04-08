package io.github.demo.discover.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import io.github.demo.base.ui.BaseFragment
import io.github.easynearby.demo.base.viewmodel.BaseViewModel
import io.github.easynearby.demo.discover.viewmodel.DiscoverViewModel

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