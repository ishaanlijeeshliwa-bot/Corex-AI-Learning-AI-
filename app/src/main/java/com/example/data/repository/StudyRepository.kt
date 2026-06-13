package com.example.data.repository

import android.util.Log
import com.example.data.database.ChatMessageEntity
import com.example.data.database.ChatSessionEntity
import com.example.data.database.FlashcardEntity
import com.example.data.database.QuizEntity
import com.example.data.database.StudyDao
import com.example.data.network.Content
import com.example.data.network.GeminiApiClient
import com.example.data.network.GenerateContentRequest
import com.example.data.network.GenerationConfig
import com.example.data.network.InlineData
import com.example.data.network.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudyRepository(private val studyDao: StudyDao) {

    // --- Local DB Queries (Reactive Flows) ---
    val allQuizzes: Flow<List<QuizEntity>> = studyDao.getAllQuizzes()
    val allFlashcards: Flow<List<FlashcardEntity>> = studyDao.getAllFlashcards()
    val allChatSessions: Flow<List<ChatSessionEntity>> = studyDao.getAllChatSessions()

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> =
        studyDao.getMessagesForSession(sessionId)

    // --- DB Mutations ---
    suspend fun saveQuiz(quiz: QuizEntity): Long = withContext(Dispatchers.IO) {
        studyDao.insertQuiz(quiz)
    }

    suspend fun updateQuiz(quiz: QuizEntity) = withContext(Dispatchers.IO) {
        studyDao.updateQuiz(quiz)
    }

    suspend fun deleteQuiz(id: Int) = withContext(Dispatchers.IO) {
        studyDao.deleteQuizById(id)
    }

    suspend fun saveFlashcard(flashcard: FlashcardEntity): Long = withContext(Dispatchers.IO) {
        studyDao.insertFlashcard(flashcard)
    }

    suspend fun updateFlashcard(flashcard: FlashcardEntity) = withContext(Dispatchers.IO) {
        studyDao.updateFlashcard(flashcard)
    }

    suspend fun deleteFlashcard(id: Int) = withContext(Dispatchers.IO) {
        studyDao.deleteFlashcardById(id)
    }

    suspend fun createChatSession(title: String, mode: String): Long = withContext(Dispatchers.IO) {
        studyDao.insertChatSession(ChatSessionEntity(title = title, mode = mode))
    }

    suspend fun saveChatMessage(sessionId: Long, role: String, message: String): Long = withContext(Dispatchers.IO) {
        studyDao.insertChatMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                role = role,
                message = message
            )
        )
    }

    suspend fun deleteChatSession(sessionId: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteChatSession(sessionId)
        studyDao.deleteMessagesForSession(sessionId)
    }

    // --- Gemini Content Generation REST API Core Call ---
    suspend fun generateContent(
        prompt: String,
        model: String = "gemini-3.5-flash",
        systemInstruction: String? = null,
        responseMimeType: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = GeminiApiClient.getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("StudyRepository", "API Key is missing or default placeholder!")
            return@withContext "API Configuration Error: Your Gemini API Key is missing. Please enter your API key in the Secrets Panel in AI Studio to proceed."
        }

        val designConfig = if (responseMimeType != null) {
            GenerationConfig(responseMimeType = responseMimeType, temperature = 0.7f)
        } else {
            GenerationConfig(temperature = 0.7f)
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = designConfig,
            systemInstruction = systemInstruction?.let {
                Content(parts = listOf(Part(text = it)))
            }
        )

        try {
            val response = GeminiApiClient.service.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Empty response from Gemini."
        } catch (e: Exception) {
            Log.e("StudyRepository", "Error in Gemini text generation: ${e.message}", e)
            "Error contacting study assistant: ${e.message ?: "Unknown Exception"}"
        }
    }

    // --- Gemini Multimodal API Call (Smart Lens Study) ---
    suspend fun generateMultimodalContent(
        prompt: String,
        base64Image: String,
        mimeType: String = "image/jpeg",
        model: String = "gemini-3.5-flash"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = GeminiApiClient.getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Configuration Error: Your Gemini API Key is missing. Add it in AI Studio Secrets Panel."
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = mimeType, data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.4f)
        )

        try {
            val response = GeminiApiClient.service.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No breakdown generated for the image."
        } catch (e: Exception) {
            Log.e("StudyRepository", "Error in Gemini multimodal generation: ${e.message}", e)
            "Error analyzing image: ${e.message ?: "Unknown Exception"}"
        }
    }
}
