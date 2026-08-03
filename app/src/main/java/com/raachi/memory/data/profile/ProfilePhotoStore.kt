package com.raachi.memory.data.profile

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ProfilePhotoStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun persist(source: Uri): String = withContext(Dispatchers.IO) {
        val directory = photoDirectory().apply { mkdirs() }
        val target = File(directory, "profile_${UUID.randomUUID()}.jpg")
        try {
            requireNotNull(openInput(source)) { "Unable to read selected profile photo." }.use { input ->
                target.outputStream().buffered().use { output -> input.copyLimitedTo(output) }
            }
            Uri.fromFile(target).toString()
        } catch (error: Throwable) {
            target.delete()
            throw error
        }
    }

    suspend fun persist(bytes: ByteArray): String = withContext(Dispatchers.IO) {
        require(bytes.isNotEmpty() && bytes.size <= MAX_PROFILE_PHOTO_BYTES) { "Invalid profile photo." }
        val directory = photoDirectory().apply { mkdirs() }
        val target = File(directory, "profile_${UUID.randomUUID()}.jpg")
        try {
            target.writeBytes(bytes)
            Uri.fromFile(target).toString()
        } catch (error: Throwable) {
            target.delete()
            throw error
        }
    }

    suspend fun read(uriString: String?): ByteArray? = withContext(Dispatchers.IO) {
        val uri = uriString?.let(Uri::parse) ?: return@withContext null
        openInput(uri)?.use { input -> input.readLimitedBytes() }
    }

    suspend fun delete(uriString: String?) = withContext(Dispatchers.IO) {
        val file = managedFile(uriString) ?: return@withContext
        file.delete()
    }

    private fun openInput(uri: Uri): InputStream? = when (uri.scheme) {
        "file" -> uri.path?.let(::File)?.inputStream()
        else -> context.contentResolver.openInputStream(uri)
    }

    private fun managedFile(uriString: String?): File? {
        val uri = uriString?.let(Uri::parse) ?: return null
        if (uri.scheme != "file") return null
        val file = uri.path?.let(::File)?.canonicalFile ?: return null
        val directory = photoDirectory().canonicalFile
        return file.takeIf { it.parentFile == directory }
    }

    private fun photoDirectory() = File(context.filesDir, PROFILE_DIRECTORY)
}

private fun InputStream.copyLimitedTo(output: java.io.OutputStream) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        total += read
        require(total <= MAX_PROFILE_PHOTO_BYTES) { "Profile photo is too large." }
        output.write(buffer, 0, read)
    }
}

private fun InputStream.readLimitedBytes(): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    copyLimitedTo(output)
    return output.toByteArray()
}

private const val PROFILE_DIRECTORY = "profile"
internal const val MAX_PROFILE_PHOTO_BYTES = 5 * 1024 * 1024
