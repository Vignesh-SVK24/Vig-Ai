package com.example.vig.domain.interfaces

import com.example.vig.tools.Tool

interface ToolRegistry {
    fun register(tool: Tool)
    fun getTool(name: String): Tool?
    fun getAllTools(): List<Tool>
    fun getToolNames(): List<String>
}
