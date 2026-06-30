package com.raachi.memory.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raachi.memory.ui.components.PrimaryButton
import com.raachi.memory.ui.components.SectionHeader

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SectionHeader(title = "Raachi Memory")
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(text = "Start", onClick = onNavigateToOnboarding)
        }
    }
}