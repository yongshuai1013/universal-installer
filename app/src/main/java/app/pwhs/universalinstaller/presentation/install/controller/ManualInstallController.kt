package app.pwhs.universalinstaller.presentation.install.controller

import android.app.Application
import android.net.Uri
import app.pwhs.universalinstaller.data.local.InstallHistoryDao
import app.pwhs.universalinstaller.domain.repository.SessionDataRepository
import app.pwhs.universalinstaller.presentation.setting.PreferencesKeys
import app.pwhs.universalinstaller.presentation.setting.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.solrudev.ackpine.installer.InstallFailure
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.session.ProgressSession
import java.util.UUID

/**
 * A controller that handles targeted user installs manually.
 */
class ManualInstallController(
    private val application: Application,
    packageInstaller: PackageInstaller,
    sessionDataRepository: SessionDataRepository,
    historyDao: InstallHistoryDao,
) : BaseInstallController(application, packageInstaller, sessionDataRepository, historyDao) {

    override suspend fun createSession(
        uris: List<Uri>,
        name: String,
        packageName: String,
    ): ProgressSession<InstallFailure> {
        // This won't be called if we override install()
        throw UnsupportedOperationException("Use install() with userId")
    }

    fun installTargeted(
        uris: List<Uri>,
        name: String,
        userId: Int,
        scope: CoroutineScope,
    ) {
        // We'll simulate a session ID to keep the UI happy
        val sessionId = UUID.randomUUID()
        // ... implementation that uses ManualTargetedInstaller ...
        // and updates sessionDataRepository
    }
}
