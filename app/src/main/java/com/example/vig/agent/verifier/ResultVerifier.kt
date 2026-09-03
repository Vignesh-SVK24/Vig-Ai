package com.example.vig.agent.verifier

import android.content.Context
import com.example.vig.tools.Tool
import com.example.vig.tools.ToolResult

class ResultVerifier {
    suspend fun verify(tool: Tool, result: ToolResult, context: Context?): Boolean {
        if (!result.success) return false
        return try {
            tool.verify(result, context)
        } catch (_: Exception) {
            false
        }
    }
}
