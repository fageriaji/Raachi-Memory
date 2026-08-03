package com.raachi.memory.domain

import com.raachi.memory.domain.model.AppLockSettings
import com.raachi.memory.domain.repository.AppLockRepository
import com.raachi.memory.domain.security.AppLockManager
import com.raachi.memory.domain.security.AppLockSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLockManagerTest {
    @Test
    fun recoveryCodeUsesReadableTwelveDigitFormat() {
        val manager = manager()

        assertTrue(manager.generateRecoveryCode().matches(Regex("\\d{4}-\\d{4}-\\d{4}")))
    }

    @Test
    fun enableStoresOnlyHashesAndVerifiesBothCredentials() = runTest {
        val repository = FakeAppLockRepository()
        val manager = manager(repository)

        manager.enable("123456", "4827-1964-7352", biometricEnabled = true)

        assertTrue(repository.state.value.enabled)
        assertTrue(repository.state.value.biometricEnabled)
        assertNotEquals("123456", repository.state.value.passcodeHash)
        assertNotEquals("482719647352", repository.state.value.recoveryHash)
        assertTrue(manager.verifyPasscode("123456"))
        assertFalse(manager.verifyPasscode("654321"))
        assertTrue(manager.verifyRecoveryCode("4827-1964-7352"))
    }

    @Test
    fun passcodeChangeRequiresCurrentPasscode() = runTest {
        val manager = manager()
        manager.enable("123456", "4827-1964-7352", biometricEnabled = false)

        assertFalse(manager.changePasscode("000000", "654321"))
        assertTrue(manager.verifyPasscode("123456"))
        assertTrue(manager.changePasscode("123456", "654321"))
        assertFalse(manager.verifyPasscode("123456"))
        assertTrue(manager.verifyPasscode("654321"))
    }

    @Test
    fun resetRotatesPasscodeAndRecoveryCode() = runTest {
        val manager = manager()
        manager.enable("123456", "4827-1964-7352", biometricEnabled = false)

        val newRecoveryCode = manager.resetPasscode("654321")

        assertFalse(manager.verifyPasscode("123456"))
        assertTrue(manager.verifyPasscode("654321"))
        assertFalse(manager.verifyRecoveryCode("4827-1964-7352"))
        assertTrue(manager.verifyRecoveryCode(newRecoveryCode))
    }

    private fun manager(repository: FakeAppLockRepository = FakeAppLockRepository()) =
        AppLockManager(repository, AppLockSession())
}

private class FakeAppLockRepository : AppLockRepository {
    val state = MutableStateFlow(AppLockSettings())
    override val appLockSettings: Flow<AppLockSettings> = state

    override suspend fun replaceAppLockSettings(settings: AppLockSettings) {
        state.value = settings
    }
}
