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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VerifiedUser
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
import com.qook.ftele.engine.PatternPacks
import com.qook.ftele.model.BehaviorCategory
import com.qook.ftele.model.CategoryType
import com.qook.ftele.ui.theme.FTeleTheme

@Composable
fun ShieldsScreen(
    categoryStates: Map<String, Boolean>,
    onToggleCategory: (String, Boolean) -> Unit
) {
    val colors = FTeleTheme.colors

    val protectionCategories = PatternPacks.CATEGORIES.filter { it.type != CategoryType.USER_PREFERENCE }
    val preferenceCategories = PatternPacks.CATEGORIES.filter { it.type == CategoryType.USER_PREFERENCE }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // --- 2. Core & Spam Protection Shields ---
        item {
            Text(
                text = "Spam & Advertising Shields",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.sub,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        items(protectionCategories, key = { it.id }) { category ->
            val isEnabled = categoryStates[category.id] ?: category.defaultEnabled
            ShieldCategoryCard(
                category = category,
                isEnabled = isEnabled,
                onToggle = { enabled -> onToggleCategory(category.id, enabled) }
            )
        }

        // --- 3. User Preference Filters (Optional / Off by Default) ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = "User Preferences (Utility Alerts)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.sub
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "These are not spam. Kept allowed by default so you still receive receipts & balance queries. Toggle ON only if you prefer total silence.",
                    fontSize = 11.sp,
                    color = colors.sub
                )
            }
        }

        items(preferenceCategories, key = { it.id }) { category ->
            val isEnabled = categoryStates[category.id] ?: category.defaultEnabled
            ShieldCategoryCard(
                category = category,
                isEnabled = isEnabled,
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
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!category.isProtection) {
                    Modifier.clickable { onToggle(!isEnabled) }
                } else Modifier
            ),
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
                        .background(colors.cardSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colors.ink
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & rule count
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = category.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${category.rules.size} rules" + if (category.type == CategoryType.USER_PREFERENCE) " · Preference" else "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.sub
                    )
                }

                // Switch or Badge
                if (!category.isProtection) {
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
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = colors.sub
            )
        }
    }
}

private fun getCategoryIcon(id: String): ImageVector {
    return when (id) {
        "cat_protection" -> Icons.Outlined.VerifiedUser
        "cat_lottery" -> Icons.Outlined.ConfirmationNumber
        "cat_telecom_promos" -> Icons.Outlined.Campaign
        "cat_vas" -> Icons.Outlined.NotificationsActive
        "cat_loans" -> Icons.Outlined.CreditCard
        "cat_pure_spam_senders" -> Icons.Outlined.NotificationsOff
        "cat_pref_recharge" -> Icons.AutoMirrored.Outlined.ReceiptLong
        "cat_pref_balance" -> Icons.Outlined.QueryStats
        "cat_pref_transfers" -> Icons.Outlined.SwapHoriz
        else -> Icons.Outlined.Shield
    }
}
