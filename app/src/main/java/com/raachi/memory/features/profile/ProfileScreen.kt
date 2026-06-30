package com.raachi.memory.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raachi.memory.ui.components.EmptyState
import com.raachi.memory.ui.components.PrimaryButton
import com.raachi.memory.ui.components.SecondaryButton
import com.raachi.memory.ui.components.SectionHeader

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionHeader(title = "Profile")
            Spacer(modifier = Modifier.height(16.dp))
            EmptyState(message = "Profile details will appear here.")
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(text = "Settings", onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(text = "Back", onClick = onNavigateBack, modifier = Modifier.fillMaxWidth())
        }
    }
}