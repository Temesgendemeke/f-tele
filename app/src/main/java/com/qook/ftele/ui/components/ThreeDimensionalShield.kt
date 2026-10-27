package com.qook.ftele.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.qook.ftele.ui.theme.FTeleTheme

@Composable
fun ThreeDimensionalShield(
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = FTeleTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "shieldScale"
    )

    val isDark = colors.bg == Color(0xFF0D0E10)

    // Left facet gradient (Illuminated by top-left ambient light)
    val leftGradientColors = if (isEnabled) {
        if (isDark) {
            listOf(Color(0xFF34D399), Color(0xFF14B8A6), Color(0xFF0F766E))
        } else {
            listOf(Color(0xFF2DD4BF), Color(0xFF0F6E56), Color(0xFF0A503E))
        }
    } else {
        if (isDark) {
            listOf(Color(0xFF6B7280), Color(0xFF4B5563), Color(0xFF374151))
        } else {
            listOf(Color(0xFF9CA3AF), Color(0xFF6B7280), Color(0xFF4B5563))
        }
    }

    // Right facet gradient (Shadow facet)
    val rightGradientColors = if (isEnabled) {
        if (isDark) {
            listOf(Color(0xFF0F766E), Color(0xFF0D524C), Color(0xFF042F2C))
        } else {
            listOf(Color(0xFF0B5844), Color(0xFF084233), Color(0xFF04261D))
        }
    } else {
        if (isDark) {
            listOf(Color(0xFF374151), Color(0xFF262E38), Color(0xFF111827))
        } else {
            listOf(Color(0xFF4B5563), Color(0xFF374151), Color(0xFF1F2937))
        }
    }

    val glowColor = if (isEnabled) colors.accent.copy(alpha = if (isDark) 0.28f else 0.16f) else Color.Transparent

    Box(
        modifier = modifier
            .size(156.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Layer 1 & 2: Ambient Halo & Recessed Pedestal Dock
        Canvas(modifier = Modifier.size(156.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val glowRadius = size.width / 2f

            if (isEnabled) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = center
                )
            }

            // Layer 2: Outer Circular Recessed Dock
            val dockRadius = size.width * 0.44f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.cardSoft,
                        colors.card
                    ),
                    center = center,
                    radius = dockRadius
                ),
                radius = dockRadius,
                center = center
            )
            // Dock outer beveled rim (specular top-left to shadow bottom-right)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.14f else 0.6f),
                        Color.Black.copy(alpha = if (isDark) 0.45f else 0.15f)
                    ),
                    start = Offset(center.x - dockRadius, center.y - dockRadius),
                    end = Offset(center.x + dockRadius, center.y + dockRadius)
                ),
                radius = dockRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Layer 3: The 3D Chiseled Shield Crest
        Canvas(modifier = Modifier.size(96.dp, 114.dp)) {
            val w = size.width
            val h = size.height

            val midX = w * 0.5f
            val topY = h * 0.05f
            val shoulderY = h * 0.15f
            val leftX = w * 0.10f
            val rightX = w * 0.90f
            val flankBottomY = h * 0.48f
            val bottomY = h * 0.95f

            // Full Shield Path for shadow & bevel outline
            val fullShieldPath = Path().apply {
                moveTo(midX, topY)
                cubicTo(midX * 0.65f, topY - 1f, leftX + 8f, shoulderY - 6f, leftX, shoulderY)
                lineTo(leftX, flankBottomY)
                cubicTo(leftX, flankBottomY + (bottomY - flankBottomY) * 0.42f, midX - w * 0.18f, bottomY - 14f, midX, bottomY)
                cubicTo(midX + w * 0.18f, bottomY - 14f, rightX, flankBottomY + (bottomY - flankBottomY) * 0.42f, rightX, flankBottomY)
                lineTo(rightX, shoulderY)
                cubicTo(rightX - 8f, shoulderY - 6f, midX + (rightX - midX) * 0.35f, topY - 1f, midX, topY)
                close()
            }

            // Draw Drop Shadow
            drawPath(
                path = fullShieldPath,
                color = Color.Black.copy(alpha = if (isDark) 0.50f else 0.22f),
                style = Fill
            )

            // Shifted slightly down for 3D depth shadow
            val shadowPath = Path().apply {
                addPath(fullShieldPath, Offset(0f, 6.dp.toPx()))
            }
            drawPath(
                path = shadowPath,
                color = Color.Black.copy(alpha = if (isDark) 0.35f else 0.15f),
                style = Fill
            )

            // Left Facet Path (Light side)
            val leftFacetPath = Path().apply {
                moveTo(midX, topY)
                cubicTo(midX * 0.65f, topY - 1f, leftX + 8f, shoulderY - 6f, leftX, shoulderY)
                lineTo(leftX, flankBottomY)
                cubicTo(leftX, flankBottomY + (bottomY - flankBottomY) * 0.42f, midX - w * 0.18f, bottomY - 14f, midX, bottomY)
                lineTo(midX, topY)
                close()
            }

            // Right Facet Path (Shadow side)
            val rightFacetPath = Path().apply {
                moveTo(midX, topY)
                cubicTo(midX + (rightX - midX) * 0.35f, topY - 1f, rightX - 8f, shoulderY - 6f, rightX, shoulderY)
                lineTo(rightX, flankBottomY)
                cubicTo(rightX, flankBottomY + (bottomY - flankBottomY) * 0.42f, midX + w * 0.18f, bottomY - 14f, midX, bottomY)
                lineTo(midX, topY)
                close()
            }

            // Draw Left Facet
            drawPath(
                path = leftFacetPath,
                brush = Brush.linearGradient(
                    colors = leftGradientColors,
                    start = Offset(leftX, topY),
                    end = Offset(midX, bottomY)
                )
            )

            // Draw Right Facet
            drawPath(
                path = rightFacetPath,
                brush = Brush.linearGradient(
                    colors = rightGradientColors,
                    start = Offset(midX, topY),
                    end = Offset(rightX, bottomY)
                )
            )

            // Top Specular Shoulder Highlight (Glint on top curve)
            val shoulderGlintPath = Path().apply {
                moveTo(leftX + 2f, shoulderY)
                cubicTo(leftX + 10f, shoulderY - 5f, midX * 0.65f, topY, midX, topY)
            }
            drawPath(
                path = shoulderGlintPath,
                color = Color.White.copy(alpha = if (isEnabled) 0.65f else 0.35f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Center Chiseled Ridge:
            // Highlight on left edge of ridge
            drawLine(
                color = Color.White.copy(alpha = if (isEnabled) 0.50f else 0.30f),
                start = Offset(midX - 0.75.dp.toPx(), topY + 2.dp.toPx()),
                end = Offset(midX - 0.75.dp.toPx(), bottomY - 2.dp.toPx()),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Shadow crease on right edge of ridge
            drawLine(
                color = Color.Black.copy(alpha = 0.45f),
                start = Offset(midX + 0.75.dp.toPx(), topY + 2.dp.toPx()),
                end = Offset(midX + 0.75.dp.toPx(), bottomY - 2.dp.toPx()),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Outer Chiseled Bevel Stroke
            drawPath(
                path = fullShieldPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.45f else 0.75f),
                        Color.Black.copy(alpha = if (isDark) 0.60f else 0.25f)
                    ),
                    start = Offset(leftX, topY),
                    end = Offset(rightX, bottomY)
                ),
                style = Stroke(width = 2.dp.toPx())
            )

            // Embossed Icon on the 3D Shield
            if (isEnabled) {
                // Checkmark Emblem with 3D drop shadow
                val checkStart = Offset(w * 0.35f, h * 0.52f)
                val checkMid = Offset(w * 0.46f, h * 0.64f)
                val checkEnd = Offset(w * 0.68f, h * 0.38f)

                val checkPath = Path().apply {
                    moveTo(checkStart.x, checkStart.y)
                    lineTo(checkMid.x, checkMid.y)
                    lineTo(checkEnd.x, checkEnd.y)
                }

                // Checkmark Shadow
                val checkShadowPath = Path().apply {
                    moveTo(checkStart.x, checkStart.y + 2.dp.toPx())
                    lineTo(checkMid.x, checkMid.y + 2.dp.toPx())
                    lineTo(checkEnd.x, checkEnd.y + 2.dp.toPx())
                }
                drawPath(
                    path = checkShadowPath,
                    color = Color.Black.copy(alpha = 0.55f),
                    style = Stroke(
                        width = 4.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Checkmark Crisp Front
                drawPath(
                    path = checkPath,
                    color = Color.White,
                    style = Stroke(
                        width = 4.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            } else {
                // Paused Bar Symbol with 3D drop shadow
                val barLeft = Offset(w * 0.36f, h * 0.51f)
                val barRight = Offset(w * 0.64f, h * 0.51f)

                // Shadow
                drawLine(
                    color = Color.Black.copy(alpha = 0.55f),
                    start = Offset(barLeft.x, barLeft.y + 2.dp.toPx()),
                    end = Offset(barRight.x, barRight.y + 2.dp.toPx()),
                    strokeWidth = 4.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Crisp Front
                drawLine(
                    color = Color.White.copy(alpha = 0.90f),
                    start = barLeft,
                    end = barRight,
                    strokeWidth = 4.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
