package com.example.vig.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vig.presentation.theme.*

// Beige & Glowing Dark Teal Card
fun Modifier.tealBeigeCard(
    elevation: Dp = 8.dp,
    cornerRadius: Dp = 24.dp,
    containerColor: Color = DarkTealSurface,
    glowColor: Color = GlowingTeal
) = composed {
    this
        .shadow(
            elevation = elevation,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = ShadowBeige,
            spotColor = glowColor.copy(alpha = 0.5f)
        )
        .clip(RoundedCornerShape(cornerRadius))
        .background(containerColor)
        .border(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(glowColor.copy(alpha = 0.6f), Color.Transparent, glowColor.copy(alpha = 0.2f))
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}

// Glowing Teal Button
fun Modifier.tealGlowingButton(
    isPressed: Boolean = false,
    cornerRadius: Dp = 20.dp,
    color: Color = GlowingTeal
) = composed {
    this
        .shadow(
            elevation = if (isPressed) 2.dp else 8.dp,
            shape = RoundedCornerShape(cornerRadius),
            spotColor = GlowingTeal
        )
        .clip(RoundedCornerShape(cornerRadius))
        .background(if (isPressed) color.copy(alpha = 0.8f) else color)
        .border(
            width = 1.5.dp,
            brush = Brush.verticalGradient(
                colors = listOf(ElectricTeal, DarkTealBase)
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}
