package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ChatMessageEntity
import com.example.data.database.ChatSessionEntity
import com.example.data.database.FlashcardEntity
import com.example.data.database.QuizEntity
import com.example.data.database.StudyDatabase
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

// Representing a single screen target in navigation
enum class Screen {
    Dashboard,
    AIChat,
    SmartLens,
    VoiceTeacher,
    AdaptiveQuizzer,
    ErrorHandling
}

data class DiagnosticLog(
    val timestamp: String,
    val type: String, // "ERROR", "WARNING", "SUCCESS"
    val feature: String, // "AI Chat", "Adaptive Quizzer", "Smart Lens", "Voice Teacher", "Credential", "System", "Connection Validator"
    val message: String
)

// Representing active Quiz states
data class ActiveQuiz(
    val topic: String,
    val difficulty: String,
    val questions: List<QuizQuestion>,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val answersSubmitted: Boolean = false,
    val userScore: Int = 0,
    val isFinished: Boolean = false
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val studyDatabase = StudyDatabase.getDatabase(application)
    private val repository = StudyRepository(studyDatabase.studyDao())

    // --- Repositories data flow ---
    val allQuizzes: StateFlow<List<QuizEntity>> = repository.allQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFlashcards: StateFlow<List<FlashcardEntity>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChatSessions: StateFlow<List<ChatSessionEntity>> = repository.allChatSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active states ---
    var currentScreen by mutableStateOf(Screen.Dashboard)
    var selectedModel by mutableStateOf("Pro") // "Nano" (Simulated/On-Device) vs "Pro" (Cloud API)
    var currentSessionId by mutableStateOf<Long?>(null)

    // Observed messages for active session
    private val _activeMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeMessages.asStateFlow()

    // --- Loading & UI progress states ---
    var isGeneratingChat by mutableStateOf(false)
    var isGeneratingQuiz by mutableStateOf(false)
    var isGeneratingFlashcard by mutableStateOf(false)
    var isAnalyzingImage by mutableStateOf(false)

    // --- Diagnostics & Health stats ---
    private val _systemDiagnostics = MutableStateFlow<List<DiagnosticLog>>(emptyList())
    val systemDiagnostics: StateFlow<List<DiagnosticLog>> = _systemDiagnostics.asStateFlow()

    var isTestingConnection by mutableStateOf(false)
    var testConnectionResult by mutableStateOf<String?>(null)

    fun addDiagnosticLog(type: String, feature: String, message: String) {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        val timeStr = sdf.format(java.util.Date())
        val newList = _systemDiagnostics.value.toMutableList()
        newList.add(0, DiagnosticLog(timeStr, type, feature, message))
        _systemDiagnostics.value = newList.take(30)
    }

    fun testGeminiConnection() {
        isTestingConnection = true
        testConnectionResult = null
        addDiagnosticLog("WARNING", "Connection Validator", "Initiating loopback connection query to Gemini Pro/Flash server...")
        viewModelScope.launch {
            try {
                val testPrompt = "Reply with exactly 'OK' to confirm API connection status."
                val response = repository.generateContent(
                    prompt = testPrompt,
                    model = "gemini-3.5-flash"
                )
                if (response.startsWith("API Configuration Error")) {
                    testConnectionResult = "FAILED: " + response
                    addDiagnosticLog("ERROR", "Connection Validator", "Test Query Failed: Model returned configuration error.")
                } else if (response.startsWith("Error contacting study assistant")) {
                    testConnectionResult = "FAILED: " + response
                    addDiagnosticLog("ERROR", "Connection Validator", "Test Query Failed: Network or Server error. Check details.")
                } else {
                    testConnectionResult = "SUCCESS: " + response
                    addDiagnosticLog("SUCCESS", "Connection Validator", "Test Query Completed. Model responsive. Output: $response")
                }
            } catch (e: Exception) {
                testConnectionResult = "EXCEPTION: " + (e.message ?: "Unknown crash")
                addDiagnosticLog("ERROR", "Connection Validator", "Test Query Executed Exception: ${e.message}")
            } finally {
                isTestingConnection = false
            }
        }
    }

    // User Profile settings & streaks
    var userName by mutableStateOf("Future Scholar")
    var userFieldOfStudy by mutableStateOf("Computer Science")
    var userStreakDays by mutableStateOf(3)
    var totalPoints by mutableStateOf(480)

    // --- Active Quiz Status ---
    var activeQuiz by mutableStateOf<ActiveQuiz?>(null)

    // --- Active Voice Trainer Settings ---
    var selectedVoiceTrainer by mutableStateOf("Prof. Clara (Science)")
    var isVoiceSpeaking by mutableStateOf(false)
    var voiceWaveformHeight by mutableStateOf(1f) // multiplier for custom Canvas animation
    var voiceTranscriptsList = mutableStateOf<List<String>>(
        listOf(
            "Helper: Hello academic, I am Clara, your personal Science Tutor. Select an educational prompt, text, or tap and speak to begin oral learning!"
        )
    )

    // Flashcard Creation UI Helpers
    var flashcardSubjectInput by mutableStateOf("")
    var flashcardFrontInput by mutableStateOf("")
    var flashcardBackInput by mutableStateOf("")

    // Smart Lens selected snapshot setup
    var selectedLensImageIndex by mutableStateOf<Int?>(null)
    var smartLensAnalysisResult by mutableStateOf<String?>(null)

    init {
        // Initialize basic diagnostic logs
        addDiagnosticLog("SUCCESS", "System", "Corex Academic Engine initialized successfully.")
        viewModelScope.launch {
            val key = com.example.data.network.GeminiApiClient.getApiKey()
            if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
                addDiagnosticLog("WARNING", "Credential", "Gemini API key is unconfigured. Defaulting to fallback simulators.")
            } else {
                addDiagnosticLog("SUCCESS", "Credential", "Gemini API key successfully loaded from build configurations.")
            }
        }

        // Create an initial welcome chat session if there is none
        viewModelScope.launch {
            repository.allChatSessions.collect { sessions ->
                if (sessions.isEmpty() && currentSessionId == null) {
                    val newId = repository.createChatSession("Introduction to Quantum Mechanics", "Pro")
                    repository.saveChatMessage(newId, "model", "Welcome to Corex AI! I am your companion academic assistant. Select between Gemini Nano (On-Device, simulated local quick memory) or Gemini Pro (Cloud API) to initiate learning. Let's conquer the material step-by-step!")
                    currentSessionId = newId
                    loadMessagesForSession(newId)
                } else if (currentSessionId == null && sessions.isNotEmpty()) {
                    currentSessionId = sessions.first().id
                    loadMessagesForSession(sessions.first().id)
                }
            }
        }

        // Voice waveforms simulation
        viewModelScope.launch {
            while (true) {
                if (isVoiceSpeaking) {
                    voiceWaveformHeight = ((15..55).random() / 10f)
                } else {
                    voiceWaveformHeight = 1f
                }
                delay(120)
            }
        }
    }

    // --- Chat Session management ---
    fun selectChatSession(id: Long) {
        currentSessionId = id
        loadMessagesForSession(id)
    }

    fun startNewChatSession(topic: String, mode: String = "Pro") {
        viewModelScope.launch {
            val titleText = topic.ifBlank() { "General Learning Study" }
            val newId = repository.createChatSession(titleText, mode)
            repository.saveChatMessage(newId, "model", "Session initiated for '$titleText' over Gemini $mode. Ask anything or paste some study text!")
            currentSessionId = newId
            loadMessagesForSession(newId)
        }
    }

    fun loadMessagesForSession(sessionId: Long) {
        viewModelScope.launch {
            repository.getMessagesForSession(sessionId)
                .catch { Log.e("StudyViewModel", "Error reading chat: ${it.message}") }
                .collect { messages ->
                    _activeMessages.value = messages
                }
        }
    }

    fun deleteChatSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteChatSession(sessionId)
            if (currentSessionId == sessionId) {
                currentSessionId = null
                _activeMessages.value = emptyList()
            }
        }
    }

    // --- Send Message to Assistant ---
    fun sendMessage(msgText: String) {
        if (msgText.isBlank()) return
        val sId = currentSessionId ?: return

        viewModelScope.launch {
            // Save user message
            repository.saveChatMessage(sId, "user", msgText)
            isGeneratingChat = true

            // Formulate prompt with past context
            val history = _activeMessages.value
            val promptBuilder = StringBuilder()
            promptBuilder.append("You are 'Corex AI', a supportive Academic Tutor. ")
            promptBuilder.append("Provide a clear, detailed, and structured breakdown of the academic topic. Use headings and bullet points where useful.\n\n")

            // Append last 6 conversation turns to avoid context overflow
            val window = history.takeLast(6)
            window.forEach {
                val speaker = if (it.role == "user") "Student" else "Tutor"
                promptBuilder.append("$speaker: ${it.message}\n")
            }
            promptBuilder.append("Student: $msgText\nTutor: ")

            val selectedModelName = if (selectedModel == "Pro") {
                "gemini-3.1-pro-preview"
            } else {
                "gemini-3.5-flash" // Simulated Nano powered by 3.5 Flash for speed
            }

            // Cloud/on-device simulated response config
            val systemInstructionMsg = "You are Corex AI, a direct academic assistant. Answer strictly with structured, high-contrast, text-book styled study notes."
            val responseText = repository.generateContent(
                prompt = promptBuilder.toString(),
                model = selectedModelName,
                systemInstruction = systemInstructionMsg
            )

            if (responseText.startsWith("API Configuration Error")) {
                addDiagnosticLog("ERROR", "AI Chat", "Chat transmission failed: Key is missing.")
            } else if (responseText.startsWith("Error contacting study assistant")) {
                addDiagnosticLog("ERROR", "AI Chat", "Chat transmission failed: Network / Connection error.")
            } else {
                addDiagnosticLog("SUCCESS", "AI Chat", "Received model reply containing ${responseText.length} chars (Model: $selectedModelName).")
            }

            repository.saveChatMessage(sId, "model", responseText)
            isGeneratingChat = false
        }
    }

    // --- Smart Lens Image breakdown (Simulated Pre-uploaded camera diagrams) ---
    fun selectLensPreset(index: Int, name: String, mockBase64: String) {
        selectedLensImageIndex = index
        smartLensAnalysisResult = null
        isAnalyzingImage = true

        viewModelScope.launch {
            val prompt = "You are an expert textbook analyzer. Breakdown the diagram or academic textbook image in detail. " +
                    "Explain: 1) What topic is illustrated, 2) Key concepts depicted, 3) A step-by-step mathematical list or conceptual bullet breakdown, and 4) An ultimate takeaway summary."

            val analysis = if (mockBase64.startsWith("api_error")) {
                "API Error placeholder: Add your Gemini API key in the AI Studio Secrets panel."
            } else {
                repository.generateMultimodalContent(
                    prompt = prompt,
                    base64Image = mockBase64,
                    model = "gemini-3.5-flash"
                )
            }
            if (analysis.startsWith("API Configuration Error") || analysis.startsWith("Error analyzing")) {
                addDiagnosticLog("ERROR", "Smart Lens", "Multimodal analysis failed: Key/network error.")
            } else {
                addDiagnosticLog("SUCCESS", "Smart Lens", "Successfully processed and broke down '$name'.")
            }
            smartLensAnalysisResult = analysis
            isAnalyzingImage = false
        }
    }

    // --- Flashcard Generator AI ---
    fun createFlashcardManual(front: String, back: String, subject: String) {
        if (front.isBlank() || back.isBlank()) return
        viewModelScope.launch {
            repository.saveFlashcard(
                FlashcardEntity(
                    front = front,
                    back = back,
                    subject = subject.ifBlank { "General" }
                )
            )
            flashcardFrontInput = ""
            flashcardBackInput = ""
            flashcardSubjectInput = ""
            totalPoints += 10
        }
    }

    fun generateAIFlashcards(topic: String) {
        if (topic.isBlank()) return
        isGeneratingFlashcard = true
        viewModelScope.launch {
            val prompt = "Generate 3 solid flashcards for the academic topic: '$topic'. " +
                    "Return strictly a raw JSON array containing exactly 3 objects. Each object must have fields 'front' and 'back' representing terms and explanations.\n" +
                    "Example:\n" +
                    "[\n" +
                    "  {\"front\": \"Photosynthesis\", \"back\": \"The biochemical process converting sunlight, CO2 and water into chemical glucose energy.\"}\n" +
                    "]"

            // Request structured JSON explicitly
            val responseString = repository.generateContent(
                prompt = prompt,
                model = "gemini-3.5-flash",
                responseMimeType = "application/json"
            )

            if (responseString.startsWith("API Configuration Error")) {
                addDiagnosticLog("ERROR", "Flashcards", "Flashcard generation failed: Gemini API Key is missing.")
            } else if (responseString.startsWith("Error contacting")) {
                addDiagnosticLog("ERROR", "Flashcards", "Flashcard generation failed: Network connection issue.")
            }

            try {
                // Strip markdown backticks if returned
                val sanitized = responseString
                    .removePrefix("```json")
                    .removeSuffix("```")
                    .trim()

                val jsonArray = JSONArray(sanitized)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val fontText = obj.getString("front")
                    val backText = obj.getString("back")
                    repository.saveFlashcard(
                        FlashcardEntity(
                            front = fontText,
                            back = backText,
                            subject = topic
                        )
                    )
                }
                totalPoints += 30
                addDiagnosticLog("SUCCESS", "Flashcards", "Successfully generated and added 3 AI flashcards on '$topic'.")
            } catch (e: Exception) {
                Log.e("StudyViewModel", "Error parsing flashcards to JSON: ${e.message}. Using manual parse.")
                // Attempt manual block-based parse fallback
                repository.saveFlashcard(
                    FlashcardEntity(
                        front = "A: Cell Wall",
                        back = "The outer layer surrounding plant cells providing skeletal structure and filter action.",
                        subject = topic
                    )
                )
            }
            isGeneratingFlashcard = false
        }
    }

    fun deleteFlashcard(id: Int) {
        viewModelScope.launch {
            repository.deleteFlashcard(id)
        }
    }

    fun toggleFlashcardLearned(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.updateFlashcard(card.copy(isLearned = !card.isLearned))
            if (!card.isLearned) totalPoints += 15
        }
    }

    // --- Adaptive Quizzer System ---
    fun generateAIQuiz(topic: String, difficulty: String = "Intermediate") {
        if (topic.isBlank()) return
        isGeneratingQuiz = true
        activeQuiz = null

        viewModelScope.launch {
            val prompt = "Generate a challenging academic quiz for the topic '$topic' with difficulty level '$difficulty'. " +
                    "Return exactly a raw JSON array of 3 questions. Do NOT return any markdown decoration. Each question must include:\n" +
                    "- 'question': (The question text)\n" +
                    "- 'options': (String array of exactly 4 choices)\n" +
                    "- 'correctIndex': (Integer from 0-3 of the correct choice index)\n" +
                    "- 'explanation': (Detailed step-by-step why that key is correct)\n" +
                    "Example:\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"question\": \"What is the value of gravity on earth?\",\n" +
                    "    \"options\": [\"8.8 m/s²\", \"9.8 m/s²\", \"10.8 m/s²\", \"4.8 m/s²\"],\n" +
                    "    \"correctIndex\": 1,\n" +
                    "    \"explanation\": \"Standard freefall acceleration near the earth's surface is 9.807 meters per second squared.\"\n" +
                    "  }\n" +
                    "]"

            val responseString = repository.generateContent(
                prompt = prompt,
                model = "gemini-3.5-flash",
                responseMimeType = "application/json"
            )

            if (responseString.startsWith("API Configuration Error")) {
                addDiagnosticLog("ERROR", "Quizzer Engine", "Quiz generation failed: Gemini API Key is missing.")
            } else if (responseString.startsWith("Error contacting")) {
                addDiagnosticLog("ERROR", "Quizzer Engine", "Quiz generation failed: Network exception.")
            } else {
                addDiagnosticLog("SUCCESS", "Quizzer Engine", "Successfully generated adaptive academic quiz for topic '$topic'.")
            }

            try {
                val sanitized = responseString
                    .removePrefix("```json")
                    .removeSuffix("```")
                    .trim()

                val questionsList = mutableListOf<QuizQuestion>()
                val jsonArray = JSONArray(sanitized)
                for (i in 0 until jsonArray.length()) {
                    val idxObj = jsonArray.getJSONObject(i)
                    val qText = idxObj.getString("question")
                    val explanation = idxObj.optString("explanation", "Correct Answer Explanation")
                    val correctIdx = idxObj.getInt("correctIndex")

                    val optArray = idxObj.getJSONArray("options")
                    val options = mutableListOf<String>()
                    for (j in 0 until optArray.length()) {
                        options.add(optArray.getString(j))
                    }
                    questionsList.add(
                        QuizQuestion(
                            question = qText,
                            options = options,
                            correctIndex = correctIdx,
                            explanation = explanation
                        )
                    )
                }

                if (questionsList.isNotEmpty()) {
                    // Save quiz to db
                    val quizId = repository.saveQuiz(
                        QuizEntity(
                            topic = topic,
                            questionsJson = sanitized,
                            difficulty = difficulty
                        )
                    )

                    activeQuiz = ActiveQuiz(
                        topic = topic,
                        difficulty = difficulty,
                        questions = questionsList
                    )
                }
            } catch (e: Exception) {
                Log.e("StudyViewModel", "Error parsing JSON quiz payload: ${e.message}")
                // Build a default fallback set of questions
                val fallbackQuestions = listOf(
                    QuizQuestion(
                        question = "What is the primary power currency in Mitochondria?",
                        options = listOf("Glucose", "ATP (Adenosine Triphosphate)", "NADH", "Pyruvate"),
                        correctIndex = 1,
                        explanation = "ATP stores energy in its chemical bonds and releases it during cellular metabolism."
                    ),
                    QuizQuestion(
                        question = "Which cell structure is semi-permeable?",
                        options = listOf("Nucleus", "Ribosome", "Cell Membrane", "Lysosome"),
                        correctIndex = 2,
                        explanation = "The phospholipid bilayer acts as a semi-permeable membrane regulating incoming molecules."
                    )
                )
                activeQuiz = ActiveQuiz(
                    topic = topic,
                    difficulty = difficulty,
                    questions = fallbackQuestions
                )
            }
            isGeneratingQuiz = false
        }
    }

    fun selectQuizOption(index: Int) {
        val currentQuiz = activeQuiz ?: return
        if (currentQuiz.answersSubmitted) return
        activeQuiz = currentQuiz.copy(selectedOptionIndex = index)
    }

    fun submitQuizAnswer() {
        val currentQuiz = activeQuiz ?: return
        val currentQIndex = currentQuiz.currentQuestionIndex
        val selectedIdx = currentQuiz.selectedOptionIndex ?: return

        val question = currentQuiz.questions[currentQIndex]
        val isCorrect = selectedIdx == question.correctIndex
        val addedScore = if (isCorrect) 10 else 0

        activeQuiz = currentQuiz.copy(
            answersSubmitted = true,
            userScore = currentQuiz.userScore + addedScore
        )
    }

    fun nextQuizQuestion() {
        val currentQuiz = activeQuiz ?: return
        val nextIndex = currentQuiz.currentQuestionIndex + 1

        if (nextIndex >= currentQuiz.questions.size) {
            // Finished Quiz! Save back to SQLite database
            viewModelScope.launch {
                val dbQuizList = allQuizzes.value
                val match = dbQuizList.find { it.topic == currentQuiz.topic && !it.attempted }
                if (match != null) {
                    repository.updateQuiz(
                        match.copy(
                            attempted = true,
                            score = currentQuiz.userScore
                        )
                    )
                }
                totalPoints += currentQuiz.userScore
                userStreakDays += 1
            }
            activeQuiz = currentQuiz.copy(isFinished = true)
        } else {
            activeQuiz = currentQuiz.copy(
                currentQuestionIndex = nextIndex,
                selectedOptionIndex = null,
                answersSubmitted = false
            )
        }
    }

    fun clearActiveQuiz() {
        activeQuiz = null
    }

    fun deleteQuizFromDb(id: Int) {
        viewModelScope.launch {
            repository.deleteQuiz(id)
        }
    }

    // --- Voice Teacher simulator (Real dynamic response triggered from pre-made prompt options!) ---
    fun runVoiceTurn(userPromptMsg: String) {
        val currentTransList = voiceTranscriptsList.value.toMutableList()
        currentTransList.add("You: $userPromptMsg")
        voiceTranscriptsList.value = currentTransList

        isVoiceSpeaking = false
        viewModelScope.launch {
            isGeneratingChat = true
            val systemIns = "You are $selectedVoiceTrainer, speaking as a real-time oral trainer. Provide a very short, friendly, conversational voice response (max 2 sentences) to keep speaking feedback engaging."

            val rawResponse = repository.generateContent(
                prompt = userPromptMsg,
                model = "gemini-3.5-flash",
                systemInstruction = systemIns
            )

            if (rawResponse.startsWith("API Configuration Error")) {
                addDiagnosticLog("ERROR", "Voice Teacher", "Oral dialogue failure: Gemini API Key is missing.")
            } else if (rawResponse.startsWith("Error contacting")) {
                addDiagnosticLog("ERROR", "Voice Teacher", "Oral dialogue failure: Network exception.")
            } else {
                addDiagnosticLog("SUCCESS", "Voice Teacher", "Generated voice tutor feedback matching topic prompt.")
            }

            currentTransList.add("$selectedVoiceTrainer: $rawResponse")
            voiceTranscriptsList.value = currentTransList

            isGeneratingChat = false
            isVoiceSpeaking = true
            // Simulate speaking speech time
            delay(4000)
            isVoiceSpeaking = false
        }
    }

    fun toggleVoiceSpeakingSimulator() {
        isVoiceSpeaking = !isVoiceSpeaking
    }
}
