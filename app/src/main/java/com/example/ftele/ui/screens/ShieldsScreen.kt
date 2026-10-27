package com.example.ftele.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ftele.engine.PatternPacks
import com.example.ftele.model.BehaviorCategory
import com.example.ftele.ui.theme.FTeleTheme

@Composable
fun ShieldsScreen(
    isCategoryEnabled: (String) -> Boolean,
    onToggleCategory: (String, Boolean) -> Unit,
    onSimulateSms: () -> Unit
) {
    val colors = FTeleTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. Explanatory Header ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.line))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Behavioral Shields",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pre-configured offline rules for telecom promos, scams, and banking protection.",
                            fontSize = 12.sp,
                            color = colors.sub
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onSimulateSms,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cardSoft,
                            contentColor = colors.ink
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
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
                }
            }
        }

        // --- 2. List of Shields ---
        items(PatternPacks.CATEGORIES, key = { it.id }) { category ->
            ShieldCategoryCard(
                category = category,
                isEnabled = isCategoryEnabled(category.id),
                onToggle = { enabled -> onToggleCategory(category.id, enabled) }
            )
        }
    }
}

@Composable
private fun ShieldCategoryCard(
    category: BehaviorCategory,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val colors = FTeleTheme.colors
    val icon = getCategoryIcon(category.id)

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (category.isProtection) colors.accentSoft else colors.cardSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (category.isProtection) colors.accent else colors.ink
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & rule count
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${category.rules.size} rules",
                        fontSize = 11.sp,
                        color = colors.sub
                    )
                }

                // Switch or Always ON Badge
                if (category.isProtection) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.accentSoft)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CORE SHIELD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.sub,
                            uncheckedTrackColor = colors.cardSoft
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = category.description,
                fontSize = 12.sp,
                color = colors.inkSoft,
                lineHeight = 16.sp
            )
        }
    }
}

private fun getCategoryIcon(categoryId: String): ImageVector {
    return when (categoryId) {
        "cat_protection" -> Icons.Outlined.VerifiedUser
        "cat_lottery" -> Icons.Outlined.ConfirmationNumber
        "cat_telecom_promos" -> Icons.Outlined.Campaign
        "cat_vas" -> Icons.Outlined.NotificationsActive
        "cat_loans" -> Icons.Outlined.CreditCard
        "cat_pure_spam_senders" -> Icons.Outlined.NotificationsOff
        else -> Icons.Outlined.Shield
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Shields Screen Dark")
@Composable
fun ShieldsScreenPreview() {
    FTeleTheme(themeSetting = "dark") {
        ShieldsScreen(
            isCategoryEnabled = { it != "cat_loans" },
            onToggleCategory = { _, _ -> },
            onSimulateSms = {}
        )
    }
}
