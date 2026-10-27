package com.qook.ftele.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qook.ftele.engine.PatternPacks
import com.qook.ftele.engine.Patterns
import com.qook.ftele.model.MatchMode
import com.qook.ftele.model.Rule
import com.qook.ftele.model.RuleAction
import com.qook.ftele.model.RuleTarget
import com.qook.ftele.ui.theme.FTeleTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RulesScreen(
    customRules: List<Rule>,
    effectiveRules: List<Rule>,
    currentMode: String,
    onSaveRule: (Rule) -> Unit,
    onToggleRule: (Rule) -> Unit,
    onRemoveRule: (Rule) -> Unit
) {
    val colors = FTeleTheme.colors
    val focusManager = LocalFocusManager.current

    // Live test state
    var testSender by remember { mutableStateOf("") }
    var testText by remember { mutableStateOf("") }

    // Draft rule state
    var draftValue by remember { mutableStateOf("") }
    var draftSender by remember { mutableStateOf("") }
    var draftTarget by remember { mutableStateOf(RuleTarget.BODY) }
    var draftMode by remember { mutableStateOf(MatchMode.CONTAINS) }
    var draftAction by remember { mutableStateOf(RuleAction.BLOCK) }
    var regexError by remember { mutableStateOf(false) }

    val liveDecision = remember(testSender, testText, effectiveRules, currentMode) {
        Patterns.decide(effectiveRules, currentMode, testSender, testText)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Live Tester Card ---
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
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Tester",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )

                        if (testSender.isNotBlank() || testText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (liveDecision.mute) colors.blockSoft else colors.accentSoft)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (liveDecision.mute) "Would mute" else "Would show",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (liveDecision.mute) colors.block else colors.accent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (testSender.isBlank() && testText.isBlank()) {
                            "Type a sender ID or message to preview decisions live."
                        } else {
                            liveDecision.reason
                        },
                        fontSize = 12.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = testSender,
                        onValueChange = { testSender = it },
                        placeholder = { Text("Sender ID (e.g. 127, Telebirr, CBE)", fontSize = 13.sp, color = colors.sub) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft,
                            focusedBorderColor = colors.line,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = colors.ink,
                            unfocusedTextColor = colors.ink
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testText,
                        onValueChange = { testText = it },
                        placeholder = { Text("SMS Message text to evaluate live...", fontSize = 13.sp, color = colors.sub) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
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
            }
        }

        // --- 2. Add Custom Rule Form ---
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
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Add Custom Rule",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Filter specific keywords, sender IDs, or regex patterns.",
                        fontSize = 12.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Segment: Block (Relative Red) vs Allow (Shield Green)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cardSoft)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (draftAction == RuleAction.BLOCK) colors.block else Color.Transparent)
                                .clickable { draftAction = RuleAction.BLOCK }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Block",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (draftAction == RuleAction.BLOCK) Color.White else colors.inkSoft
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (draftAction == RuleAction.ALLOW) colors.accent else Color.Transparent)
                                .clickable { draftAction = RuleAction.ALLOW }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Allow",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (draftAction == RuleAction.ALLOW) Color.White else colors.inkSoft
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target Segment: Body vs Sender vs Both
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cardSoft)
                            .padding(2.dp)
                    ) {
                        listOf(RuleTarget.BODY, RuleTarget.SENDER, RuleTarget.BOTH).forEach { target ->
                            val isSelected = draftTarget == target
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) colors.ink else Color.Transparent)
                                    .clickable { draftTarget = target }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = target.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.bg else colors.inkSoft
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Value Input
                    OutlinedTextField(
                        value = draftValue,
                        onValueChange = {
                            draftValue = it
                            regexError = false
                        },
                        placeholder = {
                            Text(
                                text = if (draftTarget == RuleTarget.SENDER) "Sender ID (e.g. 605, Safaricom)" else "Keyword, phrase, or pattern...",
                                fontSize = 13.sp,
                                color = colors.sub
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft,
                            focusedBorderColor = colors.line,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = colors.ink,
                            unfocusedTextColor = colors.ink
                        )
                    )

                    // Optional Sender Filter if target != SENDER
                    if (draftTarget != RuleTarget.SENDER) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = draftSender,
                            onValueChange = { draftSender = it },
                            placeholder = { Text("From Sender ID (Optional, e.g. 6060)", fontSize = 13.sp, color = colors.sub) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Match Mode Selector Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MatchMode.entries.forEach { mode ->
                            val isSelected = draftMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) colors.ink else colors.cardSoft)
                                    .clickable { draftMode = mode }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.bg else colors.inkSoft
                                )
                            }
                        }
                    }

                    if (regexError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Invalid regular expression pattern", fontSize = 11.sp, color = colors.block)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (draftValue.isBlank()) return@Button
                            if (draftMode == MatchMode.REGEX) {
                                try {
                                    Regex(draftValue)
                                } catch (_: Exception) {
                                    regexError = true
                                    return@Button
                                }
                            }

                            onSaveRule(
                                Rule(
                                    id = PatternPacks.newRuleId(),
                                    action = draftAction,
                                    target = draftTarget,
                                    mode = draftMode,
                                    value = draftValue.trim(),
                                    senderFilter = draftSender.trim().ifBlank { null },
                                    category = "Custom",
                                    enabled = true
                                )
                            )
                            draftValue = ""
                            draftSender = ""
                            focusManager.clearFocus()
                        },
                        enabled = draftValue.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.ink,
                            contentColor = colors.bg,
                            disabledContainerColor = colors.cardSoft,
                            disabledContentColor = colors.sub
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save Rule", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- 3. Custom Rules List ---
        item {
            Text(
                text = "Your Rules (${customRules.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        if (customRules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No custom rules yet. Rules you add above will appear here.",
                            fontSize = 12.sp,
                            color = colors.sub
                        )
                    }
                }
            }
        } else {
            items(customRules, key = { it.id }) { rule ->
                CustomRuleItem(
                    rule = rule,
                    onToggle = { onToggleRule(rule) },
                    onDelete = { onRemoveRule(rule) }
                )
            }
        }
    }
}

@Composable
private fun CustomRuleItem(
    rule: Rule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = FTeleTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action pill: Block (Red) or Allow (Green)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (rule.action == RuleAction.BLOCK) colors.blockSoft else colors.accentSoft)
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = rule.action.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rule.action == RuleAction.BLOCK) colors.block else colors.accent
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${rule.target.label} · ${rule.mode.label}",
                        fontSize = 11.sp,
                        color = colors.sub
                    )
                    if (!rule.senderFilter.isNullOrBlank()) {
                        Text(
                            text = " · Sender: ${rule.senderFilter}",
                            fontSize = 11.sp,
                            color = colors.sub
                        )
                    }
                }
            }

            Switch(
                checked = rule.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.accent,
                    uncheckedThumbColor = colors.sub,
                    uncheckedTrackColor = colors.cardSoft
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete",
                    modifier = Modifier.size(16.dp),
                    tint = colors.sub
                )
            }
        }
    }
}
