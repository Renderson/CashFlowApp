package com.renderson.cashflowapp.util.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

fun Modifier.skeletonLoading(isLoading: Boolean? = false): Modifier = composed {
    if (isLoading == true) {
        val colors = listOf(
            Color(0xFFD5D5D5),
            Color(0xFFF5F6F8),
            Color(0xFFFFFFFF),
            Color(0xFFF5F6F8),
            Color(0xFFD5D5D5),
        )

        var size by remember {
            mutableStateOf(IntSize.Zero)
        }
        val transition = rememberInfiniteTransition(label = "")
        val startOffsetX by transition.animateFloat(
            initialValue = -2 * size.width.toFloat(),
            targetValue = 2 * size.width.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(1800)
            ),
            label = ""
        )
        
        this
            .background(
                brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(startOffsetX, 0f),
                    end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
                )
            )
            .onGloballyPositioned {
                size = it.size
            }
    } else {
        this
    }
}

@Preview
@Composable
fun PreviewDS3SkeletonLoadingCircle() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .skeletonLoading(true)
    )
}

@Preview
@Composable
fun PreviewDS3SkeletonLoading() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .skeletonLoading(true)
    )
}