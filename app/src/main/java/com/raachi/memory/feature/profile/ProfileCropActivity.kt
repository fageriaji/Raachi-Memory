package com.raachi.memory.feature.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.automirrored.outlined.RotateLeft
import androidx.compose.material.icons.automirrored.outlined.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canhub.cropper.CropImageView
import com.raachi.memory.R
import com.raachi.memory.ThemeViewModel
import com.raachi.memory.core.designsystem.theme.RaachiMemoryTheme
import com.raachi.memory.domain.model.ThemeMode
import com.raachi.memory.domain.security.AppLockManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ProfileCropActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    @Inject lateinit var appLockManager: AppLockManager
    private var lockedWhileStopped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val source = intent.data
        if (source == null) {
            setResult(RESULT_CROP_FAILED)
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            RaachiMemoryTheme(darkTheme = darkTheme) {
                ProfileCropScreen(
                    source = source,
                    onCancel = { finish() },
                    onCropped = { cropped ->
                        setResult(Activity.RESULT_OK, Intent().setData(cropped))
                        finish()
                    },
                    onFailed = {
                        setResult(RESULT_CROP_FAILED)
                        finish()
                    },
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && !isFinishing) {
            lockedWhileStopped = true
            appLockManager.lockSession()
        }
    }

    override fun onStart() {
        super.onStart()
        if (lockedWhileStopped) {
            lockedWhileStopped = false
            finish()
        }
    }

    companion object {
        const val RESULT_CROP_FAILED = Activity.RESULT_FIRST_USER + 12
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileCropScreen(
    source: Uri,
    onCancel: () -> Unit,
    onCropped: (Uri) -> Unit,
    onFailed: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var cropView by remember { mutableStateOf<CropImageView?>(null) }
    var isCropping by remember { mutableStateOf(false) }
    var cropFailed by remember { mutableStateOf(false) }

    DisposableEffect(cropView) {
        val view = cropView
        view?.setOnCropImageCompleteListener { _, result ->
            isCropping = false
            if (result.isSuccessful && result.uriContent != null) {
                onCropped(requireNotNull(result.uriContent))
            } else {
                cropFailed = true
            }
        }
        onDispose { view?.setOnCropImageCompleteListener(null) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crop_profile_photo)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val view = cropView ?: return@TextButton
                            val cropDirectory = File(context.cacheDir, "profile-crops").apply { mkdirs() }
                            val destination = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.profilephotos",
                                File(cropDirectory, "profile_crop_${UUID.randomUUID()}.jpg"),
                            )
                            isCropping = true
                            cropFailed = false
                            runCatching {
                                view.croppedImageAsync(
                                    Bitmap.CompressFormat.JPEG,
                                    90,
                                    1024,
                                    1024,
                                    CropImageView.RequestSizeOptions.RESIZE_INSIDE,
                                    destination,
                                )
                            }.onFailure {
                                isCropping = false
                                cropFailed = true
                            }
                        },
                        enabled = !isCropping,
                    ) {
                        if (isCropping) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        else Text(stringResource(R.string.use_profile_photo))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { cropView?.rotateImage(-90) }, enabled = !isCropping) {
                    Icon(Icons.AutoMirrored.Outlined.RotateLeft, contentDescription = stringResource(R.string.rotate_left))
                }
                IconButton(onClick = { cropView?.rotateImage(90) }, enabled = !isCropping) {
                    Icon(Icons.AutoMirrored.Outlined.RotateRight, contentDescription = stringResource(R.string.rotate_right))
                }
                Text(
                    text = stringResource(if (cropFailed) R.string.profile_photo_failed else R.string.profile_photo_crop_gesture_support),
                    modifier = Modifier.weight(1f),
                    color = if (cropFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (cropFailed) Button(onClick = onFailed) { Text(stringResource(R.string.close)) }
            }
        },
    ) { innerPadding ->
        AndroidView(
            factory = { viewContext ->
                CropImageView(viewContext).apply {
                    cropShape = CropImageView.CropShape.RECTANGLE
                    guidelines = CropImageView.Guidelines.ON
                    setFixedAspectRatio(true)
                    setAspectRatio(1, 1)
                    setImageUriAsync(source)
                    cropView = this
                }
            },
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
    }
}
