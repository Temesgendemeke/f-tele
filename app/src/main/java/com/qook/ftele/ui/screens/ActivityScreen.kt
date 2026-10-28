package com.qook.ftele.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qook.ftele.data.RuleStore
import com.qook.ftele.engine.Patterns
import com.qook.ftele.model.LogEntry
import com.qook.ftele.model.Rule
import com.qook.ftele.model.RuleAction
import com.qook.ftele.model.RuleTarget
import com.qook.ftele.ui.components.ThreeDimensionalShield
import com.qook.ftele.ui.theme.FTeleTheme

/**
 * Aggregated representation of repetitive messages (e.g. repeated promotional SMS).
 */
data class AggregatedLogEntry(
    val log: LogEntry,
    val count: Int,
    val firstSeenTime: Long,
    val lastSeenTime: Long
)

@OptIn(ExperimentalLayoutApi::class)
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
    onClearLogs: () -> Unit,
    onAllowPattern: (String) -> Unit,
    onBlockPattern: (String) -> Unit
) {
    val colors = FTeleTheme.colors

    // Dialog state for confirming/editing the message pattern
    var selectedLogForRule by remember { mutableStateOf<LogEntry?>(null) }
    var selectedAction by remember { mutableStateOf<RuleAction?>(null) }
    var patternDraft by remember { mutableStateOf("") }

    // Filter out background sync noise and sort recent to oldest
    val cleanLogs = remember(logs) {
        logs.filter { !RuleStore.isBackgroundNoise(it.sender, it.text) }
            .sortedByDescending { it.time }
    }

    // Authentic live protection stats
    val totalBlocked = remember(cleanLogs) { cleanLogs.count { it.muted } }
    val totalAllowed = remember(cleanLogs) { cleanLogs.count { !it.muted } }
    val totalProcessed = cleanLogs.size

    // Aggregate repetitive identical messages into compact multiplier chips
    val aggregatedLogs = remember(cleanLogs) {
        val groups = LinkedHashMap<String, MutableList<LogEntry>>()
        for (entry in cleanLogs) {
            val normText = Patterns.loose(entry.text).ifBlank { entry.text.trim().lowercase() }
            val key = "${entry.sender.trim().lowercase()}:::$normText"
            groups.getOrPut(key) { mutableListOf() }.add(entry)
        }
        groups.values.map { list ->
            AggregatedLogEntry(
                log = list.first(),
                count = list.size,
                firstSeenTime = list.last().time,
                lastSeenTime = list.first().time
            )
        }.sortedByDescending { it.lastSeenTime }
    }

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
                decisionsCount = totalProcessed,
                onToggleProtection = onToggleProtection
            )
        }

        // --- 2. Permission Banner (if missing) ---
        item {
            AnimatedVisibility(visible = !hasPermission) {
                PermissionBanner(onOpenPermissionSettings = onOpenPermissionSettings)
            }
        }

        // --- 3. Geometric Defense Metrics Bar ---
        item {
            DefenseStatsCard(
                totalBlocked = totalBlocked,
                totalAllowed = totalAllowed,
                totalProcessed = totalProcessed
            )
        }

        // --- 4. Section Header: Activity Logs & Controls ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity (${aggregatedLogs.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )

                if (aggregatedLogs.isNotEmpty()) {
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
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Clear", fontSize = 11.sp, color = colors.sub)
                    }
                }
            }
        }

        // --- 5. Message Logs List (Recent to Oldest with compact counts) ---
        if (aggregatedLogs.isEmpty()) {
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
                            text = "Incoming SMS notifications will appear here ordered from most recent. Repeated promos will show a compact count.",
                            fontSize = 12.sp,
                            color = colors.sub,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(aggregatedLogs, key = { it.log.id }) { item ->
                val log = item.log
                val looseText = Patterns.loose(log.text)
                val matchingPatternRule = currentRules.find {
                    it.target == RuleTarget.BODY && it.enabled && Patterns.loose(it.value).let { ruleVal ->
                        ruleVal.isNotEmpty() && (looseText.contains(ruleVal) || ruleVal.contains(looseText))
                    }
                }

                LogItemCard(
                    item = item,
                    matchingPatternRule = matchingPatternRule,
                    onAllowPatternClick = {
                        selectedLogForRule = log
                        selectedAction = RuleAction.ALLOW
                        patternDraft = extractCleanPattern(log.text)
                    },
                    onBlockPatternClick = {
                        selectedLogForRule = log
                        selectedAction = RuleAction.BLOCK
                        patternDraft = extractCleanPattern(log.text)
                    }
                )
            }
        }
    }

    // --- Pattern Rule Confirmation Dialog ---
    if (selectedLogForRule != null && selectedAction != null) {
        val currentLog = selectedLogForRule!!
        val isBlock = selectedAction == RuleAction.BLOCK
        val suggestions = remember(currentLog.text) { extractPatternSuggestions(currentLog.text) }

        AlertDialog(
            onDismissRequest = {
                selectedLogForRule = null
                selectedAction = null
            },
            title = {
                Text(
                    text = if (isBlock) "Block Message Pattern" else "Always Allow Pattern",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.ink
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBlock) {
                            "Mutes only messages matching this text pattern. Other messages from \"${currentLog.sender}\" (like transactions or receipts) will NOT be blocked."
                        } else {
                            "Always allows messages containing this text pattern."
                        },
                        fontSize = 12.sp,
                        color = colors.sub,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (suggestions.isNotEmpty()) {
                        Text(
                            text = "Quick phrase suggestions:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.sub
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            suggestions.forEach { chipText ->
                                val isSelected = patternDraft == chipText
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) colors.ink else colors.cardSoft)
                                        .clickable { patternDraft = chipText }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = chipText,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) colors.bg else colors.inkSoft
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = patternDraft,
                        onValueChange = { patternDraft = it },
                        label = { Text("Pattern to ${if (isBlock) "block" else "allow"}", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft,
                            focusedBorderColor = colors.line,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = colors.ink,
                            unfocusedTextColor = colors.ink
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = patternDraft.trim()
                        if (clean.isNotBlank()) {
                            if (isBlock) {
                                onBlockPattern(clean)
                            } else {
                                onAllowPattern(clean)
                            }
                        }
                        selectedLogForRule = null
                        selectedAction = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBlock) colors.block else colors.accent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBlock) "Block Pattern" else "Allow Pattern",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedLogForRule = null
                        selectedAction = null
                    }
                ) {
                    Text("Cancel", color = colors.sub, fontSize = 12.sp)
                }
            },
            containerColor = colors.card,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

/**
 * Geometric, non-slop statistics card displaying authentic defense metrics.
 */
@Composable
private fun DefenseStatsCard(
    totalBlocked: Int,
    totalAllowed: Int,
    totalProcessed: Int
) {
    val colors = FTeleTheme.colors
    val quietRate = if (totalProcessed > 0) (totalBlocked * 100 / totalProcessed) else 100
    val focusSavedMin = maxOf(0, (totalBlocked * 25) / 60)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEFENSE METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = colors.sub
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Metric Geometric Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric 1: Spam Silenced
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalBlocked.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (totalBlocked > 0) colors.block else colors.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SPAM SILENCED",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = colors.sub
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(colors.line)
                )

                // Metric 2: Clean Passed (OTPs & Contacts)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalAllowed.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "CLEAN PASSED",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = colors.sub
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(colors.line)
                )

                // Metric 3: Quiet Ratio or Focus Saved
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (focusSavedMin > 0) "~${focusSavedMin}m" else "${quietRate}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (focusSavedMin > 0) "FOCUS SAVED" else "QUIET RATE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = colors.sub
                    )
                }
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

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isEnabled) colors.accentSoft else colors.cardSoft)
                    .clickable { onToggleProtection() }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isEnabled) "TAP TO PAUSE" else "TAP TO RESUME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) colors.accent else colors.inkSoft
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
                    text = "Notification Access Required",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Allow F-Tele to detect and mute promo SMS notifications.",
                    fontSize = 11.sp,
                    color = colors.sub
                )
            }

            Button(
                onClick = onOpenPermissionSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.block,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = "Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LogItemCard(
    item: AggregatedLogEntry,
    matchingPatternRule: Rule?,
    onAllowPatternClick: () -> Unit,
    onBlockPatternClick: () -> Unit
) {
    val colors = FTeleTheme.colors
    val log = item.log
    val count = item.count

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

                    // Compact multiplier badge for repeated messages (e.g. ×3)
                    if (count > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.cardSoft)
                                .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "×$count",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (log.muted) colors.block else colors.accent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatRelativeTime(log.time),
                        fontSize = 11.sp,
                        color = colors.sub
                    )
                }

                // Status Badge: Muted (Red) or Shown (Green)
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

            // If an active custom rule matches this pattern, display it
            if (matchingPatternRule != null) {
                val isRuleBlock = matchingPatternRule.action == RuleAction.BLOCK
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isRuleBlock) colors.blockSoft else colors.accentSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Active Rule: ${if (isRuleBlock) "Blocked" else "Allowed"} by \"${matchingPatternRule.value}\"",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isRuleBlock) colors.block else colors.accent
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Quick pattern actions: Always Allow vs Block Pattern
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Always Allow Pattern Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, colors.line, RoundedCornerShape(6.dp))
                        .clickable { onAllowPatternClick() }
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

                Spacer(modifier = Modifier.width(8.dp))

                // Block Pattern Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, colors.line, RoundedCornerShape(6.dp))
                        .clickable { onBlockPatternClick() }
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
                            text = "Block Pattern",
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

private fun extractCleanPattern(text: String): String {
    val clean = text.trim()
    if (clean.length <= 80) return clean
    val sentence = clean.split(Regex("[\\n።.!?]")).map { it.trim() }.firstOrNull { it.length >= 8 } ?: clean
    return if (sentence.length > 80) sentence.take(80).trim() else sentence
}

private fun extractPatternSuggestions(text: String): List<String> {
    val list = mutableListOf<String>()
    val clean = text.trim()
    val fullSentence = extractCleanPattern(clean)
    if (fullSentence.isNotBlank()) {
        list.add(fullSentence)
    }

    // Extract notable clauses
    val parts = clean.split(Regex("[\\n።.!?፣,]"))
        .map { it.trim() }
        .filter { it.length in 6..40 && it != fullSentence }
    list.addAll(parts.take(3))

    return list.distinct()
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
