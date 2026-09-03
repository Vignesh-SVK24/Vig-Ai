package com.example.vig.agent.planner

import com.example.vig.domain.interfaces.ToolRegistry
import com.example.vig.domain.models.AgentTask
import com.example.vig.domain.models.RiskLevel
import com.example.vig.domain.models.TaskCategory
import com.example.vig.domain.models.TaskStep
import java.util.UUID

class TaskPlanner(private val toolRegistry: ToolRegistry) {

    fun planTask(command: String, category: TaskCategory): AgentTask {
        val task = AgentTask(
            taskId = UUID.randomUUID().toString(),
            originalCommand = command,
            category = category
        )

        val lower = command.lowercase()
        val steps = mutableListOf<TaskStep>()

        // 1. Math / Calculation Step
        if (lower.contains("calculate") || lower.matches(Regex(""".*\b\d+\s*[+\-*/x×÷]\s*\d+.*"""))) {
            val expr = extractMathExpression(command)
            steps.add(
                TaskStep(
                    description = "Calculate numerical expression '$expr'",
                    toolName = "Calculator",
                    arguments = mapOf("expression" to expr)
                )
            )
        }

        // 2. Document & PDF Steps
        if (lower.contains("pdf") || lower.contains("assignment") || lower.contains("document")) {
            steps.add(
                TaskStep(
                    description = "Read and extract content from document",
                    toolName = "ReadPdf",
                    arguments = mapOf("documentName" to command)
                )
            )
            if (lower.contains("summar") || lower.contains("explain") || lower.contains("what is in")) {
                steps.add(
                    TaskStep(
                        description = "Summarize document highlights",
                        toolName = "SummarizeDocument",
                        arguments = mapOf("query" to command)
                    )
                )
            }
            if (lower.contains("quiz") || lower.contains("question")) {
                steps.add(
                    TaskStep(
                        description = "Generate assessment quiz from document topics",
                        toolName = "GenerateQuiz",
                        arguments = mapOf("topic" to "Document Material")
                    )
                )
            }
        }

        // 3. Study Steps
        if (lower.contains("study") || lower.contains("plan my day") || lower.contains("schedule")) {
            steps.add(
                TaskStep(
                    description = "Generate structured adaptive study schedule",
                    toolName = "CreateStudyPlan",
                    arguments = mapOf("topic" to command)
                )
            )
        }
        if (lower.contains("flashcard")) {
            steps.add(
                TaskStep(
                    description = "Generate quick-revision flashcards",
                    toolName = "GenerateFlashcard",
                    arguments = mapOf("topic" to command)
                )
            )
        }

        // 4. Device App Launching
        if (lower.contains("open ") || lower.startsWith("launch ")) {
            val appName = extractAppName(lower)
            if (appName.isNotBlank() && appName != "settings" && !appName.startsWith("http")) {
                steps.add(
                    TaskStep(
                        description = "Launch $appName application",
                        toolName = "OpenApp",
                        arguments = mapOf("appName" to appName)
                    )
                )
            } else if (appName.startsWith("http")) {
                steps.add(
                    TaskStep(
                        description = "Open web link $appName",
                        toolName = "OpenUrl",
                        arguments = mapOf("url" to appName)
                    )
                )
            } else if (appName == "settings") {
                steps.add(
                    TaskStep(
                        description = "Open device settings",
                        toolName = "OpenSettings",
                        arguments = mapOf("settingType" to "general")
                    )
                )
            }
        }

        // 5. Web Search
        if (lower.contains("search") || lower.contains("find out") || lower.contains("google ")) {
            val query = command.replace(Regex("(?i)^(search( the web for)?|find out|google)\\s*"), "").trim()
            steps.add(
                TaskStep(
                    description = "Perform live web search for '$query'",
                    toolName = "WebSearch",
                    arguments = mapOf("query" to query)
                )
            )
        }

        // 6. Communication (High Risk)
        if (lower.contains("call ") || lower.contains("dial ")) {
            val target = command.replace(Regex("(?i)^(call|dial)\\s*"), "").trim()
            steps.add(
                TaskStep(
                    description = "Place phone call to $target",
                    toolName = "CallContact",
                    arguments = mapOf("contact" to target),
                    requiresConfirmation = true
                )
            )
            task.riskLevel = RiskLevel.HIGH
        }

        // 7. Reminders & Alarms
        if (lower.contains("remind") || lower.contains("alarm")) {
            steps.add(
                TaskStep(
                    description = "Create reminder/alarm: '$command'",
                    toolName = "CreateAlarmTool",
                    arguments = mapOf("title" to command),
                    requiresConfirmation = false
                )
            )
        }

        // 8. Memory Management
        if (lower.contains("remember ")) {
            val fact = command.replace(Regex("(?i)^remember( that)?\\s*"), "").trim()
            steps.add(
                TaskStep(
                    description = "Store fact in long-term memory",
                    toolName = "Remember",
                    arguments = mapOf("fact" to fact)
                )
            )
        } else if (lower.contains("what do you remember") || lower.contains("recall")) {
            steps.add(
                TaskStep(
                    description = "Recall stored user memory",
                    toolName = "Recall",
                    arguments = mapOf("query" to command)
                )
            )
        } else if (lower.contains("forget ")) {
            val target = command.replace(Regex("(?i)^forget\\s*"), "").trim()
            steps.add(
                TaskStep(
                    description = "Remove item from memory",
                    toolName = "Forget",
                    arguments = mapOf("key" to target)
                )
            )
        }

        // 9. Time & Date Utility
        if (lower.contains("what time") || lower.contains("current time")) {
            steps.add(
                TaskStep(
                    description = "Fetch local device time",
                    toolName = "Time"
                )
            )
        } else if (lower.contains("what day") || lower.contains("today's date") || lower.contains("what date")) {
            steps.add(
                TaskStep(
                    description = "Fetch current calendar date",
                    toolName = "Date"
                )
            )
        }

        task.steps.addAll(steps)
        return task
    }

    private fun extractAppName(lower: String): String {
        return lower
            .replace("open ", "")
            .replace("launch ", "")
            .replace("and search.*".toRegex(), "")
            .trim()
    }

    private fun extractMathExpression(command: String): String {
        val mathMatch = Regex("""\b(\d+(\.\d+)?\s*[+\-*/x×÷]\s*\d+(\.\d+)?)\b""").find(command)
        return mathMatch?.value ?: command.replace(Regex("(?i)calculate\\s*"), "").trim()
    }
}
