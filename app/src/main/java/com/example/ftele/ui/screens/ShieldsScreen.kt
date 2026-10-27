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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Explanatory Banner
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Behavioral Shields",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hand-crafted offline rules for telecom spam, scams & banking protection.",
                                fontSize = 13.sp,
                                color = colors.sub
                            )
                        }

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
                            Text(text = "Simulate", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // List of Shields
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (category.isProtection) colors.accent.copy(alpha = 0.5f) else colors.line
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
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (category.isProtection) colors.accentSoft else colors.cardSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (category.isProtection) colors.accent else colors.ink
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & rule count
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${category.rules.size} behavioral rules",
                        fontSize = 12.sp,
                        color = colors.sub
                    )
                }

                // Switch or Always ON Badge
                if (category.isProtection) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.accentSoft)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "ALWAYS ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
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

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = category.description,
                fontSize = 13.sp,
                color = colors.inkSoft,
                lineHeight = 18.sp
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
