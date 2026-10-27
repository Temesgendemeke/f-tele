package com.example.ftele.ui.screens

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
import com.example.ftele.engine.PatternPacks
import com.example.ftele.engine.Patterns
import com.example.ftele.model.MatchMode
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget
import com.example.ftele.ui.theme.FTeleTheme

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

                        // Live verdict chip (Shield green vs Relative red)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (liveDecision.mute) colors.blockSoft else colors.allowSoft)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (liveDecision.mute) "Would mute" else "Would show",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (liveDecision.mute) colors.block else colors.allow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = liveDecision.reason,
                        fontSize = 11.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = testSender,
                        onValueChange = { testSender = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Sender ID (e.g. 127, Telebirr, CBE)", fontSize = 12.sp, color = colors.sub) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.ink,
                            unfocusedBorderColor = colors.line,
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft,
                            focusedTextColor = colors.ink,
                            unfocusedTextColor = colors.ink
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testText,
                        onValueChange = { testText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("SMS Message text to evaluate live...", fontSize = 12.sp, color = colors.sub) },
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.ink,
                            unfocusedBorderColor = colors.line,
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft,
                            focusedTextColor = colors.ink,
                            unfocusedTextColor = colors.ink
                        ),
                        shape = RoundedCornerShape(8.dp)
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Filter specific keywords, sender IDs, or regex patterns.",
                        fontSize = 11.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Selector: Block (Relative Red) / Allow (Shield Green)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cardSoft)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ActionPill(
                            label = "Block",
                            selected = draftAction == RuleAction.BLOCK,
                            activeColor = colors.block,
                            modifier = Modifier.weight(1f),
                            onClick = { draftAction = RuleAction.BLOCK }
                        )
                        ActionPill(
                            label = "Allow",
                            selected = draftAction == RuleAction.ALLOW,
                            activeColor = colors.accent,
                            modifier = Modifier.weight(1f),
                            onClick = { draftAction = RuleAction.ALLOW }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target Selector: Body / Sender / Both (Clean Monochrome)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cardSoft)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MonochromePill(
                            label = "Body",
                            selected = draftTarget == RuleTarget.BODY,
                            modifier = Modifier.weight(1f),
                            onClick = { draftTarget = RuleTarget.BODY }
                        )
                        MonochromePill(
                            label = "Sender",
                            selected = draftTarget == RuleTarget.SENDER,
                            modifier = Modifier.weight(1f),
                            onClick = { draftTarget = RuleTarget.SENDER }
                        )
                        MonochromePill(
                            label = "Both",
                            selected = draftTarget == RuleTarget.BOTH,
                            modifier = Modifier.weight(1f),
                            onClick = { draftTarget = RuleTarget.BOTH }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Value input
                    OutlinedTextField(
                        value = draftValue,
                        onValueChange = {
                            draftValue = it
                            regexError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Keyword, phrase, or pattern...", fontSize = 12.sp, color = colors.sub) },
                        singleLine = true,
                        isError = regexError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.ink,
                            unfocusedBorderColor = colors.line,
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft,
                            focusedTextColor = colors.ink,
                            unfocusedTextColor = colors.ink
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    AnimatedVisibility(visible = regexError) {
                        Text(
                            text = "Invalid regex syntax",
                            fontSize = 11.sp,
                            color = colors.block,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Optional Sender filter
                    OutlinedTextField(
                        value = draftSender,
                        onValueChange = { draftSender = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("From Sender ID (Optional, e.g. 6060)", fontSize = 12.sp, color = colors.sub) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.ink,
                            unfocusedBorderColor = colors.line,
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft,
                            focusedTextColor = colors.ink,
                            unfocusedTextColor = colors.ink
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Match Mode Selector Chips (Clean Monochrome)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MatchMode.values().forEach { mode ->
                            val isSelected = draftMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) colors.ink else colors.cardSoft)
                                    .clickable { draftMode = mode }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Save Rule Button (Clean Monochrome Primary)
                    Button(
                        onClick = {
                            val trimmedValue = draftValue.trim()
                            if (trimmedValue.isBlank()) return@Button

                            if (draftMode == MatchMode.REGEX) {
                                try {
                                    Regex(trimmedValue)
                                } catch (_: Exception) {
                                    regexError = true
                                    return@Button
                                }
                            }
                            regexError = false

                            val newRule = Rule(
                                id = PatternPacks.newRuleId(),
                                action = draftAction,
                                target = draftTarget,
                                mode = draftMode,
                                value = trimmedValue,
                                senderFilter = draftSender.trim().ifBlank { null },
                                category = "Custom Rule",
                                enabled = true
                            )

                            onSaveRule(newRule)
                            draftValue = ""
                            draftSender = ""
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.ink,
                            contentColor = colors.bg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save Rule", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 3. Custom Rules Section Header ---
        item {
            Text(
                text = "Custom Rules (${customRules.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // --- 4. Custom Rules List or Empty ---
        if (customRules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No custom rules yet",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.sub
                        )
                    }
                }
            }
        } else {
            items(customRules, key = { it.id }) { rule ->
                CustomRuleCard(
                    rule = rule,
                    onToggle = { onToggleRule(rule) },
                    onDelete = { onRemoveRule(rule) }
                )
            }
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    selected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = FTeleTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) activeColor else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else colors.inkSoft
        )
    }
}

@Composable
private fun MonochromePill(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = FTeleTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) colors.ink else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colors.bg else colors.inkSoft
        )
    }
}

@Composable
private fun CustomRuleCard(
    rule: Rule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = FTeleTheme.colors
    val isAllow = rule.action == RuleAction.ALLOW

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action chip: Shield green vs Relative red
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (isAllow) colors.allowSoft else colors.blockSoft)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isAllow) "Allow" else "Block",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllow) colors.allow else colors.block
                        )
                    }

                    Text(
                        text = rule.value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Target & mode metadata
                val senderPart = if (!rule.senderFilter.isNullOrBlank()) "From [${rule.senderFilter}] · " else ""
                Text(
                    text = "$senderPart${rule.target.label} · ${rule.mode.label}",
                    fontSize = 11.sp,
                    color = colors.sub
                )
            }

            // Switch
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

            Spacer(modifier = Modifier.width(6.dp))

            // Delete button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Remove rule",
                    modifier = Modifier.size(18.dp),
                    tint = colors.sub
                )
            }
        }
    }
}
