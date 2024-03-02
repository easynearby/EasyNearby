package com.changeworld.offternetpoc.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.changeworld.offternetpoc.Message
import com.changeworld.offternetpoc.databinding.FragmentMessagesDialogBinding


class MessagesDialog : DialogFragment() {

    private var _binding: FragmentMessagesDialogBinding? = null
    private val binding get() = _binding!!

    private val messages: MutableList<Message> = mutableListOf()

    private var sendMessageListener: ((String) -> Unit)? = null

    fun setSendMessageListener(listener: (String) -> Unit) {
        sendMessageListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
    }

    fun addMessage(message: Message) {
        messages.add(message)
        if (isVisible) {
            appendMessage(message)
        }
    }

    fun setMessages(messages: List<Message>) {
        with(this.messages) {
            clear()
            addAll(messages)
        }
        if (isVisible) {
            printMessages()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        printMessages()
        binding.sendBtn.setOnClickListener {
            with(binding.messageInputEdtxt.text.toString()) {
                if (isBlank()) {
                    Toast.makeText(requireContext(), "Error: Empty message", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                sendMessageListener?.invoke(this)?.also {
                    binding.messageInputEdtxt.text?.clear()
                }
            }
        }
    }

    private fun printMessages() {
        messages.forEach {
            appendMessage(it)
        }
    }

    private fun appendMessage(message: Message) {
        binding.messagesTv.append("\n${message.from}: ${message.message}")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}