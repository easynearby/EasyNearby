package io.github.easynearby.demo.ui.message.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.easynearby.demo.base.ConnectionsManager

class MessageViewModel(private val deviceId: String) : ViewModel() {

    val messages = ConnectionsManager.receiveMessagesFor(deviceId)

    fun sendMessage(message: String) {
        ConnectionsManager.sendMessageFor(deviceId, message)
    }

    fun getConnectionName(): String? = ConnectionsManager.getConnectionNameFor(deviceId)

    companion object {
        fun provideViewModelFactory(deviceId: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return modelClass.getConstructor(String::class.java).newInstance(deviceId)
                }
            }
        }
    }

}