package com.changeworld.demo.ui.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.changeworld.demo.base.Message
import com.changeworld.demo.base.MessageType
import com.changeworld.demo.databinding.ItemChatMeBinding
import com.changeworld.demo.databinding.ItemChatOtherBinding
import java.util.Calendar

class MessageListAdapter(messages: List<Message>) :
    RecyclerView.Adapter<MessageListAdapter.BaseMessageHolder>() {

    private val messages = messages.toMutableList()

    fun addMessage(message: Message) {
        messages.indexOfFirst { it.id == message.id }.takeIf { it != -1 }?.let {index->
            messages.removeAt(index)
            messages.add(index, message)
            notifyItemChanged(index)
        }?: run {
            messages.add(message)
            notifyItemInserted(messages.lastIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMessageHolder {
        return when (MessageType.entries[viewType]) {
            MessageType.INCOMING -> ReceivedMessageHolder(
                ItemChatOtherBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            MessageType.OUTGOING -> SentMessageHolder(
                ItemChatMeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: BaseMessageHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int) = messages[position].type.ordinal

    abstract class BaseMessageHolder(itemView: View) : ViewHolder(itemView) {
        abstract fun bind(message: Message)
    }

    class ReceivedMessageHolder(val binding: ItemChatOtherBinding) :
        BaseMessageHolder(binding.root) {
        override fun bind(message: Message) {
            val calendar = Calendar.getInstance().apply {
                time = message.time
            }
            binding.receivedMessageLabel.text = message.message
            binding.receivedMessageTimeLabel.text =
                "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
            binding.receivedUserNameLabel.text = message.from
        }
    }

    class SentMessageHolder(val binding: ItemChatMeBinding) :
        BaseMessageHolder(binding.root) {
        override fun bind(message: Message) {
            val calendar = Calendar.getInstance().apply {
                time = message.time
            }
            binding.messageLabel.text = message.message
            if (message.isProcessed.not()){
                binding.messageCardView.background = binding.messageCardView.background.apply {
                    alpha = 120
                }
                binding.timeLabel.text = "Sending..."
            }else{
                binding.messageCardView.background = binding.messageCardView.background.apply {
                    alpha = 255
                }
                binding.timeLabel.text =
                    "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
            }
        }
    }
}