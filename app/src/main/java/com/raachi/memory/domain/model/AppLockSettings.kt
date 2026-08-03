package com.raachi.memory.domain.model

data class AppLockSettings(
    val enabled: Boolean = false,
    val passcodeHash: String = "",
    val passcodeSalt: String = "",
    val recoveryHash: String = "",
    val recoverySalt: String = "",
    val biometricEnabled: Boolean = false,
)
