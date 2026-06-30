package com.raachi.memory.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raachi.memory.ui.components.PrimaryButton
import com.raachi.memory.ui.components.SectionHeader

@Composable
fun NameInputScreen(
    onContinue: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            SectionHeader(title = "What should we call you?")

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name (Required)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Continue",
                onClick = { onContinue(name) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}