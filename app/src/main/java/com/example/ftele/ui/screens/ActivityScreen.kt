package com.example.ftele.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.GppBad
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ftele.engine.Patterns
import com.example.ftele.model.LogEntry
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget
import com.example.ftele.ui.theme.FTeleTheme

@Composable
fun ActivityScreen(
    isEnabled: Boolean,
    hasPermission: Boolean,
    activeShieldCount: Int,
    isStrict: Boolean,
    logs: List<LogEntry>,
    currentRules: List<Rule>,
    onToggleProtection: () -> Unit,
    onOpenPermissionSettings: () -> Unit,
    onSimulateSms: () -> Unit,
    onClearLogs: () -> Unit,
    onAllowSender: (String) -> Unit,
    onBlockSender: (String) -> Unit
) {
    val colors = FTeleTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Hero Shield Status Card ---
        item {
            HeroShieldCard(
                isEnabled = isEnabled,
                activeShieldCount = activeShieldCount,
                isStrict = isStrict,
                decisionsCount = logs.size,
                onToggleProtection = onToggleProtection
            )
        }

        // --- 2. Permission Banner (Clean Monochrome / Relative Red Attention) ---
        item {
            AnimatedVisibility(visible = !hasPermission) {
                PermissionBanner(onOpenPermissionSettings = onOpenPermissionSettings)
            }
        }

        // --- 3. Section Header: Activity Logs & Controls ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity (${logs.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSimulateSms,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cardSoft,
                            contentColor = colors.ink
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = colors.ink
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Simulate", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    if (logs.isNotEmpty()) {
                        Button(
                            onClick = onClearLogs,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.cardSoft,
                                contentColor = colors.sub
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Clear activity log",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Logs List or Clean Empty State ---
        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.cardSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = colors.sub
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No SMS processed yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.ink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Incoming SMS will be evaluated offline against active shields and custom rules.",
                            fontSize = 12.sp,
                            color = colors.sub,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onSimulateSms,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.ink,
                                contentColor = colors.bg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Simulate Sample SMS", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                val looseSender = Patterns.loose(log.sender)
                val senderRule = currentRules.find {
                    it.target == RuleTarget.SENDER && Patterns.loose(it.value) == looseSender
                }

                LogItemCard(
                    log = log,
                    senderRule = senderRule,
                    onAllowSender = { onAllowSender(log.sender) },
                    onBlockSender = { onBlockSender(log.sender) }
                )
            }
        }
    }
}

@Composable
private fun HeroShieldCard(
    isEnabled: Boolean,
    activeShieldCount: Int,
    isStrict: Boolean,
    decisionsCount: Int,
    onToggleProtection: () -> Unit
) {
    val colors = FTeleTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large 3D Chiseled Security Shield
            ThreeDimensionalShield(
                isEnabled = isEnabled,
                onClick = onToggleProtection
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isEnabled) "PROTECTION ACTIVE" else "PROTECTION PAUSED",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = colors.ink
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isEnabled) "Quietly filtering spam notifications" else "SMS filtering is temporarily paused",
                fontSize = 12.sp,
                color = colors.sub
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tap toggle prompt (clean monochrome pill)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isEnabled) colors.accentSoft else colors.cardSoft)
                    .clickable { onToggleProtection() }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isEnabled) "TAP TO PAUSE" else "TAP TO RESUME",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = if (isEnabled) colors.accent else colors.inkSoft
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.lineSoft)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Clean Monochrome Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(label = "Shields", value = "$activeShieldCount Packs")
                StatColumn(label = "Mode", value = if (isStrict) "Strict" else "Normal")
                StatColumn(label = "Decisions", value = "$decisionsCount")
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    val colors = FTeleTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.ink)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, color = colors.sub)
    }
}

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

@Composable
private fun PermissionBanner(onOpenPermissionSettings: () -> Unit) {
    val colors = FTeleTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.block.copy(alpha = 0.4f)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.blockSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colors.block
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Permission Required",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )
                Text(
                    text = "Notification access is needed to mute spam SMS.",
                    fontSize = 11.sp,
                    color = colors.sub
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onOpenPermissionSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.block,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "Grant Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LogItemCard(
    log: LogEntry,
    senderRule: Rule?,
    onAllowSender: () -> Unit,
    onBlockSender: () -> Unit
) {
    val colors = FTeleTheme.colors
    val isAllowed = senderRule?.action == RuleAction.ALLOW
    val isBlocked = senderRule?.action == RuleAction.BLOCK

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Sender, Time ago, and Status Chip (Shield green vs Relative red)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (log.sender.isBlank()) "Unknown Sender" else log.sender,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimeAgo(log.time),
                        fontSize = 11.sp,
                        color = colors.sub
                    )
                }

                // Verdict Chip: Green (Shield) vs Red (Harmonized)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (log.muted) colors.blockSoft else colors.allowSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (log.muted) "Muted" else "Shown",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.muted) colors.block else colors.allow
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body text
            Text(
                text = log.text,
                fontSize = 13.sp,
                color = colors.inkSoft,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reason pill (clean monochrome)
            if (log.reason.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.cardSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = log.reason,
                        fontSize = 11.sp,
                        color = colors.sub,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Quick Actions: Monochrome by default, highlighting in Shield Green or Relative Red when selected
            if (log.sender.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Allow sender button
                    Button(
                        onClick = onAllowSender,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAllowed) colors.allowSoft else colors.cardSoft,
                            contentColor = if (isAllowed) colors.allow else colors.inkSoft
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (isAllowed) colors.allow else colors.sub
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAllowed) "Allowed" else "Allow sender",
                            fontSize = 11.sp,
                            fontWeight = if (isAllowed) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Block sender button
                    Button(
                        onClick = onBlockSender,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBlocked) colors.blockSoft else colors.cardSoft,
                            contentColor = if (isBlocked) colors.block else colors.inkSoft
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (isBlocked) colors.block else colors.sub
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBlocked) "Blocked" else "Block sender",
                            fontSize = 11.sp,
                            fontWeight = if (isBlocked) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeAgo(timeMs: Long): String {
    val s = Math.max(1, (System.currentTimeMillis() - timeMs) / 1000)
    return when {
        s < 60 -> "${s}s ago"
        s < 3600 -> "${s / 60}m ago"
        s < 86400 -> "${s / 3600}h ago"
        else -> "${s / 86400}d ago"
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Activity Screen Light")
@Composable
fun ActivityScreenPreviewLight() {
    FTeleTheme(themeSetting = "light") {
        ActivityScreen(
            isEnabled = true,
            hasPermission = true,
            activeShieldCount = 6,
            isStrict = false,
            logs = listOf(
                LogEntry("1", "telebirr", "You received ETB 100.00 from 0911...", System.currentTimeMillis() - 60000, false, "Protected: OTP & Security"),
                LogEntry("2", "6060", "Enkwan des alot! You won 10,000 ETB in lottery. Call 994", System.currentTimeMillis() - 120000, true, "Matched lottery shield")
            ),
            currentRules = emptyList(),
            onToggleProtection = {},
            onOpenPermissionSettings = {},
            onSimulateSms = {},
            onClearLogs = {},
            onAllowSender = {},
            onBlockSender = {}
        )
    }
}
