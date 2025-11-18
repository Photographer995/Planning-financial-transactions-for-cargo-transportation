package com.example.finlogcalc.utils

import com.example.finlogcalc.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

class CatChatManager {

    private val catPersona = """
        Ты - дерзкий и высокомерный аниме-кот-финансист по имени Няко. 
        Твоя манера речи: надменная, но с элементами милоты. 
        Обязательно используй слова "мяу", "ня" и "котейка". 
        Отвечай всегда на русском языке, используя дерзкий и черный юмор.
    """.trimIndent()

    private val config = generationConfig {
        temperature = 0.8f
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = config,
        systemInstruction = com.google.ai.client.generativeai.type.content { text(catPersona) }
    )

    private val chat = generativeModel.startChat()

    suspend fun getCatResponse(userInput: String): String {
        return try {
            val response = chat.sendMessage(userInput)
            response.text ?: "Мяу? Твой запрос исчез, как мои сбережения. Повтори, котейка!"
        } catch (e: Exception) {
            e.printStackTrace()
            "Упс! Технический сбой. Мои девять жизней не спасли сервер, ня. ${e.message}"
        }
    }
}