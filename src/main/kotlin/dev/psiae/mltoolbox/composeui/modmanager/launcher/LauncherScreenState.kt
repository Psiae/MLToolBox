package dev.psiae.mltoolbox.composeui.modmanager.launcher

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.composeui.modmanager.ModManagerScreenState
import dev.psiae.mltoolbox.java.jFile
import kotlinx.coroutines.*
import java.io.Closeable

@Composable
fun rememberLauncherScreenState(
    modManagerScreenState: ModManagerScreenState
): LauncherScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(modManagerScreenState) {
        LauncherScreenState(modManagerScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class LauncherScreenState(
    val modManagerScreenState: ModManagerScreenState,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var selectedTab by mutableStateOf("direct")

    fun stateEnter() {
        _coroutineScope = CoroutineScope(uiContext.dispatchContext.mainDispatcher.immediate)
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    fun init() {

    }
}