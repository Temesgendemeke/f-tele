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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Rule
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.text.input.ImeAction
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Live Tester Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Rule Tester",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )

                        // Live verdict chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (liveDecision.mute) colors.blockSoft else colors.allowSoft)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (liveDecision.mute) "Would mute" else "Would show",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (liveDecision.mute) colors.block else colors.allow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = liveDecision.reason,
                        fontSize = 12.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = testSender,
                        onValueChange = { testSender = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Sender (e.g. 127, Telebirr, CBE)", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.line,
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testText,
                        onValueChange = { testText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("SMS Message text to test...", fontSize = 13.sp) },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.line,
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // --- 2. Add Rule Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Add Custom Rule",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Filter specific keywords, sender numbers, or regular expressions.",
                        fontSize = 12.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Selector: Block / Allow
                    Text(text = "Action", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.inkSoft)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cardSoft)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SegmentPill(
                            label = "Block",
                            selected = draftAction == RuleAction.BLOCK,
                            activeColor = colors.block,
                            modifier = Modifier.weight(1f),
                            onClick = { draftAction = RuleAction.BLOCK }
                        )
                        SegmentPill(
                            label = "Allow",
                            selected = draftAction == RuleAction.ALLOW,
                            activeColor = colors.allow,
                            modifier = Modifier.weight(1f),
                            onClick = { draftAction = RuleAction.ALLOW }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target Selector: Body / Sender / Both
                    Text(text = "Target Field", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.inkSoft)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cardSoft)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SegmentPill(
                            label = "Body",
                            selected = draftTarget == RuleTarget.BODY,
                            activeColor = colors.accent,
                            modifier = Modifier.weight(1f),
                            onClick = { draftTarget = RuleTarget.BODY }
                        )
                        SegmentPill(
                            label = "Sender",
                            selected = draftTarget == RuleTarget.SENDER,
                            activeColor = colors.accent,
                            modifier = Modifier.weight(1f),
                            onClick = { draftTarget = RuleTarget.SENDER }
                        )
                        SegmentPill(
                            label = "Both",
                            selected = draftTarget == RuleTarget.BOTH,
                            activeColor = colors.accent,
                            modifier = Modifier.weight(1f),
                            onClick = { draftTarget = RuleTarget.BOTH }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Keyword / Pattern value
                    OutlinedTextField(
                        value = draftValue,
                        onValueChange = {
                            draftValue = it
                            regexError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Word, phrase, or pattern...", fontSize = 13.sp) },
                        singleLine = true,
                        isError = regexError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.line,
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    AnimatedVisibility(visible = regexError) {
                        Text(
                            text = "Invalid regular expression pattern",
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
                        placeholder = { Text("From Sender (Optional, e.g. 6060)", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.line,
                            focusedContainerColor = colors.cardSoft,
                            unfocusedContainerColor = colors.cardSoft
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Match Mode Selector Chips
                    Text(text = "Match Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.inkSoft)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MatchMode.values().forEach { mode ->
                            val isSelected = draftMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accent else colors.cardSoft)
                                    .clickable { draftMode = mode }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else colors.ink
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Add Rule Button
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
                            containerColor = colors.accent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save Rule", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 3. Custom Rules Section Header ---
        item {
            Text(
                text = "Custom Rules (${customRules.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // --- 4. Custom Rules List or Empty ---
        if (customRules.isEmpty()) {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No custom rules added",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.sub
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rules you add above will appear here.",
                            fontSize = 12.sp,
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
private fun SegmentPill(
    label: String,
    selected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = FTeleTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) activeColor else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else colors.inkSoft
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isAllow) colors.allowSoft else colors.blockSoft)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isAllow) "Allow" else "Block",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllow) colors.allow else colors.block
                        )
                    }

                    Text(
                        text = rule.value,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Target & mode metadata
                val senderPart = if (!rule.senderFilter.isNullOrBlank()) "From [${rule.senderFilter}] · " else ""
                Text(
                    text = "$senderPart${rule.target.label} · ${rule.mode.label}",
                    fontSize = 12.sp,
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

            Spacer(modifier = Modifier.width(8.dp))

            // Delete button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Remove rule",
                    modifier = Modifier.size(20.dp),
                    tint = colors.sub
                )
            }
        }
    }
}
