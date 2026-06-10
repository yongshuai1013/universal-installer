package app.pwhs.tv

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NonInteractiveSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.pwhs.tv.presentation.manage.ManageScreen
import app.pwhs.tv.presentation.receive.ReceiveScreen
import app.pwhs.tv.presentation.settings.SettingsScreen

/**
 * Top-level TV shell: a side Navigation Rail for Install | Manage | Settings.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvApp(modifier: Modifier = Modifier) {
    var tab by remember { mutableIntStateOf(0) }

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RectangleShape,
        colors = NonInteractiveSurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxSize()) {
            // Side Navigation Rail
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(240.dp),
                shape = RectangleShape,
                colors = NonInteractiveSurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 32.dp, start = 12.dp)
                    )

                    NavigationItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        iconRes = R.drawable.ic_apk_install,
                        label = stringResource(R.string.tv_app_tab_install)
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        iconRes = R.drawable.ic_delete,
                        label = stringResource(R.string.tv_app_tab_manage)
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        iconRes = R.drawable.ic_setting,
                        label = stringResource(R.string.tv_app_tab_settings)
                    )
                }
            }

            // Content Area
            Box(Modifier.weight(1f).fillMaxHeight()) {
                when (tab) {
                    0 -> ReceiveScreen(modifier = Modifier.fillMaxSize())
                    1 -> ManageScreen(modifier = Modifier.fillMaxSize())
                    else -> SettingsScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    label: String
) {
    Surface(
        onClick = onClick,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            focusedContainerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
