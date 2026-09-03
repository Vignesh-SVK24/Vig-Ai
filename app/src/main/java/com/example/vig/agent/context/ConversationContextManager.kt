package com.example.vig.agent.context

data class ConversationTurn(
    val userQuery: String,
    val assistantResponse: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ConversationContextManager {
    private val turns = mutableListOf<ConversationTurn>()
    private val maxTurns = 6

    fun addTurn(userQuery: String, assistantResponse: String) {
        turns.add(ConversationTurn(userQuery, assistantResponse))
        if (turns.size > maxTurns) {
            turns.removeAt(0)
        }
    }

    fun getFormattedContext(): String {
        if (turns.isEmpty()) return ""
        return buildString {
            appendLine("Recent Conversation Context:")
            turns.takeLast(3).forEach {
                appendLine("User: ${it.userQuery}")
                appendLine("ViG: ${it.assistantResponse.take(150)}...")
            }
            appendLine()
        }
    }

    fun clear() {
        turns.clear()
    }
}
