package com.kaezzy.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaezzy.app.core.database.KaezzyDatabase
import com.kaezzy.app.core.database.entity.AppSettingsEntity
import com.kaezzy.app.core.database.entity.UserProgressEntity
import com.kaezzy.app.core.security.PinHasher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val database: KaezzyDatabase) : ViewModel() {

    val appSettings: StateFlow<AppSettingsEntity?> = database.appSettingsDao().getAppSettings().stateInScope(null)

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun updateNickname(nickname: String) {
        viewModelScope.launch {
            val current = database.appSettingsDao().getAppSettingsDirect() ?: AppSettingsEntity()
            database.appSettingsDao().insertOrUpdate(current.copy(nickname = nickname.trim()))
        }
    }

    fun setupPin(pin: String, recoveryPassphrase: String) {
        viewModelScope.launch {
            val pinSalt = PinHasher.generateSalt()
            val pinHash = PinHasher.hashPin(pin, pinSalt)

            val recSalt = PinHasher.generateSalt()
            val recHash = PinHasher.hashRecoveryPassphrase(recoveryPassphrase, recSalt)

            val current = database.appSettingsDao().getAppSettingsDirect() ?: AppSettingsEntity()
            database.appSettingsDao().insertOrUpdate(
                current.copy(
                    isPinEnabled = 1,
                    pinHash = pinHash,
                    pinSalt = pinSalt,
                    recoveryHash = recHash,
                    recoverySalt = recSalt,
                    failedPinAttempts = 0,
                    lockoutUntil = null
                )
            )
            _snackbarEvent.emit("PIN & Security berhasil diatur, brok!")
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            val current = database.appSettingsDao().getAppSettingsDirect() ?: AppSettingsEntity()
            database.appSettingsDao().insertOrUpdate(
                current.copy(
                    isPinEnabled = 0,
                    pinHash = null,
                    pinSalt = null,
                    recoveryHash = null,
                    recoverySalt = null,
                    isBiometricEnabled = 0,
                    failedPinAttempts = 0,
                    lockoutUntil = null
                )
            )
            _snackbarEvent.emit("App Lock dinonaktifkan.")
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            val current = database.appSettingsDao().getAppSettingsDirect() ?: AppSettingsEntity()
            database.appSettingsDao().insertOrUpdate(current.copy(isBiometricEnabled = if (enabled) 1 else 0))
        }
    }

    fun exportBackup(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = BackupRestoreManager.exportBackupJson(database)
            onExportReady(json)
        }
    }

    fun restoreBackup(jsonContent: String) {
        viewModelScope.launch {
            val error = BackupRestoreManager.restoreBackupJson(database, jsonContent)
            if (error != null) {
                _snackbarEvent.emit(error)
            } else {
                _snackbarEvent.emit("Data berhasil dipulihkan 100%, brok!")
            }
        }
    }

    fun resetAllData(confirmInput: String) {
        if (confirmInput.trim().uppercase() != "HAPUS") {
            viewModelScope.launch {
                _snackbarEvent.emit("Ketik kata 'HAPUS' secara persis untuk mengonfirmasi reset data.")
            }
            return
        }

        viewModelScope.launch {
            database.clearAllTables()
            database.appSettingsDao().insertOrUpdate(AppSettingsEntity())
            database.userProgressDao().insertOrUpdate(UserProgressEntity())
            _snackbarEvent.emit("Seluruh data berhasil di-reset bersih.")
        }
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInScope(initialValue: T): StateFlow<T> {
        val flow = this
        val state = MutableStateFlow(initialValue)
        viewModelScope.launch {
            flow.collect { state.value = it }
        }
        return state.asStateFlow()
    }
}
