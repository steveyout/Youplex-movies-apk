package com.example.cinestream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdBlockIndicator(
    blockedCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF10B981).copy(alpha = 0.2f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.testTag("ad_block_indicator")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Ad Blocker Shield",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "ADS BLOCKED: $blockedCount",
                color = Color(0xFF10B981),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
