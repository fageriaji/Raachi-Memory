package com.raachi.memory.core.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.raachi.memory.R
import com.raachi.memory.domain.model.Gender
import com.raachi.memory.domain.model.ProfileField
import com.raachi.memory.domain.model.ProfileInput
import com.raachi.memory.domain.model.ProfileValidationError
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProfileForm(
    input: ProfileInput,
    errors: Map<ProfileField, ProfileValidationError>,
    onNameChanged: (String) -> Unit,
    onDateOfBirthChanged: (LocalDate?) -> Unit,
    onMobileChanged: (String) -> Unit,
    onGenderChanged: (Gender?) -> Unit,
    onEmailChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    showHealthFields: Boolean = false,
    onHeightChanged: (String) -> Unit = {},
    onWeightChanged: (String) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        OutlinedTextField(
            value = input.name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.name_label)) },
            placeholder = { Text(stringResource(R.string.name_placeholder)) },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            isError = ProfileField.NAME in errors,
            supportingText = errors[ProfileField.NAME]?.let { error ->
                { ValidationErrorText(error) }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
        )

        DateOfBirthField(
            value = input.dateOfBirth,
            error = errors[ProfileField.DATE_OF_BIRTH],
            onValueChanged = onDateOfBirthChanged,
        )

        OutlinedTextField(
            value = input.mobile,
            onValueChange = onMobileChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.mobile_label)) },
            placeholder = { Text(stringResource(R.string.mobile_placeholder)) },
            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
            isError = ProfileField.MOBILE in errors,
            supportingText = errors[ProfileField.MOBILE]?.let { error ->
                { ValidationErrorText(error) }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
        )

        GenderField(
            selectedGender = input.gender,
            onGenderChanged = onGenderChanged,
        )

        OutlinedTextField(
            value = input.email,
            onValueChange = onEmailChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.email_label)) },
            placeholder = { Text(stringResource(R.string.email_placeholder)) },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            isError = ProfileField.EMAIL in errors,
            supportingText = errors[ProfileField.EMAIL]?.let { error ->
                { ValidationErrorText(error) }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = if (showHealthFields) ImeAction.Next else ImeAction.Done,
            ),
        )

        if (showHealthFields) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                OutlinedTextField(
                    value = input.heightCm,
                    onValueChange = onHeightChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.height_label)) },
                    placeholder = { Text(stringResource(R.string.height_placeholder)) },
                    isError = ProfileField.HEIGHT in errors,
                    supportingText = errors[ProfileField.HEIGHT]?.let { error ->
                        { ValidationErrorText(error) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                )
                OutlinedTextField(
                    value = input.weightKg,
                    onValueChange = onWeightChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.weight_label)) },
                    placeholder = { Text(stringResource(R.string.weight_placeholder)) },
                    isError = ProfileField.WEIGHT in errors,
                    supportingText = errors[ProfileField.WEIGHT]?.let { error ->
                        { ValidationErrorText(error) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                )
            }
        }
    }
}

@Composable
private fun DateOfBirthField(
    value: LocalDate?,
    error: ProfileValidationError?,
    onValueChanged: (LocalDate?) -> Unit,
) {
    val context = LocalContext.current
    val initialDate = value ?: LocalDate.now().minusYears(18)
    val showDatePicker = {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onValueChanged(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth,
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.date_of_birth_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Surface(
            onClick = showDatePicker,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = if (error == null) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.error
                },
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = value?.format(DATE_OF_BIRTH_FORMAT)
                        ?: stringResource(R.string.select_date),
                    color = if (value == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (value != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onValueChanged(null) }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.clear_date),
                        )
                    }
                }
            }
        }
        error?.let { ValidationErrorText(it) }
    }
}

@Composable
private fun GenderField(
    selectedGender: Gender?,
    onGenderChanged: (Gender?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.gender_label),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Gender.entries.forEach { gender ->
                FilterChip(
                    selected = selectedGender == gender,
                    onClick = {
                        onGenderChanged(gender.takeUnless { selectedGender == it })
                    },
                    label = { Text(stringResource(gender.labelResource)) },
                )
            }
        }
    }
}

private val Gender.labelResource: Int
    get() = when (this) {
        Gender.MALE -> R.string.gender_male
        Gender.FEMALE -> R.string.gender_female
        Gender.OTHER -> R.string.gender_other
    }

private val DATE_OF_BIRTH_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu", Locale.ROOT)

@Composable
private fun ValidationErrorText(error: ProfileValidationError) {
    Text(
        text = stringResource(error.messageResource),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
    )
}

private val ProfileValidationError.messageResource: Int
    get() = when (this) {
        ProfileValidationError.REQUIRED -> R.string.field_required
        ProfileValidationError.FUTURE_DATE -> R.string.future_date_error
        ProfileValidationError.INVALID_MOBILE -> R.string.invalid_mobile
        ProfileValidationError.INVALID_EMAIL -> R.string.invalid_email
        ProfileValidationError.INVALID_HEIGHT -> R.string.invalid_height
        ProfileValidationError.INVALID_WEIGHT -> R.string.invalid_weight
    }
