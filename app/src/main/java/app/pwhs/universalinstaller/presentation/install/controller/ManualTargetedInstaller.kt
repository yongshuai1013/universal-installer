package app.pwhs.universalinstaller.presentation.install.controller

import android.content.Context
import android.content.pm.PackageInstaller
import android.net.Uri
import app.pwhs.universalinstaller.util.HiddenApiHacks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * A manual installer that targets a specific user ID using hidden APIs.
 * Used when Ackpine doesn't support the target user.
 */
object ManualTargetedInstaller {

    suspend fun install(
        context: Context,
        uris: List<Uri>,
        userId: Int,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val targetedInstaller = HiddenApiHacks.createPackageInstallerForUser(context, userId)
                ?: throw RuntimeException("Failed to get targeted PackageInstaller")

            val params = PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
            )
            
            // Allow downgrade, test packages etc. could be added here if needed
            
            val sessionId = targetedInstaller.createSession(params)
            targetedInstaller.openSession(sessionId).use { session ->
                uris.forEachIndexed { index, uri ->
                    val name = "apk_$index.apk"
                    val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                        ?: throw RuntimeException("Failed to open Uri: $uri")
                    pfd.use {
                        session.openWrite(name, 0, it.statSize).use { out ->
                            java.io.FileInputStream(it.fileDescriptor).use { input ->
                                input.copyTo(out)
                            }
                        }
                    }
                    onProgress((index + 1).toFloat() / uris.size)
                }
                
                // Track result via broadcast
                val intent = android.content.Intent("app.pwhs.universalinstaller.INSTALL_STATUS")
                val receiver = android.app.PendingIntent.getBroadcast(
                    context, sessionId, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
                )
                session.commit(receiver.intentSender)
            }
        }
    }
}
