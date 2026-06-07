package app.pwhs.tv

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import app.pwhs.core.data.AppRepository
import app.pwhs.core.domain.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 10-foot manage screen: a D-pad-navigable list of installed user apps. Selecting a row
 * launches the system uninstall dialog (no privileged permission needed); on return we
 * bump [reloadTick] to refresh the list.
 *
 * tv-material3 [Card] handles focus + center-key click natively, so there's no manual
 * focus/clickable wiring.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ManageScreen(repo: AppRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var reloadTick by remember { mutableIntStateOf(0) }

    val uninstallLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { reloadTick++ }

    LaunchedEffect(reloadTick) {
        loading = true
        apps = withContext(Dispatchers.IO) { repo.getInstalledApps(includeSystem = false) }
        loading = false
    }

    Surface(modifier = modifier.fillMaxSize(), shape = RectangleShape) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Overscan-safe margins for the 10-foot UI.
                .padding(horizontal = 48.dp, vertical = 32.dp),
        ) {
            Text(
                text = "Installed apps",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (loading) "Loading…" else "${apps.size} apps · select to uninstall",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppRow(app) {
                        uninstallLauncher.launch(
                            Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AppRow(app: InstalledApp, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(text = app.appName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
