package com.example.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatMessageEntity
import com.example.data.database.ChatSessionEntity
import com.example.data.database.FlashcardEntity
import com.example.data.database.QuizEntity
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryAqua
import com.example.ui.theme.SlateBlueDeep
import com.example.ui.viewmodel.ActiveQuiz
import com.example.ui.viewmodel.QuizQuestion
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StudyViewModel
import org.json.JSONArray

// Prebuilt light 1x1 PNG Base64 and descriptions to simulate high-fidelity camera studies
data class LensPreset(val title: String, val category: String, val base64: String, val info: String, val iconEmoji: String)

val PREBUILT_LENS_PRESETS = listOf(
    LensPreset(
        title = "Physics: Elastic Collision Graph",
        category = "Mechanics",
        base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
        info = "Deconstructs momentum transfer pre and post изолирован contact bounds.",
        iconEmoji = "📐"
    ),
    LensPreset(
        title = "Calculus: Definite Integral Area",
        category = "Analysis",
        base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
        info = "Solves definite integral under a parabolic graph curves limits step-by-step.",
        iconEmoji = "📊"
    ),
    LensPreset(
        title = "Biochemistry: Mitochondria Map",
        category = "Citology",
        base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
        info = "Deconstructs Krebs Cycle and ATP chemical extraction inside cristae folds.",
        iconEmoji = "🧬"
    )
)

data class CustomTabItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorexMainLayout(viewModel: StudyViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(Color(0xFFD0BCFF), Color(0xFF381E72))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            )
                        }
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "Corex AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = 18.sp
                                )
                            )
                            Text(
                                text = if (viewModel.selectedModel == "Pro") "GEMINI PRO LINKED" else "GEMINI NANO ACTIVE",
                                style = androidx.compose.ui.text.TextStyle(
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // High fidelity streak badge utilizing emojis
                        BadgeIndicatorEmoji(emoji = "🔥", label = "${viewModel.userStreakDays}d")
                        BadgeIndicatorEmoji(emoji = "🏆", label = "${viewModel.totalPoints} XP")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            StudyBottomNavigationBar(
                currentScreen = viewModel.currentScreen,
                onScreenSelected = { viewModel.currentScreen = it }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (viewModel.currentScreen) {
                Screen.Dashboard -> DashboardScreen(viewModel)
                Screen.AIChat -> AIChatScreen(viewModel)
                Screen.SmartLens -> SmartLensScreen(viewModel)
                Screen.VoiceTeacher -> VoiceTeacherScreen(viewModel)
                Screen.AdaptiveQuizzer -> AdaptiveQuizzerScreen(viewModel)
                Screen.ErrorHandling -> ErrorHandlingScreen(viewModel)
            }
        }
    }
}

@Composable
fun StudyBottomNavigationBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF211F26),
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val items = listOf(
            CustomTabItem(Screen.Dashboard, Icons.Default.Home, "Home"),
            CustomTabItem(Screen.AIChat, Icons.Default.Send, "AI Chat"),
            CustomTabItem(Screen.SmartLens, Icons.Default.Search, "Smart Lens"),
            CustomTabItem(Screen.VoiceTeacher, Icons.Default.PlayArrow, "Oral Voice"),
            CustomTabItem(Screen.AdaptiveQuizzer, Icons.Default.Check, "Quizzer"),
            CustomTabItem(Screen.ErrorHandling, Icons.Default.Warning, "Diagnostics")
        )

        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFEADDFF),
                    selectedTextColor = Color(0xFFEADDFF),
                    indicatorColor = Color(0xFF4F378B),
                    unselectedIconColor = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                    unselectedTextColor = Color(0xFFCAC4D0).copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
fun BadgeIndicatorEmoji(emoji: String, label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}

// --- Dashboard Screen ---
@Composable
fun DashboardScreen(viewModel: StudyViewModel) {
    val quizzes by viewModel.allQuizzes.collectAsState()
    val flashcards by viewModel.allFlashcards.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Profile Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎓", fontSize = 28.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.userName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = viewModel.userFieldOfStudy,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.userName = if (viewModel.userName == "Future Scholar") "Dr. Harrison" else "Future Scholar"
                            viewModel.userFieldOfStudy = if (viewModel.userFieldOfStudy == "Computer Science") "Quantum Physics" else "Computer Science"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Cycle Profile", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            // Engine spec configuration banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Engine Core Selector",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = "Model: Gemini ${viewModel.selectedModel}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (viewModel.selectedModel == "Pro")
                            "Running in Cloud Mode (Gemini Pro): Advanced reasoning, math theorem deconstruction, multimodals and deep academic recitation."
                        else
                            "Running in On-Device Mode (Gemini Nano): Simulated swift localized offline-optimized flashcards and quick bullet summaries.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.selectedModel = "Nano" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (viewModel.selectedModel == "Nano") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, if (viewModel.selectedModel == "Nano") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        ) {
                            Text("Gemini Nano", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.selectedModel = "Pro" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (viewModel.selectedModel == "Pro") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, if (viewModel.selectedModel == "Pro") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        ) {
                            Text("Gemini Pro", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Academic Modules",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            // 1. Primary Action: Smart Lens (Deep Purplish Brand Theme)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.currentScreen = Screen.SmartLens },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF381E72)
                ),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Smart Lens Study",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEADDFF)
                            )
                        )
                        Text(
                            text = "Point & scan textbook pages for instant Gemini breakdown.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFD0BCFF),
                                lineHeight = 14.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFD0BCFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Smart Lens",
                            tint = Color(0xFF381E72),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        item {
            // 2. High Density stats and tools side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    iconEmoji = "🧠",
                    title = "Quizzer Challenge",
                    desc = "Solve adaptive tests",
                    color = Color(0xFFF97316),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.currentScreen = Screen.AdaptiveQuizzer }
                )
                QuickActionCard(
                    iconEmoji = "💬",
                    title = "AI Companion",
                    desc = "Free-form companion lesson",
                    color = Color(0xFF00AA90),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.currentScreen = Screen.AIChat }
                )
            }
        }

        item {
            // 3. Voice Teacher High density active wave shortcut
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.currentScreen = Screen.VoiceTeacher },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2B2930)
                ),
                border = BorderStroke(1.dp, Color(0xFF49454F))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4F378B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Voice Teacher Mic",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voice Teacher",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Interactive hands-free mode active.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF938F99)
                            )
                        )
                    }
                    // Sound wave visualizer micro-widget (3 reactive animated bars)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(20.dp)
                    ) {
                        Box(modifier = Modifier.size(3.dp, 10.dp).clip(CircleShape).background(Color(0xFFD0BCFF)))
                        Box(modifier = Modifier.size(3.dp, 18.dp).clip(CircleShape).background(Color(0xFFD0BCFF)))
                        Box(modifier = Modifier.size(3.dp, 7.dp).clip(CircleShape).background(Color(0xFFD0BCFF)))
                    }
                }
            }
        }

        item {
            // Stats & streaks metrics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricWidget(value = "${quizzes.size}", descriptor = "Tests Created")
                    VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    MetricWidget(value = "${flashcards.size}", descriptor = "Flashcards")
                    VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    val attempts = quizzes.filter { it.attempted }
                    val avg = if (attempts.isNotEmpty()) attempts.map { it.score ?: 0 }.average().toInt() else 100
                    MetricWidget(value = "$avg%", descriptor = "Avg. Score")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Adaptive Tests Logs",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { viewModel.currentScreen = Screen.AdaptiveQuizzer }) {
                    Text("Manage Tests", fontSize = 12.sp)
                }
            }
        }

        if (quizzes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🧠", fontSize = 34.sp)
                        Text(
                            "No generated tests logged.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        )
                        Text(
                            "Head over to Quizzer to generate beautiful AI adaptive lessons!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(quizzes.take(3)) { quiz ->
                QuizItemCard(quiz = quiz, onDeleClicked = { viewModel.deleteQuizFromDb(quiz.id) }) {
                    try {
                        val arr = JSONArray(quiz.questionsJson)
                        val qList = mutableListOf<QuizQuestion>()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val qText = obj.getString("question")
                            val correctIdx = obj.getInt("correctIndex")
                            val explanation = obj.optString("explanation", "Correct Choice explanation")
                            val optArray = obj.getJSONArray("options")
                            val options = mutableListOf<String>()
                            for (j in 0 until optArray.length()) {
                                options.add(optArray.getString(j))
                            }
                            qList.add(QuizQuestion(qText, options, correctIdx, explanation))
                        }
                        viewModel.activeQuiz = ActiveQuiz(
                            topic = quiz.topic,
                            difficulty = quiz.difficulty,
                            questions = qList
                        )
                        viewModel.currentScreen = Screen.AdaptiveQuizzer
                    } catch (e: Exception) {
                        Log.e("CorexScreens", "Error loading quiz: ${e.message}")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickActionCard(
    iconEmoji: String,
    title: String,
    desc: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconEmoji, fontSize = 20.sp)
            }
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    maxLines = 2,
                    lineHeight = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MetricWidget(value: String, descriptor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = descriptor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun QuizItemCard(quiz: QuizEntity, onDeleClicked: () -> Unit, onStartClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = quiz.topic,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = quiz.difficulty,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (quiz.attempted) "Completed Score: ${quiz.score} XP" else "Adaptive Test Not Answered.",
                    fontSize = 11.sp,
                    color = if (quiz.attempted) SoftGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDeleClicked) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Test",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
                Button(
                    onClick = onStartClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (quiz.attempted) "Retake" else "Begin", fontSize = 11.sp)
                }
            }
        }
    }
}

// --- AI Chat Screen ---
@Composable
fun AIChatScreen(viewModel: StudyViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputMessageText by remember { mutableStateOf("") }
    var chatSessionTriggerTopic by remember { mutableStateOf("") }

    val activeSessions by viewModel.allChatSessions.collectAsState()
    val messagesList by viewModel.activeMessages.collectAsState()

    val listState = rememberLazyListState()
    LaunchedEffect(messagesList.size, viewModel.isGeneratingChat) {
        if (messagesList.isNotEmpty()) {
            listState.animateScrollToItem(messagesList.size - 1)
        }
    }

    var showSessionsHistoryMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Corex Companion Chat",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (viewModel.isGeneratingChat) AccentOrange else SoftGreen)
                            )
                            Text(
                                text = if (viewModel.isGeneratingChat) "Gemini is replying..." else "Ready for study prompt",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { showSessionsHistoryMenu = !showSessionsHistoryMenu }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Active sessions logs",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.clickable {
                                viewModel.selectedModel = if (viewModel.selectedModel == "Pro") "Nano" else "Pro"
                            }
                        ) {
                            Text(
                                text = "Engine: ${viewModel.selectedModel} ▼",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = showSessionsHistoryMenu) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Study Sessions Registers",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = chatSessionTriggerTopic,
                                onValueChange = { chatSessionTriggerTopic = it },
                                placeholder = { Text("Topic: e.g. Quantum Physics", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Button(
                                onClick = {
                                    if (chatSessionTriggerTopic.isNotBlank()) {
                                        viewModel.startNewChatSession(chatSessionTriggerTopic, viewModel.selectedModel)
                                        chatSessionTriggerTopic = ""
                                        showSessionsHistoryMenu = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("Add", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (activeSessions.isEmpty()) {
                            Text(
                                "No logs registered.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        } else {
                            activeSessions.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectChatSession(item.id)
                                            showSessionsHistoryMenu = false
                                        }
                                        .background(
                                            if (viewModel.currentSessionId == item.id) MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.08f
                                            ) else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = "📝", fontSize = 12.sp)
                                        Text(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (viewModel.currentSessionId == item.id) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.widthIn(max = 200.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteChatSession(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete log",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            if (messagesList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🧠", fontSize = 48.sp)
                            Text(
                                "Corex Academic Chat",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Submit formulas, coding errors or homework topics. Gemini will return comprehensive step-by-step notes.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(240.dp)
                            )
                        }
                    }
                }
            } else {
                items(messagesList) { bubble ->
                    ChatBubbleItem(bubble = bubble, onDiagnosticsClick = { viewModel.currentScreen = Screen.ErrorHandling })
                }
            }

            if (viewModel.isGeneratingChat) {
                item {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Corex is generating lesson...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputMessageText,
                    onValueChange = { inputMessageText = it },
                    placeholder = { Text("Ask your academic coach anything...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputMessageText.isNotBlank()) {
                            viewModel.sendMessage(inputMessageText)
                            inputMessageText = ""
                            keyboardController?.hide()
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                FloatingActionButton(
                    onClick = {
                        if (inputMessageText.isNotBlank()) {
                            viewModel.sendMessage(inputMessageText)
                            inputMessageText = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send prompt", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(bubble: ChatMessageEntity, onDiagnosticsClick: () -> Unit) {
    val isErrorConfig = bubble.message.contains("API Configuration Error") || 
            bubble.message.contains("API Error placeholder:") || 
            bubble.message.contains("Error contacting study assistant") ||
            bubble.message.contains("Error analyzing image") ||
            bubble.message.contains("API has returned an error")

    if (isErrorConfig) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C1B1B), // Dark crimson red for High Density aesthetics
                contentColor = Color(0xFFFFDAD6)
            ),
            border = BorderStroke(1.dp, Color(0xFFFFB4AA).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFFFB4AB).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Diagnostic warning",
                            tint = Color(0xFFFFB4AB),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Gemini API Connection Alert",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFFFB4AB)
                    )
                }

                Text(
                    text = bubble.message,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFFFFDAD6)
                )

                HorizontalDivider(color = Color(0xFFFFB4AB).copy(alpha = 0.2f), thickness = 1.dp)

                Text(
                    text = "Corex Companion could not establish communication with Gemini. Access the diagnostic repair room to execute automated selfkeys validation or connect active proxies.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFFFFDAD6).copy(alpha = 0.7f)
                )

                Button(
                    onClick = onDiagnosticsClick,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB4AB),
                        contentColor = Color(0xFF5E1214)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Go to diagnostics",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Resolve Setup Diagnostics", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val isModel = bubble.role == "model"
    val align = if (isModel) Alignment.Start else Alignment.End
    val containerC = if (isModel) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val textC = if (isModel) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(text = if (isModel) "☕" else "👤", fontSize = 12.sp)
            Text(
                text = if (isModel) "AI Tutor Corex" else "Scholar Student",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            color = containerC,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isModel) 0.dp else 16.dp,
                bottomEnd = if (isModel) 16.dp else 0.dp
            )
        ) {
            Text(
                text = bubble.message,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = textC
            )
        }
    }
}

// --- Smart Lens Screen (Textbook camera/diagram processor) ---
@Composable
fun SmartLensScreen(viewModel: StudyViewModel) {
    var cameraFlashlightOn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📸", fontSize = 20.sp)
            }
            Column {
                Text(
                    text = "Smart Lens Study",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Multimodal physics and math diagram problem solver",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Camera Simulated Viewfinder Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val laserOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 180f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val len = 24.dp.toPx()
                    val strokeW = 3.dp.toPx()
                    val tint = Color(0xFF00D4B2)

                    // Draw camera boundaries
                    drawLine(tint, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx() + len, 16.dp.toPx()), strokeWidth = strokeW, cap = StrokeCap.Round)
                    drawLine(tint, Offset(16.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx(), 16.dp.toPx() + len), strokeWidth = strokeW, cap = StrokeCap.Round)
                    drawLine(tint, Offset(w - 16.dp.toPx(), h - 16.dp.toPx()), Offset(w - 16.dp.toPx() - len, h - 16.dp.toPx()), strokeWidth = strokeW, cap = StrokeCap.Round)
                    drawLine(tint, Offset(w - 16.dp.toPx(), h - 16.dp.toPx()), Offset(w - 16.dp.toPx(), h - 16.dp.toPx() - len), strokeWidth = strokeW, cap = StrokeCap.Round)

                    drawLine(
                        color = Color(0x9900D4B2),
                        start = Offset(10.dp.toPx(), laserOffset.dp.toPx()),
                        end = Offset(w - 10.dp.toPx(), laserOffset.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "STUDY LENS MULTIMODAL",
                                color = Color(0xFF00D4B2),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(
                            onClick = { cameraFlashlightOn = !cameraFlashlightOn },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                        ) {
                            Text(text = if (cameraFlashlightOn) "💡 ON" else "💡 OFF", color = Color.White, fontSize = 9.sp)
                        }
                    }

                    Text(
                        text = if (viewModel.isAnalyzingImage) "TRANSMITTING DIAGRAM TO GEMINI CLOUD..." else "ALIGN TEXTBOOK DIAGRAM OR PROBLEM BLOCK",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        Text(
            text = "Select a Preloaded Study Page to Solve:",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PREBUILT_LENS_PRESETS.forEachIndexed { idx, preset ->
                val isSelected = viewModel.selectedLensImageIndex == idx
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectLensPreset(idx, preset.title, preset.base64) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.DarkGray.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = preset.iconEmoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = preset.title.substringBefore(":"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = preset.info,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (viewModel.isAnalyzingImage) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                    Text("Deep Analyzing page using Gemini multimodal engine...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Deconstructing geometric limits and solving equations...", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else if (viewModel.smartLensAnalysisResult != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "✨", fontSize = 14.sp)
                            Text(
                                text = "Gemini Lens Solution Notes",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        TextButton(onClick = { viewModel.smartLensAnalysisResult = null; viewModel.selectedLensImageIndex = null }) {
                            Text("Clear Solution", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.smartLensAnalysisResult!!,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "📸", fontSize = 34.sp)
                    Text(
                        "No lens analysis active.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                    Text(
                        "Click on a physics, calculus, or biochem preloaded study page above to see Gemini break down physical graphs and formulas step-by-step!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}



// --- Voice Teacher Screen ---
@Composable
fun VoiceTeacherScreen(viewModel: StudyViewModel) {
    val waveformHeights = listOf(0.4f, 1.2f, 0.7f, 1.9f, 0.5f, 1.1f, 0.3f, 1.5f, 0.8f, 1.8f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎙️", fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Aural Voice Teacher",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Real-time bidirectional vocal study",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Text(
            text = "Select your Oral Coach Persona:",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val trainers = listOf("Prof. Clara (Science)", "Socrates (Philosophy)", "Oliver (Languages)")
            trainers.forEach { trainer ->
                val isSelected = viewModel.selectedVoiceTrainer == trainer
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectedVoiceTrainer = trainer },
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = trainer.first().toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = trainer.substringBefore(" ("),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = trainer.substringAfter("(").removeSuffix(")"),
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Voice Speaking animated waveforms card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val isSpeaking = viewModel.isVoiceSpeaking
                val waveformScale = viewModel.voiceWaveformHeight

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(80.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val spacing = w / (waveformHeights.size + 1)
                    val densVal = this.density // LocalDensity receiver wrapper

                    waveformHeights.forEachIndexed { index, baseHeight ->
                        val currentHeight = baseHeight * 20f * densVal * waveformScale
                        val startX = (index + 1) * spacing
                        val startY = (h / 2) - (currentHeight / 2)
                        val endY = (h / 2) + (currentHeight / 2)

                        drawLine(
                            color = if (isSpeaking) Color(0xFF10B981) else primaryColor.copy(alpha = 0.3f),
                            start = Offset(startX, startY),
                            end = Offset(startX, endY),
                            strokeWidth = 5f * densVal,
                            cap = StrokeCap.Round
                        )
                    }
                }

                if (viewModel.isGeneratingChat) {
                    CircularProgressIndicator(color = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                }
            }
        }

        OutlinedButton(
            onClick = { viewModel.toggleVoiceSpeakingSimulator() },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (viewModel.isVoiceSpeaking) Color(0xFF10B981).copy(alpha = 0.15f) else Color.Transparent,
                contentColor = Color(0xFF10B981)
            ),
            border = BorderStroke(2.dp, Color(0xFF10B981)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = if (viewModel.isVoiceSpeaking) "🔇 Mute Clara" else "🎙️ Speak to Tutor",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "Recite Oral Topic Presets:",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val prompts = listOf(
                "Explain Photosynthesis",
                "Explain String Hypothesis",
                "Explain Inflation Index"
            )
            prompts.forEach { recPrompt ->
                Button(
                    onClick = { viewModel.runVoiceTurn(recPrompt) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = recPrompt,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        minLines = 2,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Vocal Recitation Log",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                viewModel.voiceTranscriptsList.value.forEach { transcript ->
                    val isUser = transcript.startsWith("You:")
                    Text(
                        text = transcript,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                if (viewModel.isGeneratingChat) {
                    Text(
                        text = "Tutor is thinking...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// --- Adaptive Quizzer Screen ---
@Composable
fun AdaptiveQuizzerScreen(viewModel: StudyViewModel) {
    val activeQuiz = viewModel.activeQuiz
    val flashcards by viewModel.allFlashcards.collectAsState()

    var topicInputText by remember { mutableStateOf("") }
    var difficultySelected by remember { mutableStateOf("Intermediate") }
    var flashcardSearchSubject by remember { mutableStateOf("") }

    var tabIndexSelector by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TabRow(
            selectedTabIndex = tabIndexSelector,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = tabIndexSelector == 0, onClick = { tabIndexSelector = 0 }) {
                Text("Quizzes Logs", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = tabIndexSelector == 1, onClick = { tabIndexSelector = 1 }) {
                Text("Flashcards Deck", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
        }

        if (tabIndexSelector == 0) {
            if (activeQuiz == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF97316).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧠", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = "Adaptive Quizzer Engine",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Generate local dynamic mock tests using Gemini AI",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Instantiate AI Study Quiz",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )

                        OutlinedTextField(
                            value = topicInputText,
                            onValueChange = { topicInputText = it },
                            label = { Text("What academic topic shall we test?") },
                            placeholder = { Text("e.g. Photosynthesis, Trigonometry") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = "Cognitive Difficulty Level:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val difficultiesList = listOf("Beginner", "Intermediate", "Advanced")
                            difficultiesList.forEach { level ->
                                val isSelected = difficultySelected == level
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { difficultySelected = level },
                                    label = { Text(level, fontSize = 11.sp) }
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.generateAIQuiz(topicInputText, difficultySelected) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !viewModel.isGeneratingQuiz && topicInputText.isNotBlank(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (viewModel.isGeneratingQuiz) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Generating cognitive database...", fontSize = 13.sp)
                                }
                            } else {
                                Text("Generate AI Cognitive Test", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                val quizzesHistory by viewModel.allQuizzes.collectAsState()
                if (quizzesHistory.isNotEmpty()) {
                    Text(
                        "Test Center Histories",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        quizzesHistory.forEach { item ->
                            QuizItemCard(quiz = item, onDeleClicked = { viewModel.deleteQuizFromDb(item.id) }) {
                                try {
                                    val arr = JSONArray(item.questionsJson)
                                    val qList = mutableListOf<QuizQuestion>()
                                    for (i in 0 until arr.length()) {
                                        val obj = arr.getJSONObject(i)
                                        val qText = obj.getString("question")
                                        val correctIdx = obj.getInt("correctIndex")
                                        val explanation = obj.optString("explanation", "Correct Choice explanation")
                                        val optArray = obj.getJSONArray("options")
                                        val options = mutableListOf<String>()
                                        for (j in 0 until optArray.length()) {
                                            options.add(optArray.getString(j))
                                        }
                                        qList.add(QuizQuestion(qText, options, correctIdx, explanation))
                                    }
                                    viewModel.activeQuiz = ActiveQuiz(
                                        topic = item.topic,
                                        difficulty = item.difficulty,
                                        questions = qList
                                    )
                                } catch (e: Exception) {
                                    Log.e("CorexScreens", "Error breaking quiz: ${e.message}")
                                }
                            }
                        }
                    }
                }
            } else {
                val questions = activeQuiz.questions
                val index = activeQuiz.currentQuestionIndex

                if (activeQuiz.isFinished) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🏆", fontSize = 40.sp)
                            }

                            Text(
                                text = "Test Finished!",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Text(
                                "Excellent, you scored:",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            Text(
                                text = "${activeQuiz.userScore} XP",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            Text(
                                text = "Topic: ${activeQuiz.topic} (${activeQuiz.difficulty})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.clearActiveQuiz() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back to Study Hub", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (index < questions.size) {
                    val question = questions[index]

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(onClick = { viewModel.clearActiveQuiz() }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Exit Quiz")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exit")
                            }
                        }

                        Text(
                            text = "Question ${index + 1} of ${questions.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Topic: ${activeQuiz.topic}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = question.question,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                lineHeight = 21.sp
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        question.options.forEachIndexed { optIndex, optionText ->
                            val isSelected = activeQuiz.selectedOptionIndex == optIndex
                            val isSubmitted = activeQuiz.answersSubmitted
                            val borderC = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            }
                            val bgC = if (isSubmitted) {
                                if (optIndex == question.correctIndex) {
                                    SoftGreen.copy(alpha = 0.15f)
                                } else if (isSelected) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            } else if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .clickable { viewModel.selectQuizOption(optIndex) },
                                border = BorderStroke(1.5.dp, borderC),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = bgC)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.1f
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val letterChar = ('A'.code + optIndex).toChar()
                                        Text(
                                            text = letterChar.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = optionText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (activeQuiz.answersSubmitted) {
                        val isCorrect = activeQuiz.selectedOptionIndex == question.correctIndex
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCorrect) SoftGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            ),
                            border = BorderStroke(1.dp, if (isCorrect) SoftGreen else MaterialTheme.colorScheme.error)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isCorrect) SoftGreen else MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = if (isCorrect) "Correct Response (+10 XP)" else "Incorrect Response",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = question.explanation,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.nextQuizQuestion() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next Question")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.submitQuizAnswer() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = activeQuiz.selectedOptionIndex != null
                        ) {
                            Text("Submit Response")
                        }
                    }
                }
            }
        } else {
            // FLASHCARDS SECTION
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00AA90).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💡", fontSize = 20.sp)
                }
                Column {
                    Text(
                        text = "Scholastic Flashcards",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Cognitive term recall and vocabulary reinforcement",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generate Flashcards via AI",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(text = "✨", fontSize = 14.sp)
                    }

                    OutlinedTextField(
                        value = viewModel.flashcardSubjectInput,
                        onValueChange = { viewModel.flashcardSubjectInput = it },
                        label = { Text("Topic: e.g. Quantum Spacing") },
                        placeholder = { Text("e.g. Photosynthesis, Trigonometry") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Button(
                        onClick = { viewModel.generateAIFlashcards(viewModel.flashcardSubjectInput) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !viewModel.isGeneratingFlashcard && viewModel.flashcardSubjectInput.isNotBlank()
                    ) {
                        if (viewModel.isGeneratingFlashcard) {
                            Text("Running cognitive analysis...")
                        } else {
                            Text("Generate 3 AI Flashcards")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    Text(
                        text = "Or create Flashcard Manually:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = viewModel.flashcardFrontInput,
                        onValueChange = { viewModel.flashcardFrontInput = it },
                        label = { Text("Front Side (Term / Question)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    OutlinedTextField(
                        value = viewModel.flashcardBackInput,
                        onValueChange = { viewModel.flashcardBackInput = it },
                        label = { Text("Back Side (Answer / Detail)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    Button(
                        onClick = {
                            viewModel.createFlashcardManual(
                                viewModel.flashcardFrontInput,
                                viewModel.flashcardBackInput,
                                viewModel.flashcardSubjectInput
                            )
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Manual", fontSize = 11.sp)
                    }
                }
            }

            OutlinedTextField(
                value = flashcardSearchSubject,
                onValueChange = { flashcardSearchSubject = it },
                label = { Text("Filter flashcards by topic") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            val filteredCards = flashcards.filter {
                flashcardSearchSubject.isBlank() || it.subject.contains(flashcardSearchSubject, ignoreCase = true)
            }

            if (filteredCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No study flashcards registered under filter.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredCards.forEach { card ->
                        FlashcardWidget(
                            card = card,
                            onToggleLearned = { viewModel.toggleFlashcardLearned(card) },
                            onDelete = { viewModel.deleteFlashcard(card.id) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FlashcardWidget(card: FlashcardEntity, onToggleLearned: () -> Unit, onDelete: () -> Unit) {
    var isRevealed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isRevealed = !isRevealed },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = card.subject.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleLearned, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (card.isLearned) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                            contentDescription = "Tag Learned",
                            tint = if (card.isLearned) SoftGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Flashcard",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = card.front,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedVisibility(visible = isRevealed) {
                Text(
                    text = card.back,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                )
            }

            if (!isRevealed) {
                Text(
                    text = "Tap Card to Reveal Answer",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ErrorHandlingScreen(viewModel: StudyViewModel) {
    val systemLogs by viewModel.systemDiagnostics.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Diagnostics",
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "System Diagnostics Room",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Real-time key check, connection validator, and debugger console",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // 2. Secret Key Verification Card
        val keyText = com.example.data.network.GeminiApiClient.getApiKey()
        val hasKey = keyText.isNotEmpty() && keyText != "MY_GEMINI_API_KEY"

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1C24),
                contentColor = Color(0xFFE6E1E6)
            ),
            border = BorderStroke(1.dp, if (hasKey) Color(0xFF4CAF50).copy(alpha = 0.4f) else Color(0xFFF44336).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Credential Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (hasKey) Color(0xFF81C784) else Color(0xFFE57373)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (hasKey) Color(0xFF2E7D32).copy(alpha = 0.2f)
                                else Color(0xFFC62828).copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (hasKey) "DETECTED" else "UNCONFIGURED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasKey) Color(0xFF81C784) else Color(0xFFE57373)
                        )
                    }
                }

                Text(
                    text = if (hasKey) {
                        "An active API credential has been loaded from the build configurations: sk-proj-...${keyText.takeLast(4)}"
                    } else {
                        "Your Gemini API Key is missing. The application is operating in local simulation/offline mode. Connect an API Key to enable cloud models."
                    },
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (!hasKey) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2B2121), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "🔧 Setup Quick Start Guide:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB4AB)
                            )
                            Text(
                                "1. Open the Secrets panel in AI Studio.\n" +
                                "2. Configure key name: GEMINI_API_KEY\n" +
                                "3. Paste your Gemini developer key and compile the project.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = Color(0xFFFFDAD6)
                            )
                        }
                    }
                }
            }
        }

        // 3. Interactive Connection Validator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1C24)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Automated Loopback Validator",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Run a live ping connection query through Corex server endpoints to confirm if the cloud proxy is responsive.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.testGeminiConnection() },
                        enabled = !viewModel.isTestingConnection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        if (viewModel.isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pinging Server...", fontSize = 11.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Ping",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RUN CONNECTIVITY TEST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (viewModel.testConnectionResult != null) {
                        val isSuccess = viewModel.testConnectionResult!!.startsWith("SUCCESS")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Test flag",
                                tint = if (isSuccess) Color(0xFF81C784) else Color(0xFFE57373),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isSuccess) "PASS" else "FAIL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) Color(0xFF81C784) else Color(0xFFE57373)
                            )
                        }
                    }
                }

                viewModel.testConnectionResult?.let { result ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF141218), RoundedCornerShape(6.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = result,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (result.startsWith("SUCCESS")) Color(0xFF81C784) else Color(0xFFE57373),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 4. Debug Console Box
        Text(
            text = "Automated Diagnostic Logs",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F0E13)
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (systemLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Console quiet. No active transactions.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(systemLogs) { log ->
                        val logColor = when (log.type) {
                            "ERROR" -> Color(0xFFF87171)
                            "WARNING" -> Color(0xFFFBBF24)
                            else -> Color(0xFF34D399)
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(1.dp, Color.White.copy(alpha = 0.03f)),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(logColor, CircleShape)
                                    )
                                    Text(
                                        text = "[${log.feature}]",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = logColor
                                    )
                                }
                                Text(
                                    text = log.timestamp,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            Text(
                                text = log.message,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFCAC4D0).copy(alpha = 0.9f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Common Error Reference Code Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1C24)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Troubleshooting Lookup Manual",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                listOf(
                    "HTTP Exception 403 (Forbidden)" to "API Key invalid. Click the Secrets panel in AI Studio, verify there are no spaces, and that the key is active.",
                    "HTTP Exception 400 (Bad Request)" to "The model name is deprecated or misformed. Re-deploy on the 3.5-flash alias.",
                    "Internet Offline / Host Unreachable" to "Enable mobile/Wi-Fi connection inside Android Emulator so dependencies can register outbound calls."
                ).forEach { (errorName, fixInfo) ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = errorName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = fixInfo,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
