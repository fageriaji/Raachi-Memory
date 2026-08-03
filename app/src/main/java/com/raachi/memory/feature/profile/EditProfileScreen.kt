package com.raachi.memory.feature.profile

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raachi.memory.R
import com.raachi.memory.core.ui.ProfileForm
import com.raachi.memory.core.ui.ProfileAvatar
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var photoError by remember { mutableStateOf(false) }
    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { cropped ->
                photoError = false
                viewModel.updateProfilePhoto(cropped.toString())
            }
        } else if (result.resultCode == ProfileCropActivity.RESULT_CROP_FAILED) {
            photoError = true
        }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { selected ->
            cropLauncher.launch(
                Intent(context, ProfileCropActivity::class.java).setData(selected),
            )
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_personal_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                ProfilePhotoEditor(
                    name = uiState.input.name,
                    photoUri = uiState.input.profilePhotoUri,
                    hasError = photoError,
                    onChoosePhoto = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    onRemovePhoto = {
                        photoError = false
                        viewModel.updateProfilePhoto(null)
                    },
                )

                ProfileForm(
                    input = uiState.input,
                    errors = uiState.errors,
                    onNameChanged = viewModel::updateName,
                    onDateOfBirthChanged = viewModel::updateDateOfBirth,
                    onMobileChanged = viewModel::updateMobile,
                    onGenderChanged = viewModel::updateGender,
                    onEmailChanged = viewModel::updateEmail,
                    showHealthFields = true,
                    onHeightChanged = viewModel::updateHeight,
                    onWeightChanged = viewModel::updateWeight,
                )

                if (uiState.saveFailed) {
                    Text(
                        text = stringResource(R.string.save_failed),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = !uiState.isSaving,
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.save_changes))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePhotoEditor(
    name: String,
    photoUri: String?,
    hasError: Boolean,
    onChoosePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileAvatar(name = name, photoUri = photoUri, size = 112.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onChoosePhoto) {
                Icon(Icons.Outlined.AddAPhoto, contentDescription = null)
                Text(
                    text = stringResource(
                        if (photoUri == null) R.string.choose_profile_photo else R.string.change_profile_photo,
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (photoUri != null) {
                OutlinedButton(onClick = onRemovePhoto) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                    Text(stringResource(R.string.remove_profile_photo), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        Text(
            text = stringResource(R.string.profile_photo_crop_support),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        if (hasError) {
            Text(
                text = stringResource(R.string.profile_photo_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
