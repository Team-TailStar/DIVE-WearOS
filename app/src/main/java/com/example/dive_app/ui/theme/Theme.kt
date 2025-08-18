package com.example.dive_app.common.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Shapes
import androidx.wear.compose.material.Typography

private val MyColorPalette = Colors(
    primary = Color.White,
    primaryVariant = Color.Gray,
    secondary = Color(0xFFFFC107),
    secondaryVariant = Color(0xFFFFA000),
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val MyTypography = Typography() // Wear OS Typography 기본값 사용
private val MyShapes = Shapes() // Wear OS Shapes 기본값 사용

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = MyColorPalette,
        typography = MyTypography,
        shapes = MyShapes,
        content = content
    )
}
