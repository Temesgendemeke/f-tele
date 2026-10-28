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
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PlayArrow
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
import androidx.compose.runtime.mutableIntStateOf
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
    var testSender by remember { mutableStateOf("ethiotel") }
    var testText by remember { mutableStateOf("ውድ ደንበኛችን ልዩ ቅናሽ! የ 50% ቦነስ የኢንተርኔት ጥቅል ይግዙ") }

    // Simple rule creation state: 0 = Block Sender, 1 = Block Word, 2 = Always Allow
    var selectedTab by remember { mutableIntStateOf(0) }
    var simpleValue by remember { mutableStateOf("") }
    var showAdvanced by remember { mutableStateOf(false) }
    var advancedMode by remember { mutableStateOf(MatchMode.CONTAINS) }

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
        // --- 2. Simple Rule Creator Card ---
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
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Block a specific spam sender or phrase in 1 tap.",
                        fontSize = 12.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simple 3-Option Segment
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cardSoft)
                            .padding(3.dp)
                    ) {
                        listOf(
                            "Block Sender",
                            "Block Word",
                            "Always Allow"
                        ).forEachIndexed { idx, label ->
                            val isSelected = selectedTab == idx
                            val bgColor = when {
                                !isSelected -> Color.Transparent
                                idx == 0 || idx == 1 -> colors.block
                                else -> colors.accent
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bgColor)
                                    .clickable { selectedTab = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else colors.inkSoft
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input Box
                    OutlinedTextField(
                        value = simpleValue,
                        onValueChange = { simpleValue = it },
                        placeholder = {
                            Text(
                                text = when (selectedTab) {
                                    0 -> "Sender name or number (e.g. 444, 605)"
                                    1 -> "Word or phrase (e.g. loan, ቅናሽ, discount)"
                                    else -> "Sender or phrase to always allow (e.g. CBE, School)"
                                },
                                fontSize = 12.sp,
                                color = colors.sub
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Advanced Options Toggle (Kept hidden unless wanted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvanced = !showAdvanced }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showAdvanced) "Hide Advanced Options" else "Show Advanced Options (Regex, Prefix)",
                            fontSize = 11.sp,
                            color = colors.sub,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (showAdvanced) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.sub
                        )
                    }

                    AnimatedVisibility(visible = showAdvanced) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "Match Type:",
                                fontSize = 11.sp,
                                color = colors.sub,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MatchMode.entries.forEach { mode ->
                                    val isSelected = advancedMode == mode
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) colors.ink else colors.cardSoft)
                                            .clickable { advancedMode = mode }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
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
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (simpleValue.isBlank()) return@Button
                            val rule = Rule(
                                id = PatternPacks.newRuleId(),
                                action = if (selectedTab == 2) RuleAction.ALLOW else RuleAction.BLOCK,
                                target = if (selectedTab == 0) RuleTarget.SENDER else RuleTarget.BODY,
                                mode = if (showAdvanced) advancedMode else MatchMode.CONTAINS,
                                value = simpleValue.trim(),
                                category = when (selectedTab) {
                                    0 -> "Block Sender: ${simpleValue.trim()}"
                                    1 -> "Block Keyword: ${simpleValue.trim()}"
                                    else -> "Always Allow: ${simpleValue.trim()}"
                                },
                                enabled = true
                            )
                            onSaveRule(rule)
                            simpleValue = ""
                            focusManager.clearFocus()
                        },
                        enabled = simpleValue.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.ink,
                            contentColor = colors.bg,
                            disabledContainerColor = colors.cardSoft,
                            disabledContentColor = colors.sub
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 11.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (selectedTab) {
                                0 -> "Block This Sender"
                                1 -> "Block This Keyword"
                                else -> "Always Allow This"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                            text = "No custom rules yet. Rules you add above or via 'Block Pattern' in Activity will appear here.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = colors.sub,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(customRules, key = { it.id }) { rule ->
                RuleCard(
                    rule = rule,
                    onToggle = { onToggleRule(rule) },
                    onDelete = { onRemoveRule(rule) }
                )
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: Rule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = FTeleTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action Badge (BLOCK in red, ALLOW in green)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (rule.action == RuleAction.BLOCK) colors.blockSoft else colors.accentSoft)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = rule.action.value.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rule.action == RuleAction.BLOCK) colors.block else colors.accent
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rule.enabled) colors.ink else colors.sub
                )
                Text(
                    text = "${rule.target.label} • ${rule.category ?: "Custom"}",
                    fontSize = 11.sp,
                    color = colors.sub
                )
            }

            Switch(
                checked = rule.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = if (rule.action == RuleAction.BLOCK) colors.block else colors.accent,
                    uncheckedThumbColor = colors.sub,
                    uncheckedTrackColor = colors.cardSoft
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

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
                    modifier = Modifier.size(18.dp),
                    tint = colors.sub
                )
            }
        }
    }
}
