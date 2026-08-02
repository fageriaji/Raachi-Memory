package com.raachi.memory.feature.ledger

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.R
import com.raachi.memory.core.ui.NotificationPermissionControls
import com.raachi.memory.core.ui.LedgerMainColor
import com.raachi.memory.domain.model.LedgerDirection
import com.raachi.memory.domain.model.LedgerInput
import com.raachi.memory.domain.model.LedgerKind
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerEditorScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LedgerEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onSaved()
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_ledger_entry)) },
            text = { Text(stringResource(R.string.delete_ledger_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.input.id == 0L) R.string.new_ledger else R.string.edit_ledger)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (state.input.id != 0L) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete_ledger_entry))
                        }
                    }
                    TextButton(onClick = viewModel::save, enabled = !state.isSaving) {
                        if (state.isSaving) CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                        else Text(stringResource(R.string.save_changes), color = LedgerMainColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = LedgerMainColor,
                    titleContentColor = LedgerMainColor,
                ),
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
        } else {
            LedgerEditorContent(
                state = state,
                onUpdate = viewModel::updateInput,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun LedgerEditorContent(
    state: LedgerEditorUiState,
    onUpdate: (LedgerInput.() -> LedgerInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    val input = state.input
    val context = LocalContext.current
    val contactPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contact = runCatching {
                result.data?.data?.let(context.contentResolver::readLedgerContact)
            }.getOrNull()
            if (contact != null) {
                onUpdate {
                    copy(
                        personName = contact.name.ifBlank { personName },
                        mobileNumber = contact.mobileNumber ?: mobileNumber,
                    )
                }
                if (contact.mobileNumber == null) {
                    Toast.makeText(context, R.string.contact_mobile_not_supported, Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, R.string.contact_read_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LedgerMainColor,
        focusedLabelColor = LedgerMainColor,
        cursorColor = LedgerMainColor,
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        NotificationPermissionControls()

        LedgerChoice(
            title = stringResource(R.string.entry_type),
            choices = LedgerKind.entries,
            selected = input.kind,
            label = { stringResource(if (it == LedgerKind.MONEY) R.string.money else R.string.item) },
            onSelected = { kind -> onUpdate { copy(kind = kind) } },
        )
        LedgerChoice(
            title = stringResource(R.string.direction),
            choices = LedgerDirection.entries,
            selected = input.direction,
            label = { stringResource(if (it == LedgerDirection.LENT) R.string.i_lent else R.string.i_borrowed) },
            onSelected = { direction -> onUpdate { copy(direction = direction) } },
        )

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (input.kind == LedgerKind.MONEY) {
                    OutlinedTextField(
                        value = input.amount,
                        onValueChange = { value ->
                            val clean = value.filter { it.isDigit() || it == '.' }
                            if (clean.count { it == '.' } <= 1) onUpdate { copy(amount = clean) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.amount_rupees)) },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = state.validation.amountError,
                        supportingText = if (state.validation.amountError) ({ Text(stringResource(R.string.invalid_amount)) }) else null,
                        colors = fieldColors,
                        singleLine = true,
                    )
                } else {
                    OutlinedTextField(
                        value = input.itemName,
                        onValueChange = { value -> onUpdate { copy(itemName = value) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.item_details)) },
                        placeholder = { Text(stringResource(R.string.item_details_example)) },
                        isError = state.validation.itemError,
                        supportingText = if (state.validation.itemError) ({ Text(stringResource(R.string.field_required)) }) else null,
                        colors = fieldColors,
                        singleLine = true,
                    )
                }
            }
        }

        OutlinedTextField(
            value = input.personName,
            onValueChange = { value -> onUpdate { copy(personName = value) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.person_name)) },
            isError = state.validation.personError,
            supportingText = if (state.validation.personError) ({ Text(stringResource(R.string.field_required)) }) else null,
            colors = fieldColors,
            singleLine = true,
        )
        OutlinedTextField(
            value = input.mobileNumber,
            onValueChange = { value -> onUpdate { copy(mobileNumber = value.filter(Char::isDigit).take(10)) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.mobile_optional)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = state.validation.mobileError,
            supportingText = if (state.validation.mobileError) ({ Text(stringResource(R.string.invalid_mobile)) }) else null,
            trailingIcon = {
                IconButton(
                    onClick = {
                        runCatching {
                            contactPicker.launch(
                                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI),
                            )
                        }.onFailure {
                            Toast.makeText(context, R.string.contact_picker_unavailable, Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Contacts,
                        contentDescription = stringResource(R.string.choose_contact),
                    )
                }
            },
            colors = fieldColors,
            singleLine = true,
        )

        LedgerDateField(
            title = stringResource(
                if (input.direction == LedgerDirection.LENT) R.string.lent_on else R.string.borrowed_on,
            ),
            date = input.transactionDate,
            onClick = {
                val initial = input.transactionDate
                DatePickerDialog(
                    context,
                    R.style.ThemeOverlay_RaachiMemory_LedgerDatePicker,
                    { _, year, month, day ->
                        val selected = LocalDate.of(year, month + 1, day)
                        onUpdate {
                            copy(
                                transactionDate = selected,
                                dueDate = dueDate?.takeUnless { it.isBefore(selected) },
                            )
                        }
                    },
                    initial.year,
                    initial.monthValue - 1,
                    initial.dayOfMonth,
                ).apply {
                    datePicker.maxDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }.show()
            },
        )

        LedgerDateField(
            title = stringResource(R.string.expected_back_on),
            date = input.dueDate,
            onClick = {
                val initial = input.dueDate ?: maxOf(LocalDate.now(), input.transactionDate)
                DatePickerDialog(
                    context,
                    R.style.ThemeOverlay_RaachiMemory_LedgerDatePicker,
                    { _, year, month, day -> onUpdate { copy(dueDate = LocalDate.of(year, month + 1, day)) } },
                    initial.year,
                    initial.monthValue - 1,
                    initial.dayOfMonth,
                ).apply {
                    datePicker.minDate = input.transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }.show()
            },
            onClear = { onUpdate { copy(dueDate = null) } },
        )

        OutlinedTextField(
            value = input.notes,
            onValueChange = { value -> onUpdate { copy(notes = value) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.ledger_notes)) },
            minLines = 3,
            colors = fieldColors,
        )
    }
}

@Composable
private fun <T> LedgerChoice(
    title: String,
    choices: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            choices.forEach { choice ->
                FilterChip(
                    selected = choice == selected,
                    onClick = { onSelected(choice) },
                    label = { Text(label(choice)) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LedgerMainColor,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                    ),
                )
            }
        }
    }
}

@Composable
private fun LedgerDateField(
    title: String,
    date: LocalDate?,
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = LedgerMainColor)
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    date?.format(DATE_FORMAT) ?: stringResource(R.string.select_date),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (date != null && onClear != null) {
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.clear_date), color = LedgerMainColor)
                }
            }
        }
    }
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu")

private data class LedgerContact(
    val name: String,
    val mobileNumber: String?,
)

private fun ContentResolver.readLedgerContact(uri: Uri): LedgerContact? = query(
    uri,
    arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
    ),
    null,
    null,
    null,
)?.use { cursor ->
    if (!cursor.moveToFirst()) return@use null
    val name = cursor.getString(
        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
    ).orEmpty().trim()
    val number = cursor.getString(
        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER),
    ).orEmpty()
    LedgerContact(name = name, mobileNumber = normalizeContactMobile(number))
}

internal fun normalizeContactMobile(number: String): String? {
    val digits = number.filter(Char::isDigit)
    val mobile = digits.takeLast(10)
    return mobile.takeIf { digits.length >= 10 && it.matches(Regex("[5-9][0-9]{9}")) }
}
