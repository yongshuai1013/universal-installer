package app.pwhs.tv.presentation.receive

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import app.pwhs.core.domain.ApkFile
import app.pwhs.core.receiver.ReceivedApk
import app.pwhs.core.receiver.ReceiverStatus
import app.pwhs.tv.R
import app.pwhs.tv.formatSize
import app.pwhs.tv.ui.components.QrCode
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import java.io.File

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ReceiveScreen(
    modifier: Modifier = Modifier,
    viewModel: ReceiveViewModel = viewModel()
) {
    val context = LocalContext.current
    val status by viewModel.status.collectAsState()
    val pending by viewModel.pendingApk.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val installResult by viewModel.installResult.collectAsState()
    val installingLabel by viewModel.installingLabel.collectAsState()

    val installFocus = remember { FocusRequester() }

    val readPerm = if (Build.VERSION.SDK_INT <= 32) Manifest.permission.READ_EXTERNAL_STORAGE else null
    fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            readPerm == null || ContextCompat.checkSelfPermission(context, readPerm) == PackageManager.PERMISSION_GRANTED
        }
    }

    var hasStorage by remember { mutableStateOf(checkStoragePermission()) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasStorage = checkStoragePermission() }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { hasStorage = checkStoragePermission() }

    LaunchedEffect(pending) { if (pending != null) runCatching { installFocus.requestFocus() } }
    LaunchedEffect(hasStorage) {
        if (hasStorage) viewModel.scanLocalApks()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 48.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Text(stringResource(R.string.tv_receive_from_phone), style = MaterialTheme.typography.headlineMedium) }

        item {
            when (val s = status) {
                is ReceiverStatus.Running -> Row(verticalAlignment = Alignment.Top) {
                    QrCode(data = s.url, modifier = Modifier.size(220.dp))
                    Spacer(Modifier.width(36.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.tv_receive_step1), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.tv_receive_step2), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.tv_receive_step3), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        Text("http://${s.ip}:${s.port}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
                ReceiverStatus.Stopped -> Text(stringResource(R.string.tv_receive_starting), style = MaterialTheme.typography.bodyMedium)
            }
        }

        installResult?.let { msg ->
            item { Text(msg, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary) }
        }

        pending?.let { p ->
            item {
                ReceivedApkCard(
                    apk = p,
                    isInstalling = installingLabel != null,
                    installFocus = installFocus,
                    onInstall = {
                        if (!context.packageManager.canRequestPackageInstalls()) {
                            openUnknownSources(context)
                        } else {
                            viewModel.install(Uri.fromFile(File(p.path)), p.fileName.isBundleName(), p.metadata?.appName ?: p.fileName)
                        }
                    },
                    onDismiss = { viewModel.dismissPending() }
                )
            }
        }

        // ── On this TV (Storage) ──────────────
        item {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.tv_receive_on_tv), style = MaterialTheme.typography.titleLarge)
        }
        if (!hasStorage) {
            item {
                Card(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}"))
                            settingsLauncher.launch(intent)
                        } else {
                            readPerm?.let { permLauncher.launch(it) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}"))
                            settingsLauncher.launch(intent)
                        } else {
                            readPerm?.let { permLauncher.launch(it) }
                        }
                    }
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.tv_receive_allow_storage_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.tv_receive_allow_storage_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else if (downloads.isEmpty() && !isScanning) {
            item { Text(stringResource(R.string.tv_receive_no_apks), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(downloads, key = { it.uri }) { apk ->
                ApkFileCard(apk) {
                    if (!context.packageManager.canRequestPackageInstalls()) {
                        openUnknownSources(context)
                    } else {
                        viewModel.install(Uri.parse(apk.uri), apk.isBundle, apk.metadata?.appName ?: apk.displayName)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ReceivedApkCard(
    apk: ReceivedApk,
    isInstalling: Boolean,
    installFocus: FocusRequester,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val meta = apk.metadata
    Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = meta?.icon
            if (icon != null) {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("APK", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(meta?.appName ?: apk.fileName, style = MaterialTheme.typography.titleLarge)
                if (meta != null) {
                    Text("${meta.packageName} · v${meta.versionName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(formatSize(context, apk.sizeBytes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier
                            .focusRequester(installFocus)
                            .clickable { onInstall() }
                    ) {
                        Text(if (isInstalling) stringResource(R.string.tv_receive_installing_plain) else stringResource(R.string.tv_receive_install))
                    }
                    Button(onClick = onDismiss, modifier = Modifier.clickable { onDismiss() }) {
                        Text(stringResource(R.string.tv_receive_dismiss))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ApkFileCard(apk: ApkFile, onClick: () -> Unit) {
    val context = LocalContext.current
    val meta = apk.metadata
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = meta?.icon
            if (icon != null) {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("APK", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(meta?.appName ?: apk.displayName, style = MaterialTheme.typography.titleMedium)
                if (meta != null) {
                    Text("v${meta.versionName} · ${formatSize(context, apk.sizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(formatSize(context, apk.sizeBytes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun String.isBundleName(): Boolean =
    substringAfterLast('.', "").lowercase() in setOf("apks", "xapk", "apkm", "apk+")

private fun openUnknownSources(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
