package com.kaezzy.app.features.settings

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.kaezzy.app.R
import com.kaezzy.app.core.copy.KaezzyMicrocopy
import com.kaezzy.app.core.database.entity.AppSettingsEntity
import com.kaezzy.app.core.security.PinHasher
import com.kaezzy.app.ui.theme.BackgroundPrimary
import com.kaezzy.app.ui.theme.PrimaryAccent
import com.kaezzy.app.ui.theme.SemanticDanger
import com.kaezzy.app.ui.theme.SurfaceLevel1
import com.kaezzy.app.ui.theme.SurfaceLevel2
import com.kaezzy.app.ui.theme.TextPrimary
import com.kaezzy.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    appSettings: AppSettingsEntity?,
    onUnlockSuccess: () -> Unit,
    onLockoutStateChanged: (failedAttempts: Int, lockoutUntil: Long?) -> Unit,
    onResetPinConfirmed: () -> Unit
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryPassphraseInput by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf<String?>(null) }

    val lockoutUntil = appSettings?.lockoutUntil
    val now = System.currentTimeMillis()
    var remainingLockoutSec by remember(lockoutUntil) {
        mutableIntStateOf(
            if (lockoutUntil != null && lockoutUntil > now) {
                ((lockoutUntil - now) / 1000).toInt()
            } else 0
        )
    }

    LaunchedEffect(remainingLockoutSec) {
        if (remainingLockoutSec > 0) {
            delay(1000)
            remainingLockoutSec -= 1
            if (remainingLockoutSec <= 0) {
                onLockoutStateChanged(0, null)
            }
        }
    }

    val isLockoutActive = remainingLockoutSec > 0

    val verifyPinAction: (String) -> Unit = { pin ->
        val salt = appSettings?.pinSalt
        val hash = appSettings?.pinHash
        if (salt != null && hash != null) {
            if (PinHasher.verifyPin(pin, salt, hash)) {
                onLockoutStateChanged(0, null)
                onUnlockSuccess()
            } else {
                enteredPin = ""
                val currentAttempts = (appSettings.failedPinAttempts) + 1
                if (currentAttempts % 5 == 0) {
                    val lockoutTime = System.currentTimeMillis() + 30_000
                    onLockoutStateChanged(currentAttempts, lockoutTime)
                    errorMessage = KaezzyMicrocopy.lockoutActive(30)
                } else {
                    val remaining = 5 - (currentAttempts % 5)
                    onLockoutStateChanged(currentAttempts, null)
                    errorMessage = KaezzyMicrocopy.pinWrong(remaining)
                }
            }
        }
    }

    fun promptBiometric() {
        val fragmentActivity = context as? FragmentActivity ?: return
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                fragmentActivity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onLockoutStateChanged(0, null)
                        onUnlockSuccess()
                    }
                }
            )
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Kaezzy")
                .setSubtitle(KaezzyMicrocopy.BIOMETRIC_PROMPT)
                .setNegativeButtonText("Gunakan PIN")
                .build()
            biometricPrompt.authenticate(promptInfo)
        }
    }

    LaunchedEffect(Unit) {
        if (appSettings?.isBiometricEnabled == 1 && !isLockoutActive) {
            promptBiometric()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.faviconk),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kaezzy",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = KaezzyMicrocopy.PIN_ENTER,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6 PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 6) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) PrimaryAccent else SurfaceLevel2
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = isLockoutActive || errorMessage != null) {
                Text(
                    text = if (isLockoutActive) {
                        KaezzyMicrocopy.lockoutActive(remainingLockoutSec)
                    } else errorMessage.orEmpty(),
                    color = SemanticDanger,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in row) {
                            KeypadButton(
                                label = key,
                                enabled = !isLockoutActive,
                                isBiometricEnabled = appSettings?.isBiometricEnabled == 1,
                                onClick = {
                                    if (key == "DEL") {
                                        if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.dropLast(1)
                                            errorMessage = null
                                        }
                                    } else if (key == "BIO") {
                                        promptBiometric()
                                    } else if (enteredPin.length < 6) {
                                        enteredPin += key
                                        errorMessage = null
                                        if (enteredPin.length == 6) {
                                            verifyPinAction(enteredPin)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { showRecoveryDialog = true }
            ) {
                Text(
                    text = KaezzyMicrocopy.FORGOT_PIN,
                    color = PrimaryAccent,
                    fontSize = 14.sp
                )
            }
        }
    }

    if (showRecoveryDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showRecoveryDialog = false }
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reset PIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = KaezzyMicrocopy.RECOVERY_PASSPHRASE_HINT,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = recoveryPassphraseInput,
                        onValueChange = {
                            recoveryPassphraseInput = it
                            recoveryError = null
                        },
                        label = { Text("Recovery Passphrase") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (recoveryError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = recoveryError.orEmpty(),
                            color = SemanticDanger,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showRecoveryDialog = false }) {
                            Text("Batal", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            onClick = {
                                val salt = appSettings?.recoverySalt
                                val hash = appSettings?.recoveryHash
                                if (salt != null && hash != null) {
                                    if (PinHasher.verifyRecoveryPassphrase(recoveryPassphraseInput, salt, hash)) {
                                        showRecoveryDialog = false
                                        onResetPinConfirmed()
                                    } else {
                                        recoveryError = "Passphrase salah, brok!"
                                    }
                                } else {
                                    recoveryError = "Recovery Passphrase belum diatur."
                                }
                            }
                        ) {
                            Text("Reset PIN", color = BackgroundPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    enabled: Boolean,
    isBiometricEnabled: Boolean,
    onClick: () -> Unit
) {
    if (label == "BIO" && !isBiometricEnabled) {
        Spacer(modifier = Modifier.size(64.dp))
        return
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(SurfaceLevel2)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when (label) {
            "DEL" -> Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Hapus",
                tint = TextPrimary
            )
            "BIO" -> Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Biometrik",
                tint = PrimaryAccent
            )
            else -> Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) TextPrimary else TextSecondary
            )
        }
    }
}
