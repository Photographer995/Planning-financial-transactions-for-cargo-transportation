package com.example.finlogcalc.utils.chat

import android.util.Log // Добавлен импорт Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finlogcalc.R
import com.example.finlogcalc.NeonColors
import com.google.ai.client.generativeai.GenerativeModel
import com.example.finlogcalc.BuildConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenNeon(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val chatBackgroundManager = remember { ChatBackgroundManager(context) }

    var messages by remember {
        mutableStateOf(emptyList<Message>())
    }
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var isTyping by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // Надежная инициализация selectedBackground
    var selectedBackground by remember {
        val initialBackground = chatBackgroundManager.getSelectedBackground()
        mutableStateOf(initialBackground.takeIf { it != 0 } ?: R.drawable.nyako)
    }

    // LaunchedEffect для сброса, если selectedBackground становится недействительным
    LaunchedEffect(selectedBackground) {
        if (selectedBackground == 0) {
            Log.w("ChatScreenNeon", "selectedBackground became 0, resetting to R.drawable.nyako")
            selectedBackground = R.drawable.nyako
            chatBackgroundManager.saveSelectedBackground(R.drawable.nyako)
        }
    }

    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-2.5-flash-lite",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    if (showDialog) {
        ChatBackgroundChangerDialog(
            onDismiss = { showDialog = false },
            onBackgroundSelected = {
                selectedBackground = it
                chatBackgroundManager.saveSelectedBackground(it)
            },
            currentBackground = selectedBackground
        )
    }

    val handleSendMessage = {
        if (userInput.text.isNotBlank()) {
            val userMessage = Message(text = userInput.text, isFromUser = true)
            messages = messages + userMessage
            userInput = TextFieldValue("")
            isTyping = true

            scope.launch {
                try {
                    val response = generativeModel.generateContent(userMessage.text)
                    val aiMessage = Message(text = response.text ?: "Произошла ошибка", isFromUser = false)
                    messages = messages + aiMessage
                } catch (e: Exception) {
                    // Handle error, e.g., show an error message
                    val errorMessage = Message(text = "Не удалось получить ответ: ${e.message}", isFromUser = false)
                    messages = messages + errorMessage
                } finally {
                    isTyping = false
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            ChatTopAppBarNeon(onBack = onNavigateBack, onSettingsClick = { showDialog = true })
        },
        bottomBar = {
            ChatInputFieldNeon(
                inputValue = userInput,
                onValueChange = { userInput = it },
                onSendMessage = handleSendMessage,
                isSending = isTyping
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Логирование значения перед вызовом painterResource
            Log.d("ChatScreenNeon", "Attempting to load background resource ID: $selectedBackground")
            Image(
                painter = painterResource(id = selectedBackground),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Changed from ContentScale.Crop to ContentScale.Fit
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 400.dp, bottom = 16.dp),
                state = listState
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }

                item {
                    AnimatedVisibility(
                        visible = isTyping,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        TypingIndicator()
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBarNeon(onBack: () -> Unit, onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(NeonColors.Slate900.copy(alpha = 0.8f))
            .border(1.dp, NeonColors.Cyan500.copy(alpha = 0.2f), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                ambientColor = NeonColors.Cyan500.copy(alpha = 0.1f),
                spotColor = NeonColors.Cyan500.copy(alpha = 0.1f)
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = NeonColors.Cyan400)
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(brush = Brush.horizontalGradient(NeonColors.CyanToPurpleGradient))) {
                        append(stringResource(R.string.chat_title))
                    }
                },
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF4ADE80), CircleShape)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFF4ADE80).copy(alpha = 0.8f),
                            spotColor = Color(0xFF4ADE80).copy(alpha = 0.8f)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.chat_online),
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonColors.Gray400
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = NeonColors.Cyan400)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, modifier: Modifier = Modifier) {
    val isUser = message.isFromUser

    // Deep Neon gradient for the message bubble background
    val bubbleBackgroundBrush = if (isUser) {
        Brush.horizontalGradient(
            colors = listOf(
                NeonColors.Purple500.copy(alpha = 0.9f),
                NeonColors.Pink500.copy(alpha = 0.9f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                NeonColors.Cyan400.copy(alpha = 0.9f),
                NeonColors.Purple400.copy(alpha = 0.9f)
            )
        )
    }

    // Bright border to enhance the neon effect
    val bubbleBorderBrush = if (isUser) {
        Brush.horizontalGradient(listOf(NeonColors.Purple400, NeonColors.Pink500))
    } else {
        Brush.horizontalGradient(listOf(NeonColors.Cyan400, NeonColors.Purple400))
    }

    // Intense shadow for a strong glow effect
    val shadowColor = if (isUser) {
        NeonColors.Purple500
    } else {
        NeonColors.Cyan500
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .shadow(
                    elevation = 40.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .background(bubbleBackgroundBrush)
                .border(
                    width = 2.dp,
                    brush = bubbleBorderBrush,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 200.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = NeonColors.Cyan500.copy(alpha = 0.2f),
                    spotColor = NeonColors.Cyan500.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NeonColors.Slate900.copy(alpha = 0.5f))
                    .border(1.dp, NeonColors.Cyan500.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.chat_typing_indicator),
                        color = NeonColors.Cyan400,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TypingAnimationDots()
                }
            }
        }
    }
}

@Composable
fun TypingAnimationDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots_transition")
    val dotOffsets = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0f at (index * 200) with EaseInOutCubic
                    8f at (300 + index * 200) with EaseInOutCubic
                    0f at (600 + index * 200) with EaseInOutCubic
                },
                repeatMode = RepeatMode.Restart
            ), label = "dot_offset_$index"
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dotOffsets.forEach { dotOffset ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = -dotOffset.value.dp)
                    .background(NeonColors.Cyan400, CircleShape)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputFieldNeon(
    inputValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSendMessage: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(NeonColors.Slate900.copy(alpha = 0.8f))
            .border(1.dp, NeonColors.Cyan500.copy(alpha = 0.2f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = NeonColors.Cyan500.copy(alpha = 0.1f),
                spotColor = NeonColors.Cyan500.copy(alpha = 0.1f)
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NeonColors.Slate900.copy(alpha = 0.5f))
                    .border(1.dp, NeonColors.Cyan500.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Mic, contentDescription = "Microphone", tint = NeonColors.Cyan400, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = inputValue,
                        onValueChange = onValueChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = NeonColors.Gray200),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        cursorBrush = SolidColor(NeonColors.Cyan400),
                        enabled = !isSending,
                        decorationBox = @Composable { innerTextField ->
                            if (inputValue.text.isEmpty()) {
                                Text(
                                    stringResource(R.string.chat_input_placeholder),
                                    color = NeonColors.Gray600,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Button(
                onClick = onSendMessage,
                enabled = inputValue.text.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(NeonColors.PurpleToPinkGradient))
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = NeonColors.Purple500.copy(alpha = 0.4f),
                        spotColor = NeonColors.Purple500.copy(alpha = 0.4f)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.cat_chat_send_button), tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}