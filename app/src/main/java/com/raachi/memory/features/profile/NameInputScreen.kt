package com.raachi.memory.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.raachi.memory.R
import com.raachi.memory.ui.components.PrimaryButton

@Composable
fun NameInputScreen(
    onContinue: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.name_input_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { newValue ->
                    if (newValue.all { it.isLetter() || it.isWhitespace() }) {
                        name = newValue
                        isError = false
                    }
                },
                label = { Text(stringResource(R.string.your_name_hint)) },
                isError = isError,
                supportingText = {
                    if (isError) Text(stringResource(R.string.error_name_required))
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = stringResource(R.string.action_continue),
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        isError = true
                    } else {
                        onContinue(trimmedName)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}