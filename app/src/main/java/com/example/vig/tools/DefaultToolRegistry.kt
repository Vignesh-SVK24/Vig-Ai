package com.example.vig.tools

import com.example.vig.domain.interfaces.ToolRegistry

class DefaultToolRegistry : ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()

    init {
        // Device
        register(OpenAppTool())
        register(FindInstalledAppTool())
        register(OpenUrlTool())
        register(OpenSettingsTool())
        register(ShareContentTool())
        register(GetDeviceStateTool())
        register(CreateAlarmTool())
        register(CreateCalendarEventTool())

        // Communication
        register(CallContactTool())
        register(FindContactTool())
        register(PrepareMessageTool())

        // Documents
        register(ReadPdfTool())
        register(SummarizeDocumentTool())
        register(CreateNoteTool())

        // Web
        register(WebSearchTool())
        register(OpenWebPageTool())

        // Study
        register(CreateStudyPlanTool())
        register(GenerateQuizTool())
        register(ExplainTopicTool())
        register(GenerateFlashcardTool())

        // Memory
        register(RememberTool())
        register(RecallTool())
        register(ForgetTool())
        register(ListMemoryTool())

        // Utility
        register(CalculatorTool())
        register(TimeTool())
        register(DateTool())
    }

    override fun register(tool: Tool) {
        tools[tool.name.lowercase()] = tool
    }

    override fun getTool(name: String): Tool? {
        return tools[name.lowercase()]
    }

    override fun getAllTools(): List<Tool> {
        return tools.values.toList()
    }

    override fun getToolNames(): List<String> {
        return tools.values.map { it.name }
    }
}
