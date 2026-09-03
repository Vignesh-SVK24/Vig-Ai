package com.example.vig.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.vig.domain.models.RiskLevel

class CallContactTool : Tool {
    override val name = "CallContact"
    override val description = "Places a phone call to a contact. (HIGH RISK - Requires User Approval)"
    override val category = ToolCategory.COMMUNICATION
    override val riskLevel = RiskLevel.HIGH
    override val requiredPermissions = listOf("android.permission.CALL_PHONE")
    override val inputSchema = "{ contactName: String, phoneNumber: String }"
    override val outputSchema = "{ dialerOpened: Boolean, recipient: String }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")
        
        // Clean phone number or dial query
        val number = input.filter { it.isDigit() || it == '+' }
        val dialUri = if (number.isNotBlank()) Uri.parse("tel:$number") else Uri.parse("tel:")
        
        val dialIntent = Intent(Intent.ACTION_DIAL, dialUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(dialIntent)
            ToolResult(
                success = true,
                message = "Opened dialer for '$input'. Tap call to proceed.",
                data = mapOf("target" to input)
            )
        } catch (e: Exception) {
            ToolResult(success = false, message = "Could not open dialer: ${e.message}", error = e.message)
        }
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class FindContactTool : Tool {
    override val name = "FindContact"
    override val description = "Looks up contact details or opens device contacts"
    override val category = ToolCategory.COMMUNICATION
    override val riskLevel = RiskLevel.MEDIUM
    override val requiredPermissions = listOf("android.permission.READ_CONTACTS")
    override val inputSchema = "{ name: String }"
    override val outputSchema = "{ found: Boolean, contactInfo: String }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://contacts/people/")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            ToolResult(
                success = true,
                message = "Opened contacts list for '$input'.",
                data = mapOf("contact" to input)
            )
        } catch (e: Exception) {
            ToolResult(success = false, message = "Could not view contacts: ${e.message}")
        }
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class PrepareMessageTool : Tool {
    override val name = "PrepareMessage"
    override val description = "Prepares an SMS text message in the default SMS app (HIGH RISK)"
    override val category = ToolCategory.COMMUNICATION
    override val riskLevel = RiskLevel.HIGH
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ recipient: String, messageText: String }"
    override val outputSchema = "{ success: Boolean }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", input)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            ToolResult(success = true, message = "Opened messaging app to prepare text.")
        } catch (e: Exception) {
            ToolResult(success = false, message = "Could not open messaging: ${e.message}")
        }
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}
