package com.example.vig.agent.manager

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.example.vig.domain.models.AutonomyLevel
import com.example.vig.domain.models.RiskLevel
import com.example.vig.tools.Tool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

data class PendingConfirmation(
    val actionId: String,
    val title: String,
    val description: String,
    val riskLevel: RiskLevel,
    val toolName: String,
    val onConfirm: suspend () -> Unit,
    val onDeny: suspend () -> Unit
)

class CapabilityManager(private val context: Context?) {
    fun isCapabilityAvailable(capability: String): Boolean {
        if (context == null) return true
        return when (capability.lowercase()) {
            "network", "web" -> {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                cm?.activeNetwork != null
            }
            "calls", "telephony" -> {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                tm?.phoneType != TelephonyManager.PHONE_TYPE_NONE
            }
            "voice", "microphone" -> {
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
            }
            else -> true
        }
    }

    fun getCapabilitiesReport(): Map<String, String> {
        return mapOf(
            "Microphone" to "AVAILABLE",
            "Voice Recognition" to "AVAILABLE",
            "Device Apps" to "AVAILABLE",
            "Web Search" to if (isCapabilityAvailable("network")) "AVAILABLE" else "OFFLINE",
            "Calls & Dialing" to if (isCapabilityAvailable("calls")) "AVAILABLE (CONFIRMATION REQUIRED)" else "UNAVAILABLE",
            "Calendar" to "AVAILABLE",
            "Memory System" to "AVAILABLE"
        )
    }
}

class PermissionManager(private val context: Context? = null) {
    private val grantedPermissions = mutableSetOf<String>()

    fun hasPermission(permission: String): Boolean {
        if (context != null) {
            val res = context.checkSelfPermission(permission)
            if (res == PackageManager.PERMISSION_GRANTED) return true
        }
        return grantedPermissions.contains(permission)
    }

    fun grantPermission(permission: String) {
        grantedPermissions.add(permission)
    }

    fun revokePermission(permission: String) {
        grantedPermissions.remove(permission)
    }

    fun checkToolPermissions(tool: Tool): List<String> {
        return tool.requiredPermissions.filter { !hasPermission(it) }
    }
}

class RiskManager {
    var autonomyLevel: AutonomyLevel = AutonomyLevel.BALANCED

    fun assessRisk(tool: Tool): RiskLevel {
        return tool.riskLevel
    }

    fun requiresConfirmation(riskLevel: RiskLevel): Boolean {
        return when (autonomyLevel) {
            AutonomyLevel.ASSISTED -> riskLevel != RiskLevel.LOW
            AutonomyLevel.BALANCED -> riskLevel == RiskLevel.HIGH
            AutonomyLevel.STRICT -> true
        }
    }
}

class ConfirmationManager {
    private val _pendingConfirmation = MutableStateFlow<PendingConfirmation?>(null)
    val pendingConfirmation: StateFlow<PendingConfirmation?> = _pendingConfirmation.asStateFlow()

    fun requestConfirmation(
        title: String,
        description: String,
        riskLevel: RiskLevel,
        toolName: String,
        onConfirm: suspend () -> Unit,
        onDeny: suspend () -> Unit
    ) {
        _pendingConfirmation.value = PendingConfirmation(
            actionId = System.currentTimeMillis().toString(),
            title = title,
            description = description,
            riskLevel = riskLevel,
            toolName = toolName,
            onConfirm = onConfirm,
            onDeny = onDeny
        )
    }

    suspend fun confirm(): Boolean {
        val pending = _pendingConfirmation.value ?: return false
        _pendingConfirmation.value = null
        pending.onConfirm()
        return true
    }

    suspend fun deny(): Boolean {
        val pending = _pendingConfirmation.value ?: return false
        _pendingConfirmation.value = null
        pending.onDeny()
        return false
    }

    fun hasPendingAction(): Boolean = _pendingConfirmation.value != null
}

object EmergencyLockManager {
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun lock() {
        _isLocked.value = true
    }

    fun unlock() {
        _isLocked.value = false
    }

    fun isEmergencyLocked(): Boolean = _isLocked.value
}
