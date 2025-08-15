package com.example.myapplication.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 재사용 가능한 원형 아이콘 버튼.
 * - Surface: 둥근 배경/음영
 * - Icon: 벡터 아이콘
 * - onClick: 클릭 콜백
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Int = 56,
    iconSizeDp: Int = 28
) {
    Surface(
        modifier = modifier
            .size(sizeDp.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = background,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSizeDp.dp)
            )
        }
    }
}
