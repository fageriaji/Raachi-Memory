package com.raachi.memory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raachi.memory.R

@Composable
fun ExpandableFab(
    onAddReminder: () -> Unit,
    onAddLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "FabRotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {

                // Add Reminder
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text = stringResource(R.string.add_reminder),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    SmallFloatingActionButton(
                        onClick = {
                            expanded = false
                            onAddReminder()
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.add_reminder)
                        )
                    }
                }

                // Add Ledger
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text = stringResource(R.string.add_ledger_entry),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    SmallFloatingActionButton(
                        onClick = {
                            expanded = false
                            onAddLedger()
                        }
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = stringResource(R.string.add_ledger_entry)
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                expanded = !expanded
            },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.expand_actions),
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}