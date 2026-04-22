package com.stefan.chatbotapp.ui.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stefan.chatbotapp.data.models.ChatResponse
import com.stefan.chatbotapp.data.models.WebsiteResponse
import com.stefan.chatbotapp.ui.theme.*

@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageInput by remember { mutableStateOf("") }
    var showWebsitePicker by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    // Auto-scroll when messages change or typing indicator appears
    LaunchedEffect(uiState.messages.size, uiState.isTyping) {
        val itemCount = uiState.messages.size + (if (uiState.isTyping) 1 else 0)
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .imePadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundDark)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.selectedWebsite?.title ?: "Select a website",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (uiState.selectedWebsite != null) {
                    Text(
                        text = uiState.selectedWebsite!!.url,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
            if (uiState.selectedWebsite != null) {
                TextButton(onClick = { showWebsitePicker = true }) {
                    Text("Switch", color = Primary, fontSize = 13.sp)
                }
            }
        }

        HorizontalDivider(color = Border, thickness = 1.dp)

        if (showWebsitePicker) {
            WebsitePicker(
                websites = uiState.websites,
                isLoading = uiState.isLoadingWebsites,
                onSelect = { website ->
                    viewModel.selectWebsite(website)
                    showWebsitePicker = false
                }
            )
        } else {
            // Messages area
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoadingSession -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Starting session...", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Empty state
                            if (uiState.messages.isEmpty() && !uiState.isTyping) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxWidth()
                                            .fillParentMaxHeight(0.8f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("💬", fontSize = 48.sp)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                "Ask anything about",
                                                color = TextSecondary,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                uiState.selectedWebsite?.title ?: "",
                                                color = Primary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            items(
                                items = uiState.messages,
                                key = { "${it.session_id}_${it.role}_${it.timestamp}_${it.message.take(10)}" }
                            ) { message ->
                                ChatBubble(message = message)
                            }

                            if (uiState.isTyping) {
                                item {
                                    TypingIndicator()
                                }
                            }
                        }
                    }
                }
            }

            // Error message
            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            HorizontalDivider(color = Border, thickness = 1.dp)

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Ask something...", color = TextSecondary, fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Primary,
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.width(8.dp))

                val canSend = messageInput.isNotBlank() &&
                        !uiState.isSending &&
                        uiState.sessionId != null

                IconButton(
                    onClick = {
                        if (canSend) {
                            viewModel.sendMessage(messageInput.trim())
                            messageInput = ""
                        }
                    },
                    enabled = canSend,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (canSend) Primary else Border)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (canSend) White else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatResponse) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(if (isUser) Primary else BackgroundDark)
                .then(
                    if (!isUser) Modifier.border(
                        1.dp,
                        Border,
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 18.dp
                        )
                    ) else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.message,
                color = if (isUser) White else TextPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                .background(BackgroundDark)
                .border(
                    1.dp,
                    Border,
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    BouncingDot(delayMillis = index * 150)
                }
            }
        }
    }
}

@Composable
fun BouncingDot(delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot_$delayMillis")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMillis)
        ),
        label = "bounce_$delayMillis"
    )

    Box(
        modifier = Modifier
            .size(7.dp)
            .offset(y = offsetY.dp)
            .clip(CircleShape)
            .background(TextSecondary)
    )
}

@Composable
fun WebsitePicker(
    websites: List<WebsiteResponse>,
    isLoading: Boolean,
    onSelect: (WebsiteResponse) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Select a website",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Choose which chatbot to talk to",
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (websites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundDark)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No websites found. Add one from the dashboard.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            websites.forEach { website ->
                Surface(
                    onClick = { onSelect(website) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = BackgroundDark,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Border, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = website.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = website.url,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}