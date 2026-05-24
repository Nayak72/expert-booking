package com.expertconnect.app.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expertconnect.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ExpertProfileViewModel = hiltViewModel()
) {
    val bio by viewModel.bio.collectAsState()
    val expertise by viewModel.expertise.collectAsState()
    val pricing by viewModel.pricing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null || errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expert Profile", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Update Your Expert Details",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Column {
                    OutlinedTextField(
                        value = expertise,
                        onValueChange = { viewModel.expertise.value = it },
                        label = { Text("Expertise (e.g., Software Engineer)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryTeal,
                            unfocusedBorderColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Column {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { viewModel.bio.value = it },
                        label = { Text("Bio", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryTeal,
                            unfocusedBorderColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Column {
                    OutlinedTextField(
                        value = if (pricing == 0.0) "" else pricing.toString(),
                        onValueChange = { viewModel.pricing.value = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Pricing (USD per hour)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryTeal,
                            unfocusedBorderColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            item {
                Column {
                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save Expert Profile", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Column {
                    if (successMessage != null) {
                        Text(text = successMessage!!, color = SecondaryTeal, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = ErrorRed, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
