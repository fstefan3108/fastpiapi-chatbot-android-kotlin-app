package com.stefan.chatbotapp.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stefan.chatbotapp.ui.theme.*

@Composable
fun DashboardScreen(
    onNavigateToChat: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var websiteToDelete by remember {
        mutableStateOf<com.stefan.chatbotapp.data.models.WebsiteResponse?>(null)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(52.dp)) }

            // Header
            item {
                Text(
                    text = "My Chatbots",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manage your websites and API keys",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Stats
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(BackgroundDark)
                        .border(1.dp, Border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Column {
                        Text(
                            uiState.websites.size.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Websites", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Section header
            if (uiState.isAddingWebsite) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundDark)
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Scraping website, this may take a minute...",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Loading
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }

            // Error
            uiState.errorMessage?.let { error ->
                item {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }

            // Empty state
            if (!uiState.isLoading && uiState.websites.isEmpty() && uiState.errorMessage == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BackgroundDark)
                            .border(1.dp, Border, RoundedCornerShape(16.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No websites yet", color = TextSecondary, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("Add your first website", color = Primary)
                            }
                        }
                    }
                }
            }

            // ── RecyclerView replaces the old items(uiState.websites) block ──
            if (uiState.websites.isNotEmpty()) {
                item {
                    AndroidView(
                        factory = { context ->
                            RecyclerView(context).apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = WebsiteAdapter(emptyList()) { website ->
                                    websiteToDelete = website
                                    showDeleteDialog = true
                                }
                                // Disable RecyclerView's own scrolling — LazyColumn handles it
                                isNestedScrollingEnabled = false
                            }
                        },
                        update = { recyclerView ->
                            (recyclerView.adapter as WebsiteAdapter).updateData(uiState.websites)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState.websites.isNotEmpty()) {
                item {
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        enabled = !uiState.isAddingWebsite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Another Website",
                            color = Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Chatbot button
            item {
                Button(
                    onClick = onNavigateToChat,
                    enabled = uiState.websites.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = White,
                        disabledContainerColor = Border,
                        disabledContentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "💬  Open Chatbot",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && websiteToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                websiteToDelete = null
            },
            containerColor = BackgroundDark,
            title = { Text("Delete Website", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete ${websiteToDelete!!.title}?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    websiteToDelete?.let { viewModel.deleteWebsite(it.id) }
                    showDeleteDialog = false
                    websiteToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    websiteToDelete = null
                }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showAddDialog) {
        AddWebsiteDialog(
            isLoading = uiState.isAddingWebsite,
            errorMessage = uiState.addWebsiteError,
            onConfirm = { url -> viewModel.addWebsite(url) { showAddDialog = false } },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun AddWebsiteDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Add Website",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    "Enter the URL of the website to scrape and index.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://example.com", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Primary,
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(url) },
                enabled = url.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}