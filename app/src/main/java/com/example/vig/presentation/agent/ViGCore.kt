package com.example.vig.presentation.agent

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.vig.domain.models.AgentState
import com.example.vig.presentation.theme.*

@Composable
fun ViGCore(state: AgentState, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "teal_core_anim")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == AgentState.EXECUTING) 1200 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "teal_spin"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = if (state == AgentState.EXECUTING || state == AgentState.LISTENING) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == AgentState.LISTENING) 350 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "teal_pulse"
    )

    val activeGlow = when (state) {
        AgentState.FAILED, AgentState.CANCELLED -> MutedRed
        AgentState.LISTENING -> ElectricTeal
        else -> GlowingTeal
    }

    Canvas(modifier = modifier.size(190.dp).padding(12.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.minDimension / 2) * corePulse

        // 1. Dark Teal Chassis Base
        drawCircle(
            color = DarkTealBase,
            radius = radius * 1.08f,
            center = center
        )

        // 2. Outer Glowing Ring
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(ElectricTeal, DarkTealSurface, GlowingTeal, DarkTealBase, ElectricTeal)
            ),
            radius = radius,
            center = center,
            style = Stroke(width = 6.dp.toPx())
        )

        // 3. Rotating Dashed Orbit Ring
        drawCircle(
            color = ElectricTeal.copy(alpha = 0.8f),
            radius = radius * 0.85f,
            center = center,
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 20f), rotation)
            )
        )

        // 4. Glowing Dark Teal Core Orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ElectricTeal,
                    GlowingTeal,
                    DarkTealSurface,
                    DarkTealBase
                ),
                center = center,
                radius = radius * 0.7f
            ),
            radius = radius * 0.65f,
            center = center
        )
    }
}
