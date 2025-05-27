package com.sample.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sample.android.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.pretendard_regular, weight = FontWeight.Normal),
            Font(R.font.pretendard_medium, weight = FontWeight.Medium),
            Font(R.font.pretendard_bold, weight = FontWeight.Bold)
        ),
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = Color.Black
    )
)