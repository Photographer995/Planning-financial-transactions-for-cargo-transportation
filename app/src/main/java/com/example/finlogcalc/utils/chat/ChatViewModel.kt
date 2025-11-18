package com.example.finlogcalc.utils.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finlogcalc.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun sendMessage(text: String) {
        // Add user message
        _messages.value = _messages.value + Message(text = text, isFromUser = true)

        // Simulate AI response
        viewModelScope.launch {
            val response = generativeModel.generateContent(text)
            _messages.value = _messages.value + Message(
                text = response.text ?: "Error",
                isFromUser = false
            )
        }
    }
}
