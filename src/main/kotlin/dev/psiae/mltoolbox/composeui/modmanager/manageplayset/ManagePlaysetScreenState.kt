package dev.psiae.mltoolbox.composeui.modmanager.manageplayset

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.composeui.modmanager.ModManagerScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Composable
fun rememberManagePlaysetScreenState(
    modManagerScreenState: ModManagerScreenState
): ManagePlaysetScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(modManagerScreenState) {
        ManagePlaysetScreenState(modManagerScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class ManagePlaysetScreenState(
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