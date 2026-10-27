package com.example.ftele.ui.screens

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
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.GppBad
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Shield Card
        item {
            HeroShieldCard(
                isEnabled = isEnabled,
                activeShieldCount = activeShieldCount,
                isStrict = isStrict,
                decisionsCount = logs.size,
                onToggleProtection = onToggleProtection
            )
        }

        // Notification Permission Warning Banner
        item {
            AnimatedVisibility(visible = !hasPermission) {
                PermissionBanner(onOpenPermissionSettings = onOpenPermissionSettings)
            }
        }

        // Section Header: Activity Log Controls
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity (${logs.size})",
                    fontSize = 17.sp,
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
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.accent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Simulate", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    if (logs.isNotEmpty()) {
                        Button(
                            onClick = onClearLogs,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.cardSoft,
                                contentColor = colors.sub
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Clear activity log",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Empty state or list of logs
        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.cardSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = colors.sub
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No SMS processed yet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "When incoming SMS notifications arrive, F-Tele will evaluate them and record decisions here.",
                            fontSize = 13.sp,
                            color = colors.sub,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onSimulateSms,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Simulate Sample SMS", fontWeight = FontWeight.SemiBold)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Clickable Ring & Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) colors.accentSoft else colors.cardSoft)
                    .border(
                        width = 3.dp,
                        color = if (isEnabled) colors.accent else colors.line,
                        shape = CircleShape
                    )
                    .clickable { onToggleProtection() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Outlined.GppGood else Icons.Outlined.GppBad,
                    contentDescription = if (isEnabled) "Protection Active" else "Protection Paused",
                    modifier = Modifier.size(52.dp),
                    tint = if (isEnabled) colors.accent else colors.sub
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isEnabled) "PROTECTION ACTIVE" else "PROTECTION PAUSED",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = if (isEnabled) colors.ink else colors.warning
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isEnabled) "F-Tele is quietly guarding your notifications" else "Spam SMS notifications will not be muted",
                fontSize = 13.sp,
                color = colors.sub
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isEnabled) colors.allowSoft else colors.warningSoft)
                    .clickable { onToggleProtection() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isEnabled) "TAP TO PAUSE" else "TAP TO RESUME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) colors.allow else colors.warning
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.lineSoft)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(label = "Shields", value = "$activeShieldCount Packs", valueColor = colors.ink)
                StatColumn(label = "Mode", value = if (isStrict) "Strict" else "Normal", valueColor = if (isStrict) colors.warning else colors.accent)
                StatColumn(label = "Decisions", value = "$decisionsCount", valueColor = colors.ink)
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, valueColor: Color) {
    val colors = FTeleTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 12.sp, color = colors.sub)
    }
}

@Composable
private fun PermissionBanner(onOpenPermissionSettings: () -> Unit) {
    val colors = FTeleTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.warningSoft),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.warning.copy(alpha = 0.4f)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = colors.warning
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Permission Required",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )
                Text(
                    text = "Enable notification access so F-Tele can filter spam.",
                    fontSize = 12.sp,
                    color = colors.inkSoft
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenPermissionSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.warning,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "Grant Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Sender, Time ago, and Status Chip
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimeAgo(log.time),
                        fontSize = 12.sp,
                        color = colors.sub
                    )
                }

                // Verdict Chip
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

            Spacer(modifier = Modifier.height(8.dp))

            // Body text
            Text(
                text = log.text,
                fontSize = 13.sp,
                color = colors.inkSoft,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reason pill
            if (log.reason.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.cardSoft)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
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

            // Quick Action Buttons
            if (log.sender.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAllowed = senderRule?.action == RuleAction.ALLOW
                    val isBlocked = senderRule?.action == RuleAction.BLOCK

                    // Allow sender button
                    Button(
                        onClick = onAllowSender,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAllowed) colors.allowSoft else colors.cardSoft,
                            contentColor = colors.allow
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colors.allow
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAllowed) "Allowed" else "Allow sender",
                            fontSize = 12.sp,
                            fontWeight = if (isAllowed) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Block sender button
                    Button(
                        onClick = onBlockSender,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBlocked) colors.blockSoft else colors.cardSoft,
                            contentColor = colors.block
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colors.block
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBlocked) "Blocked" else "Block sender",
                            fontSize = 12.sp,
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
