package com.example.vig.tools

import android.content.Context
import com.example.vig.domain.models.RiskLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalculatorTool : Tool {
    override val name = "Calculator"
    override val description = "Performs mathematical and numerical calculations"
    override val category = ToolCategory.UTILITY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ expression: String }"
    override val outputSchema = "{ result: Double, expression: String }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val expr = input.replace("×", "*").replace("x", "*").replace("÷", "/")
        val result = try {
            evalSimpleMath(expr)
        } catch (e: Exception) {
            null
        }

        return if (result != null) {
            ToolResult(
                success = true,
                message = "Result: $result",
                data = mapOf("expression" to input, "result" to result.toString())
            )
        } else {
            ToolResult(false, "Could not evaluate expression '$input'")
        }
    }

    private fun evalSimpleMath(expr: String): Double {
        val clean = expr.filter { it.isDigit() || it in "+-*/. " }.trim()
        val tokens = clean.split(" ").filter { it.isNotBlank() }
        if (tokens.size == 3) {
            val a = tokens[0].toDouble()
            val op = tokens[1]
            val b = tokens[2].toDouble()
            return when (op) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> if (b != 0.0) a / b else Double.NaN
                else -> a
            }
        }
        // Handle no-space simple math like 25*18
        val regex = Regex("""^(\d+(\.\d+)?)\s*([+\-*/])\s*(\d+(\.\d+)?)$""")
        val match = regex.find(clean)
        if (match != null) {
            val a = match.groupValues[1].toDouble()
            val op = match.groupValues[3]
            val b = match.groupValues[4].toDouble()
            return when (op) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> if (b != 0.0) a / b else Double.NaN
                else -> a
            }
        }
        return clean.toDoubleOrNull() ?: 0.0
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class TimeTool : Tool {
    override val name = "Time"
    override val description = "Retrieves current local time and timezone"
    override val category = ToolCategory.UTILITY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{}"
    override val outputSchema = "{ time: String }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val sdf = SimpleDateFormat("h:mm a, zzzz", Locale.getDefault())
        val timeStr = sdf.format(Date())
        return ToolResult(true, "Current Time: $timeStr", mapOf("time" to timeStr))
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = true
}

class DateTool : Tool {
    override val name = "Date"
    override val description = "Retrieves current calendar date and day of the week"
    override val category = ToolCategory.UTILITY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{}"
    override val outputSchema = "{ date: String }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date())
        return ToolResult(true, "Current Date: $dateStr", mapOf("date" to dateStr))
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = true
}
