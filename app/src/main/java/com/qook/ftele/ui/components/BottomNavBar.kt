package com.qook.ftele.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qook.ftele.ui.theme.FTeleTheme

sealed class ScreenTab(
    val index: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    data object Activity : ScreenTab(0, "Activity", "Live SMS notifications", Icons.Outlined.Inbox)
    data object Shields : ScreenTab(1, "Shields", "Behavioral protection packs", Icons.Outlined.Security)
    data object Rules : ScreenTab(2, "Rules", "Custom keyword and pattern filters", Icons.Outlined.Tune)
    data object Settings : ScreenTab(3, "Settings", "Strategy & notification access", Icons.Outlined.Settings)

    companion object {
        val ALL = listOf(Activity, Shields, Rules, Settings)
    }
}

@Composable
fun FTeleBottomNavBar(
    currentTab: Int,
    needsAttention: Boolean,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FTeleTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.card)
            .navigationBarsPadding()
    ) {
        // Top hairline separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.line)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenTab.ALL.forEach { tab ->
                val isSelected = currentTab == tab.index

                val pillBg by animateColorAsState(
                    targetValue = if (isSelected) colors.accentSoft else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "pillBg"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) colors.accent else colors.sub,
                    animationSpec = tween(durationMillis = 200),
                    label = "contentColor"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(pillBg)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onTabSelected(tab.index) }
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp),
                                tint = contentColor
                            )

                            // Attention indicator badge for Settings
                            if (tab == ScreenTab.Settings && needsAttention) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.block)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
