package com.raachi.memory.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raachi.memory.ui.components.EmptyState
import com.raachi.memory.ui.components.PrimaryButton
import com.raachi.memory.ui.components.SectionHeader

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            SectionHeader(title = "Onboarding")
            Spacer(modifier = Modifier.height(16.dp))

            // FIX: Added the 'title' parameter to EmptyState
            EmptyState(
                title = "Welcome",
                message = "Let's set up your profile."
            )

            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(text = "Complete Setup", onClick = onComplete, modifier = Modifier.fillMaxWidth())
        }
    }
}