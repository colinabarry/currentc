package com.barry.currentc.common.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlin.math.pow
import kotlin.math.roundToInt


fun <T : Number> T.remap(
    fromMin: T,
    fromMax: T,
    toMin: T,
    toMax: T
): T {
    val fromAbs = toDouble() - fromMin.toDouble()
    val fromMaxAbs = fromMax.toDouble() - fromMin.toDouble()

    val normal = fromAbs / fromMaxAbs

    val toMaxAbs = toMax.toDouble() - toMin.toDouble()
    val toAbs = toMaxAbs * normal

    val result = toAbs + toMin.toDouble()

    return when (this) {
        is Float -> result.toFloat()
        is Double -> result
        is Int -> result.toInt()
        is Long -> result.toLong()
        is Short -> result.toInt().toShort()
        is Byte -> result.toInt().toByte()
        else -> throw IllegalArgumentException("Unsupported numeric type")
    } as T
}

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun Float.roundToPlaces(places: Int): Float {
    val multiplier = 10.0.pow(places.toDouble())
    return (this * multiplier).roundToInt() / multiplier.toFloat()
}