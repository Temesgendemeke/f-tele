package com.qook.ftele.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qook.ftele.engine.Patterns
import com.qook.ftele.model.LogEntry
import com.qook.ftele.model.Rule
import com.qook.ftele.model.RuleAction
import com.qook.ftele.model.RuleTarget
import com.qook.ftele.ui.components.ThreeDimensionalShield
import com.qook.ftele.ui.theme.FTeleTheme

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
        // --- 1. Hero 3D Shield Status Card ---
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
                                contentDescription = "Clear",
                                modifier = Modifier.size(15.dp),
                                tint = colors.sub
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
                            text = "Incoming SMS will be evaluated offline against active shields, user preferences, and custom rules.",
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
                horizontalArrangement = Arrangement.SpaceAround
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
                    text = "Notification Access Required",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Grant notification listener access to allow local offline SMS filtering.",
                    fontSize = 11.sp,
                    color = colors.sub
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onOpenPermissionSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.block,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "Enable", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = log.sender,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatRelativeTime(log.time),
                        fontSize = 11.sp,
                        color = colors.sub
                    )
                }

                // Status Badge: Muted (State Red) or Shown (App Shield Green)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (log.muted) colors.blockSoft else colors.accentSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (log.muted) "Muted" else "Shown",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.muted) colors.block else colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = log.text,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = colors.inkSoft
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reason pill
            if (log.reason.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.cardSoft)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.reason,
                        fontSize = 11.sp,
                        color = colors.sub
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Quick sender rule actions (Clean monochrome pills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (senderRule == null || senderRule.action != RuleAction.ALLOW) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, colors.line, RoundedCornerShape(6.dp))
                            .clickable { onAllowSender() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = colors.accent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Always Allow",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.ink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (senderRule == null || senderRule.action != RuleAction.BLOCK) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, colors.line, RoundedCornerShape(6.dp))
                            .clickable { onBlockSender() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = colors.block
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Always Block",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.ink
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return "just now"
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}
