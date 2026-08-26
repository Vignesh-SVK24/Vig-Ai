package com.example.vig.agent.manager

import com.example.vig.domain.models.RiskLevel
import com.example.vig.tools.Tool

class PermissionManager {
    private val grantedPermissions = mutableSetOf<String>()

    fun hasPermission(permission: String): Boolean {
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
    fun assessRisk(tool: Tool): RiskLevel {
        return tool.riskLevel
    }

    fun requiresConfirmation(riskLevel: RiskLevel): Boolean {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.MEDIUM
    }
}

class ConfirmationManager {
    private var pendingAction: String? = null

    fun requestConfirmation(action: String): String {
        pendingAction = action
        return "Action requires confirmation: $action"
    }

    fun confirm(): Boolean {
        pendingAction = null
        return true
    }

    fun deny(): Boolean {
        pendingAction = null
        return false
    }

    fun hasPendingAction(): Boolean = pendingAction != null
}
