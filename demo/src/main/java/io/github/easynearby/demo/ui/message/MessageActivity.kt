package io.github.easynearby.demo.ui.message

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.easynearby.demo.base.Message
import io.github.easynearby.demo.databinding.ActivityMessageBinding
import io.github.easynearby.demo.ui.message.viewmodel.MessageViewModel
import kotlinx.coroutines.launch

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding

    private val deviceId by lazy {
        intent.getStringExtra(EXTRA_DEVICE_ID) ?: error("Missing device id")
    }
    private val viewModel by viewModels<MessageViewModel> {
        MessageViewModel.provideViewModelFactory(deviceId)
    }

    private lateinit var adapter: MessageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        setTitle("Messaging with ${viewModel.getConnectionName()}")


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.chatboxLayout) { v, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = if (imeVisible) imeHeight else 0
            }
            insets
        }

        initRecyclerView()

        binding.chatSendButton.setOnClickListener {
            with(binding.chatMessageEdittext.text.toString().trim()) {
                if (isBlank()) {
                    Toast.makeText(this@MessageActivity, "Error: Empty message", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                viewModel.sendMessage(this)
                binding.chatMessageEdittext.text?.clear()
            }
        }

        lifecycleScope.launch {
            viewModel.messages.collect {
                adapter.addMessage(it)
                binding.noMessagesLabel.visibility = View.INVISIBLE
            }
        }

        binding.closeImage.setOnClickListener {
            finish()
        }
    }

    private fun initRecyclerView() {
        adapter = MessageListAdapter(emptyList())
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = adapter
        toogleNoMessagesLabelIfNeeded()
    }

    private fun toogleNoMessagesLabelIfNeeded(messages: List<Message> = emptyList()) {
        if (messages.isEmpty()) {
            binding.noMessagesLabel.visibility = View.VISIBLE
        } else {
            binding.noMessagesLabel.visibility = View.INVISIBLE
        }
    }

    companion object {
        val EXTRA_DEVICE_ID = "device_id"
    }
}