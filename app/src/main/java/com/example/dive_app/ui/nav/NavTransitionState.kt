package com.example.dive_app.ui.nav

import androidx.compose.ui.graphics.TransformOrigin

object NavTransitionState {
    // 네비게이션 직전에 Home에서 세팅 (기본: 화면 중앙)
    @JvmStatic
    var origin: TransformOrigin = TransformOrigin.Center
}
