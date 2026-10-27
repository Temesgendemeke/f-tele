package com.example.ftele.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ftele.ui.theme.FTeleTheme

@Composable
fun SettingsScreen(
    hasPermission: Boolean,
    currentMode: String,
    themeMode: String,
    onOpenPermissionSettings: () -> Unit,
    onSelectMode: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onClearAllLogs: () -> Unit
) {
    val colors = FTeleTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Notification Permission Card ---
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (hasPermission) colors.allowSoft else colors.warningSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasPermission) Icons.Outlined.Check else Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (hasPermission) colors.allow else colors.warning
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Notification Access",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.ink
                                )
                                Text(
                                    text = if (hasPermission) "Active & operational" else "Action required to mute SMS",
                                    fontSize = 12.sp,
                                    color = colors.sub
                                )
                            }
                        }

                        // Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (hasPermission) colors.allowSoft else colors.blockSoft)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (hasPermission) "ACTIVE" else "REQUIRED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasPermission) colors.allow else colors.block
                            )
                        }
                    }

                    if (!hasPermission) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenPermissionSettings,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Grant Notification Access", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 2. Filtering Mode Card ---
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
                        text = "Filtering Strategy",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose how strictly F-Tele handles non-categorized SMS.",
                        fontSize = 12.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Segmented Button: Blocklist vs Allowlist
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cardSoft)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ModePill(
                            label = "Normal (Blocklist)",
                            selected = currentMode == "blocklist",
                            activeColor = colors.accent,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectMode("blocklist") }
                        )
                        ModePill(
                            label = "Strict (Allowlist)",
                            selected = currentMode == "allowlist",
                            activeColor = colors.warning,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectMode("allowlist") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (currentMode == "blocklist") {
                            "Normal mode: only the senders and words matching blocked rules are muted."
                        } else {
                            "Strict mode: every SMS is muted unless you explicitly allow it or it's protected."
                        },
                        fontSize = 12.sp,
                        color = colors.inkSoft,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // --- 3. Theme Mode Card ---
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
                        text = "Appearance",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Select application theme.",
                        fontSize = 12.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cardSoft)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ThemePill(
                            label = "System",
                            icon = Icons.Outlined.PhoneAndroid,
                            selected = themeMode == "system",
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectTheme("system") }
                        )
                        ThemePill(
                            label = "Light",
                            icon = Icons.Outlined.LightMode,
                            selected = themeMode == "light",
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectTheme("light") }
                        )
                        ThemePill(
                            label = "Dark",
                            icon = Icons.Outlined.DarkMode,
                            selected = themeMode == "dark",
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectTheme("dark") }
                        )
                    }
                }
            }
        }

        // --- 4. Data Management ---
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
                        text = "Data Management",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Clear past activity logs. Your custom rules and shield settings are preserved.",
                        fontSize = 12.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onClearAllLogs,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cardSoft,
                            contentColor = colors.block
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Clear Activity Log", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 5. Privacy Guarantee Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardSoft),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = colors.accent
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% Offline & Private",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "No internet permissions. Zero analytics. All evaluations run locally on your device.",
                            fontSize = 11.sp,
                            color = colors.sub,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModePill(
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
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else colors.inkSoft
        )
    }
}

@Composable
private fun ThemePill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = FTeleTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) colors.accent else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (selected) Color.White else colors.inkSoft
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else colors.inkSoft
            )
        }
    }
}
