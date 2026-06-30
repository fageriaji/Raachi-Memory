package com.raachi.memory.features.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.raachi.memory.features.profile.viewmodel.SplashState
import com.raachi.memory.features.profile.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToWelcome: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Simple fade-in animation
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "SplashAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(state) {
        when (state) {
            is SplashState.GoToDashboard -> onNavigateToDashboard()
            is SplashState.GoToWelcome -> onNavigateToWelcome()
            is SplashState.Loading -> { /* Wait */ }
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Raachi Memory",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(alphaAnim)
                )
            }
        }
    }
}