package app.pwhs.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import app.pwhs.tv.presentation.manage.ManageScreen
import app.pwhs.tv.presentation.receive.ReceiveScreen
import app.pwhs.tv.presentation.settings.SettingsScreen

/**
 * Top-level TV shell: A completely static side Navigation Rail.
 * Extremely lightweight and performant for low-end TV hardware.
 */
@Composable
fun TvApp(modifier: Modifier = Modifier) {
    // Survives the locale/config-change recreate so the user stays on their current tab.
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.fillMaxSize()) {
            // Static Side Navigation Rail (Icon only)
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    // Overscan-safe: the rail background bleeds to the edge
                    .padding(start = 32.dp, end = 12.dp, top = 28.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // App Logo
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
                }

                Spacer(Modifier.height(32.dp))

                NavigationItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    iconRes = R.drawable.ic_apk_install
                )
                Spacer(Modifier.height(12.dp))
                NavigationItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    iconRes = R.drawable.ic_delete
                )
                Spacer(Modifier.height(12.dp))
                NavigationItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    iconRes = R.drawable.ic_setting
                )
            }

            // Content Area — all three destinations stay composed once first visited (keep-alive),
            // so switching tabs is an alpha/focus toggle instead of a full teardown + rebuild +
            // data reload. That rebuild-and-reload on every switch is what made navigation feel
            // janky on low-end TV hardware; here the LaunchedEffect keys never reset, so the app
            // list / QR / storage stats are computed once and simply shown again.
            Box(Modifier.weight(1f).fillMaxHeight()) {
                ScreenSlot(active = tab == 0) {
                    ReceiveScreen(active = tab == 0, modifier = Modifier.fillMaxSize())
                }
                ScreenSlot(active = tab == 1) {
                    ManageScreen(active = tab == 1, modifier = Modifier.fillMaxSize())
                }
                ScreenSlot(active = tab == 2) {
                    SettingsScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/**
 * Keeps a destination's composition — and therefore its loaded data, scroll position and focus
 * state — alive across tab switches. The slot composes its [content] the first time it becomes
 * [active] (lazy, so unvisited tabs cost nothing at startup) and never disposes it afterwards.
 *
 * When inactive it is drawn fully transparent and made focus-inert: [canFocus] is false and any
 * D-pad attempt to enter the group is cancelled, so focus can't wander into an off-screen
 * destination. (Explicit `requestFocus()` calls still pierce this, which is why the screens gate
 * their own focus requests on an `active` flag.)
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun ScreenSlot(active: Boolean, content: @Composable () -> Unit) {
    var everActive by remember { mutableStateOf(false) }
    if (active) everActive = true
    if (!everActive) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (active) 1f else 0f)
            .graphicsLayer { alpha = if (active) 1f else 0f }
            .focusProperties {
                canFocus = active
                if (!active) onEnter = { cancelFocus() }
            }
            .focusGroup(),
    ) {
        content()
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
            focusedContainerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // Fixed height to make it a square-like button
            .clip(shape)
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
