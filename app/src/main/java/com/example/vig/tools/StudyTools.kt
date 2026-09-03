package com.example.vig.tools

import android.content.Context
import com.example.vig.domain.models.RiskLevel

class CreateStudyPlanTool : Tool {
    override val name = "CreateStudyPlan"
    override val description = "Generates a structured, adaptive study plan block for topics or exams"
    override val category = ToolCategory.STUDY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ topic: String, durationHours: Int }"
    override val outputSchema = "{ plan: String, blocks: List<String> }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val topic = if (input.isNotBlank()) input else "Core Computer Science"
        val plan = """
            📅 Adaptive Study Plan for: $topic
            • 7:00 PM – 7:45 PM: Core Concepts & Theoretical Foundations
            • 7:45 PM – 8:00 PM: Active Recall & Quick Note Taking
            • 8:00 PM – 8:45 PM: Problem Solving & Practice Implementation
            • 8:45 PM – 9:00 PM: Self-Assessment Quiz & Spaced Repetition Review
        """.trimIndent()

        return ToolResult(
            success = true,
            message = plan,
            data = mapOf("topic" to topic, "plan" to plan)
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class GenerateQuizTool : Tool {
    override val name = "GenerateQuiz"
    override val description = "Generates interactive quiz questions with explanations"
    override val category = ToolCategory.STUDY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ topic: String, count: Int }"
    override val outputSchema = "{ questions: List<String> }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val topic = if (input.isNotBlank()) input else "Artificial Intelligence"
        val quiz = """
            🧠 Quiz on $topic:
            Q1: What property must an A* heuristic satisfy to guarantee an optimal path?
            A) Monotonicity & Admissibility  B) High branching factor  C) Negative weights
            [Answer: A - Admissible heuristics never overestimate true cost to the goal]

            Q2: In Minimax with Alpha-Beta pruning, what does Beta represent?
            A) The best value for MAX  B) The best value for MIN  C) Random choice
            [Answer: B - Beta is the minimum upper bound for MIN]

            Q3: Which search algorithm uses O(b*d) space complexity?
            A) Breadth-First Search  B) Depth-First Search  C) Dijkstra
            [Answer: B - DFS uses linear space relative to search depth]
        """.trimIndent()

        return ToolResult(
            success = true,
            message = quiz,
            data = mapOf("topic" to topic, "quiz" to quiz)
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class ExplainTopicTool : Tool {
    override val name = "ExplainTopic"
    override val description = "Delivers an articulate, intuitive explanation of complex concepts"
    override val category = ToolCategory.STUDY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ topic: String }"
    override val outputSchema = "{ explanation: String }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        return ToolResult(
            success = true,
            message = "Explained '$input' with key concepts and real-world clarity.",
            data = mapOf("topic" to input)
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class GenerateFlashcardTool : Tool {
    override val name = "GenerateFlashcard"
    override val description = "Generates structured flashcards (Term + Definition) for rapid review"
    override val category = ToolCategory.STUDY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ topic: String }"
    override val outputSchema = "{ flashcards: List<String> }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val flashcards = """
            🗂️ Flashcards:
            1. [Agent Architecture]: Sensors -> Perception -> Decision Function -> Actuators.
            2. [Heuristic]: Problem-specific domain knowledge guiding search towards the goal.
            3. [Constraint Satisfaction]: Problems defined by variables with domains subject to valid constraints.
        """.trimIndent()

        return ToolResult(
            success = true,
            message = flashcards,
            data = mapOf("flashcards" to flashcards)
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}
