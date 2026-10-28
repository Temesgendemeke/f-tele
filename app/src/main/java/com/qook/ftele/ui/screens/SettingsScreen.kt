package com.qook.ftele.ui.screens

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
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneAndroid
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qook.ftele.ui.theme.FTeleTheme

@Composable
fun SettingsScreen(
    hasPermission: Boolean,
    isIgnoringBattery: Boolean,
    hasContactPermission: Boolean,
    onRequestContactPermission: () -> Unit,
    hasSmsPermission: Boolean = true,
    onRequestSmsPermission: () -> Unit = {},
    deviceTip: String,
    currentMode: String,
    themeMode: String,
    onOpenPermissionSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenAutostartSettings: () -> Unit,
    onChangeMode: (String) -> Unit,
    onChangeTheme: (String) -> Unit,
    onClearLogs: () -> Unit
) {
    val colors = FTeleTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Notification Permission Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (hasPermission) colors.line else colors.block.copy(alpha = 0.5f)
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (hasPermission) colors.accentSoft else colors.blockSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasPermission) Icons.Outlined.Check else Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (hasPermission) colors.accent else colors.block
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification Access",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hasPermission) "Active Watching for spam" else "Action needed to block SMS",
                                fontSize = 11.sp,
                                color = if (hasPermission) colors.sub else colors.block
                            )
                        }

                        if (!hasPermission) {
                            Button(
                                onClick = onOpenPermissionSettings,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.block,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Grant Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.accentSoft)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                )
                            }
                        }
                    }

                    // Easy 3-step guide if permission is missing
                    if (!hasPermission) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.blockSoft.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "How to enable:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.block
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "1. Tap 'Grant Access' above.\n2. Find 'F-Tele' and toggle it ON.\n(Android 13+: If switch says 'Restricted setting', open App Info > 3 dots ⋮ in top corner > tap 'Allow restricted settings' first)",
                                    fontSize = 11.sp,
                                    color = colors.ink,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Keep Running in Background Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder()
                    .copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isIgnoringBattery) colors.accentSoft else colors.cardSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isIgnoringBattery) Icons.Outlined.Check else Icons.Outlined.BatteryAlert,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isIgnoringBattery) colors.accent else colors.sub
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Background Protection",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isIgnoringBattery) "Unrestricted (Safe from battery killer)" else "May sleep when screen is locked",
                                fontSize = 11.sp,
                                color = colors.sub
                            )
                        }

                        Button(
                            onClick = onOpenBatterySettings,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIgnoringBattery) colors.cardSoft else colors.ink,
                                contentColor = if (isIgnoringBattery) colors.ink else colors.bg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isIgnoringBattery) "Review" else "Keep Running",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (deviceTip.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cardSoft)
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = colors.sub
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Tip for your phone:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.ink
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = deviceTip,
                                    fontSize = 11.sp,
                                    color = colors.sub,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onOpenAutostartSettings,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.ink,
                                        contentColor = colors.bg
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Open Phone Autostart Settings",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Contacts Protection Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder()
                    .copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = colors.accent
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Contacts Protection",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All senders from your contacts are ALWAYS allowed",
                                fontSize = 11.sp,
                                color = colors.sub
                            )
                        }

                        if (!hasContactPermission) {
                            Button(
                                onClick = onRequestContactPermission,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.ink,
                                    contentColor = colors.bg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(text = "Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.accentSoft)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "F-Tele automatically allows any SMS from your phonebook contacts so you never miss messages from family, friends, or work.",
                        fontSize = 11.sp,
                        color = colors.sub
                    )
                }
            }
        }

        // --- SMS Inbox History Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder()
                    .copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (hasSmsPermission) colors.accentSoft else colors.cardSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasSmsPermission) Icons.Outlined.Check else Icons.Outlined.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (hasSmsPermission) colors.accent else colors.sub
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SMS Inbox History",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hasSmsPermission) "Past SMS before installation loaded" else "Grant SMS access to analyze past inbox messages",
                                fontSize = 11.sp,
                                color = colors.sub
                            )
                        }

                        if (!hasSmsPermission) {
                            Button(
                                onClick = onRequestSmsPermission,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.ink,
                                    contentColor = colors.bg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(text = "Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.accentSoft)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "LOADED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Allows F-Tele to display and analyze promotional SMS received prior to app installation.",
                        fontSize = 11.sp,
                        color = colors.sub
                    )
                }
            }
        }

        // --- 3. Simple Protection Mode ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder()
                    .copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Protection Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Choose how strictly messages are filtered.",
                        fontSize = 11.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cardSoft)
                            .padding(2.dp)
                    ) {
                        listOf(
                            "blocklist" to "Smart Protection (Recommended)",
                            "allowlist" to "VIP Strict (All Muted)"
                        ).forEach { (mode, label) ->
                            val isSelected = currentMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) colors.ink else Color.Transparent)
                                    .clickable { onChangeMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.bg else colors.inkSoft
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (currentMode == "allowlist") {
                            "VIP Mode: Mutes ALL incoming messages unless they are in your allow list or are critical bank OTPs."
                        } else {
                            "Smart Mode: Only mutes known spam, promos, and lottery messages. Personal SMS and bank alerts always arrive normally."
                        },
                        fontSize = 11.sp,
                        color = colors.sub
                    )
                }
            }
        }

        // --- 4. Appearance (Light / Dark Theme) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder()
                    .copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "App Theme",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cardSoft)
                            .padding(2.dp)
                    ) {
                        listOf(
                            Triple("system", "Auto", Icons.Outlined.PhoneAndroid),
                            Triple("light", "Light", Icons.Outlined.LightMode),
                            Triple("dark", "Dark", Icons.Outlined.DarkMode)
                        ).forEach { (theme, label, icon) ->
                            val isSelected = themeMode == theme
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) colors.ink else Color.Transparent)
                                    .clickable { onChangeTheme(theme) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) colors.bg else colors.inkSoft
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.bg else colors.inkSoft
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Data & Privacy ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder()
                    .copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Activity History & Privacy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your messages never leave your phone. 100% offline with zero internet access.",
                        fontSize = 11.sp,
                        color = colors.sub
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onClearLogs,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cardSoft,
                            contentColor = colors.block
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Clear Activity History", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
