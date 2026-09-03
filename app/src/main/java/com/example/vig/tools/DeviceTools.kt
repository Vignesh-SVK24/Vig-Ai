package com.example.vig.tools

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import com.example.vig.domain.models.RiskLevel
import org.json.JSONObject

class OpenAppTool : Tool {
    override val name = "OpenApp"
    override val description = "Opens an installed Android application by name or package"
    override val category = ToolCategory.DEVICE
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ appName: String }"
    override val outputSchema = "{ success: Boolean, packageName: String, message: String }"

    private val commonAppPackages = mapOf(
        "youtube" to "com.google.android.youtube",
        "chrome" to "com.android.chrome",
        "maps" to "com.google.android.apps.maps",
        "whatsapp" to "com.whatsapp",
        "camera" to "com.google.android.GoogleCamera",
        "settings" to "com.android.settings",
        "calculator" to "com.google.android.calculator",
        "clock" to "com.google.android.deskclock",
        "calendar" to "com.google.android.calendar",
        "gmail" to "com.google.android.gm"
    )

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) {
            return ToolResult(success = false, message = "Android context unavailable")
        }

        val appName = try {
            JSONObject(input).optString("appName", input).trim().lowercase()
        } catch (_: Exception) {
            input.trim().lowercase()
        }

        val pm = context.packageManager
        val targetPackage = commonAppPackages[appName] ?: findPackageByName(pm, appName)

        if (targetPackage == null) {
            return ToolResult(
                success = false,
                message = "Application '$appName' is not installed on this device.",
                error = "APP_NOT_INSTALLED"
            )
        }

        val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
            ?: Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", targetPackage, null)
            }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(launchIntent)
            ToolResult(
                success = true,
                message = "Launched $appName successfully.",
                data = mapOf("package" to targetPackage, "appName" to appName)
            )
        } catch (e: Exception) {
            ToolResult(success = false, message = "Failed to open $appName: ${e.message}", error = e.message)
        }
    }

    private fun findPackageByName(pm: android.content.pm.PackageManager, query: String): String? {
        val installedApps = pm.getInstalledApplications(0)
        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(query) || app.packageName.lowercase().contains(query)) {
                return app.packageName
            }
        }
        return null
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean {
        return result.success
    }
}

class FindInstalledAppTool : Tool {
    override val name = "FindInstalledApp"
    override val description = "Searches for installed applications on the device"
    override val category = ToolCategory.DEVICE
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ query: String }"
    override val outputSchema = "{ apps: List<String> }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")
        val pm = context.packageManager
        val query = input.lowercase().trim()
        val matches = pm.getInstalledApplications(0)
            .map { pm.getApplicationLabel(it).toString() }
            .filter { it.lowercase().contains(query) }
            .take(10)

        return ToolResult(
            success = true,
            message = "Found ${matches.size} matching apps: ${matches.joinToString()}",
            data = mapOf("matches" to matches.joinToString(", "))
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class OpenUrlTool : Tool {
    override val name = "OpenUrl"
    override val description = "Opens any web URL in the default browser"
    override val category = ToolCategory.DEVICE
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ url: String }"
    override val outputSchema = "{ success: Boolean }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")
        var url = input.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ToolResult(success = true, message = "Opened $url in browser", data = mapOf("url" to url))
        } catch (e: Exception) {
            ToolResult(success = false, message = "Could not open URL: ${e.message}")
        }
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class OpenSettingsTool : Tool {
    override val name = "OpenSettings"
    override val description = "Opens Android system settings or specific sub-settings"
    override val category = ToolCategory.DEVICE
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ settingType: String }"
    override val outputSchema = "{ success: Boolean }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")
        val action = when (input.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "sound" -> Settings.ACTION_SOUND_SETTINGS
            "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        return try {
            context.startActivity(intent)
            ToolResult(success = true, message = "Opened $input settings.")
        } catch (e: Exception) {
            ToolResult(success = false, message = "Failed to open settings: ${e.message}")
        }
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class ShareContentTool : Tool {
    override val name = "ShareContent"
    override val description = "Opens the Android system sharesheet to share text or links"
    override val category = ToolCategory.DEVICE
    override val riskLevel = RiskLevel.MEDIUM
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ text: String, title: String }"
    override val outputSchema = "{ success: Boolean }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, input)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share via ViG").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(shareIntent)
            ToolResult(success = true, message = "Share sheet opened.")
        } catch (e: Exception) {
            ToolResult(success = false, message = "Failed to share: ${e.message}")
        }
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class GetDeviceStateTool : Tool {
    override val name = "GetDeviceState"
    override val description = "Retrieves battery level, charging status, and network connection"
    override val category = ToolCategory.DEVICE
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{}"
    override val outputSchema = "{ batteryPct: Int, isCharging: Boolean, network: String }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryPct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val isCharging = batteryManager?.isCharging ?: false

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkType = cm?.activeNetwork?.let { net ->
            val caps = cm.getNetworkCapabilities(net)
            when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
                else -> "Connected"
            }
        } ?: "Offline"

        val msg = "Battery: $batteryPct% (${if (isCharging) "Charging" else "Not Charging"}), Network: $networkType"
        return ToolResult(
            success = true,
            message = msg,
            data = mapOf(
                "batteryPct" to "$batteryPct",
                "isCharging" to "$isCharging",
                "network" to networkType
            )
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class CreateAlarmTool : Tool {
    override val name = "CreateAlarmTool"
    override val description = "Sets an alarm for a specified hour and minute"
    override val category = ToolCategory.DEVICE
    override val riskLevel = RiskLevel.MEDIUM
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ hour: Int, minute: Int, message: String }"
    override val outputSchema = "{ success: Boolean }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, "ViG Alarm")
            putExtra(AlarmClock.EXTRA_HOUR, 7)
            putExtra(AlarmClock.EXTRA_MINUTES, 0)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ToolResult(success = true, message = "Alarm created for the specified time.")
        } catch (e: Exception) {
            ToolResult(success = false, message = "Could not set alarm: ${e.message}")
        }
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class CreateCalendarEventTool : Tool {
    override val name = "CreateCalendarEvent"
    override val description = "Creates a calendar event with a title and description"
    override val category = ToolCategory.DEVICE
    override val riskLevel = RiskLevel.MEDIUM
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ title: String, description: String }"
    override val outputSchema = "{ success: Boolean }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, input)
            putExtra(CalendarContract.Events.DESCRIPTION, "Scheduled by ViG Personal Assistant")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ToolResult(success = true, message = "Opened calendar to schedule '$input'.")
        } catch (e: Exception) {
            ToolResult(success = false, message = "Could not schedule event: ${e.message}")
        }
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}
