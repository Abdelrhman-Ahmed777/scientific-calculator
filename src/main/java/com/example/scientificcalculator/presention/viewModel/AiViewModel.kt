package com.example.scientificcalculator.presention.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scientificcalculator.domain.MessageData
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AiViewModel() : ViewModel() {


    private val GEMINI_API_KEY = "AIzaSyAZieoDfj61ALVMLHlh6RmX0TTHJ8fHfRM"

    private val generativeModel: GenerativeModel = GenerativeModel(
        "gemini-pro" ,
        GEMINI_API_KEY ,
    )

    val messageList by lazy {
        mutableListOf<MessageData>()
    }

    private val _messageList = MutableStateFlow<List<MessageData>>(emptyList())
    val messageListState: StateFlow<List<MessageData>> = _messageList.asStateFlow()


    @RequiresApi(Build.VERSION_CODES.O)
    fun onMessageSend(message: String) {
        viewModelScope.launch {
            try {
                val history = messageList.map { messageData ->
                    content(role = messageData.user) { text(messageData.text) }

                }
                val chat = generativeModel.startChat(
                    history = history
                )
                val userMessage = MessageData(
                    "user" ,
                    message ,
                    isSent = true ,
                    isDelivered = true ,
                    isSeen = true ,
                    isFav = false ,
                    sendingDate = LocalDate.now() ,
                )
                messageList.add(userMessage)
                _messageList.value += userMessage


                val response = chat.sendMessage(message)
                withContext(Dispatchers.IO) {
                    val modelMessage = MessageData(
                        "model" ,
                        response.text.toString() ,
                        isSent = true ,
                        isDelivered = true ,
                        isSeen = true ,
                        isFav = false ,
                        sendingDate = LocalDate.now()
                    )
                    messageList.add(modelMessage)
                    _messageList.value += modelMessage
                }
            } catch (e: Exception) {
                messageList.add(
                    MessageData(
                        "model" ,
                        "Error : ${e.message}" ,
                        isSent = true ,
                        isDelivered = true ,
                        isSeen = true ,
                        isFav = false ,
                        sendingDate = LocalDate.now()
                    )
                )
                _messageList.value += messageList
            }

        }
    }
}
