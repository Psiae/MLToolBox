package dev.psiae.mltoolbox.composeui.gestures

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.defaultSurfaceGestureModifiers() = pointerInput(Unit) {}

private fun Modifier.consumeDownGesture(
    requireUnconsumed: Boolean = true,
    eventPass: PointerEventPass = PointerEventPass.Main
): Modifier = pointerInput(requireUnconsumed, eventPass) {
    awaitEachGesture {
        awaitFirstDown(
            requireUnconsumed = requireUnconsumed,
            pass = eventPass
        ).apply(PointerInputChange::consume)
    }
}