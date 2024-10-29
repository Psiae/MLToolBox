package dev.psiae.mltoolbox.composeui.modmanager.managemods.direct

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.composeui.HeightSpacer
import dev.psiae.mltoolbox.composeui.WidthSpacer
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.composeui.modmanager.WIPScreen
import dev.psiae.mltoolbox.composeui.modmanager.managemods.ManageModsScreenState
import dev.psiae.mltoolbox.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Spec
import dev.psiae.mltoolbox.uifoundation.themes.md3.incrementsDp
import dev.psiae.mltoolbox.uifoundation.themes.md3.padding

@Composable
fun ManageDirectModsContent(
    manageModsScreenState: ManageModsScreenState
) {
    val state = rememberManageDirectModsScreenState(manageModsScreenState)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surfaceDim)
            .defaultSurfaceGestureModifiers()
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            HeightSpacer(16.dp)
            Box {
                SelectedWorkingBinaryFilePanel(state)
            }
            HeightSpacer(4.dp)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                PrimarySection(state)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .border(
                        width = 1.dp,
                        color = Material3Theme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(Material3Theme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                    .defaultSurfaceGestureModifiers(),
            ) {
                var width by remember {
                    mutableStateOf(0.dp)
                }
                val density = LocalDensity.current
                Column(
                    // TODO: measure content ourselves then render the divider based on it
                    modifier = Modifier
                        .onGloballyPositioned { coord ->
                            with(density) { coord.size.width.toDp() }.let {
                                width = it
                            }
                        }
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Material3Theme.colorScheme.surfaceContainer)
                                .defaultSurfaceGestureModifiers()
                        ) {
                            DirectModList(state)
                        }
                    }
                }
            }
        }
        if (state.installModDestination) {
            DirectInstallModScreen(state)
        }
        if (state.uninstallModDestination) {
            WIPScreen()
        }
    }
}


@Composable
private fun SelectedWorkingBinaryFilePanel(
    manageDirectModsScreenState: ManageDirectModsScreenState
) {
    BoxWithConstraints(modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .defaultMinSize(minWidth = 1200.dp)
        .defaultMinSize(minHeight = 36.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(4.dp))
        .then(
            if (LocalIsDarkTheme.current)
                Modifier.shadow(elevation = 2.dp, RoundedCornerShape(4.dp))
            else
                Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
        )
        .clickable { manageDirectModsScreenState.manageModsScreenState.modManagerScreenState.userInputChangeWorkingDir() }
        .background(Material3Theme.colorScheme.inverseOnSurface)
        .padding(MD3Spec.padding.incrementsDp(2).dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 2.dp)
                .defaultMinSize(minWidth = with(LocalDensity.current) { constraints.minWidth.toDp() }),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            run {
                val f = manageDirectModsScreenState.manageModsScreenState.modManagerScreenState.gameBinaryFile
                val fp = manageDirectModsScreenState.manageModsScreenState.modManagerScreenState.gameBinaryFile?.absolutePath
                val (fp1, fp2) = remember(f) {
                    run {
                        var dash = false
                        fp?.dropLastWhile { c -> !dash.also { dash = c == '\\' } }
                    } to run {
                        var dash = false
                        fp?.takeLastWhile { c -> !dash.also { dash = c == '\\' } }
                    }
                }
                val color = Material3Theme.colorScheme.onSurface.copy(alpha = 0.78f)
                Text(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .align(Alignment.CenterVertically),
                    text = fp1?.plus(fp2) ?: "Click here to select game executable",
                    style = Material3Theme.typography.labelMedium,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)
            Icon(
                modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
                painter = painterResource("drawable/icon_folder_96px.png"),
                contentDescription = null,
                tint = Material3Theme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun PrimarySection(
    manageDirectModsScreenState: ManageDirectModsScreenState
) {
    Box(
        modifier = Modifier
            .defaultSurfaceGestureModifiers()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Material3Theme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Material3Theme.colorScheme.surfaceContainer)
                .defaultSurfaceGestureModifiers()
                .padding(12.dp),
        ) {
            Row(

            ) {
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { manageDirectModsScreenState.userInputNavigateToInstallModScreen() }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f, false),
                        text = "Install mod",
                        style = Material3Theme.typography.labelLarge.copy(
                            baselineShift = BaselineShift(-0.1f)
                        ),
                        color = Material3Theme.colorScheme.primary,
                        maxLines = 1
                    )
                }
                WidthSpacer(6.dp)
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = false) { }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f, false),
                        text = "Uninstall mod (WIP)",
                        style = Material3Theme.typography.labelLarge.copy(
                            baselineShift = BaselineShift(-0.1f)
                        ),
                        color = Material3Theme.colorScheme.primary.copy(alpha = 0.38f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}