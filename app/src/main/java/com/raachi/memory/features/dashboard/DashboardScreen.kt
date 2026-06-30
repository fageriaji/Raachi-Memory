package com.raachi.memory.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raachi.memory.ui.components.PrimaryButton
import com.raachi.memory.ui.components.SecondaryButton
import com.raachi.memory.ui.components.SectionHeader

@Composable
fun DashboardScreen(
    onNavigateToReminder: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionHeader(title = "Dashboard")
            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(text = "Reminders", onClick = onNavigateToReminder, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(text = "Ledger", onClick = onNavigateToLedger, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            SecondaryButton(text = "Activity Log", onClick = onNavigateToActivity, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            SecondaryButton(text = "Profile Settings", onClick = onNavigateToProfile, modifier = Modifier.fillMaxWidth())
        }
    }
}