package app.pwhs.core.data

import app.pwhs.core.util.RootShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Root-shell app-management operations for the TV Manage screen — the counterpart to
 * [app.pwhs.core.install.RootInstaller] for the "manage" side. Each runs a single privileged
 * `pm`/`am` command via [RootShell] and verifies it actually took effect: `pm` prints soft
 * failures ("Failure [...]") with exit 0, so a success *token* is checked alongside the exit code.
 *
 * Package names are regex-validated before being interpolated into the shell command to block
 * injection — callers feed PackageManager-sourced strings that can't contain metacharacters,
 * but we verify anyway.
 */
object PrivilegedAppOps {

    private val SAFE_PACKAGE = Regex("^[A-Za-z0-9._]+$")

    /** Terminates the app's processes. `am force-stop` prints nothing on success. */
    suspend fun forceStop(packageName: String): Result<Unit> =
        run(packageName, "am force-stop $packageName", successToken = null)

    /** Enables or disables (for user 0) the package. */
    suspend fun setEnabled(packageName: String, enabled: Boolean): Result<Unit> {
        val cmd = if (enabled) "pm enable $packageName" else "pm disable-user --user 0 $packageName"
        val token = if (enabled) "new state: enabled" else "new state: disabled"
        return run(packageName, cmd, successToken = token)
    }

    /** `pm clear` wipes cache + data + obb for the package. */
    suspend fun clearData(packageName: String): Result<Unit> =
        run(packageName, "pm clear $packageName", successToken = "Success")

    /**
     * Silent uninstall. [system] apps can't be fully removed, so they're uninstalled for the
     * current user (`--user 0`) — the standard "remove a bundled app" approach.
     */
    suspend fun uninstall(packageName: String, system: Boolean): Result<Unit> {
        val cmd = if (system) "pm uninstall --user 0 $packageName" else "pm uninstall $packageName"
        return run(packageName, cmd, successToken = "Success")
    }

    private suspend fun run(
        packageName: String,
        cmd: String,
        successToken: String?,
        timeoutSeconds: Long = 20,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!SAFE_PACKAGE.matches(packageName)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid package name: $packageName"))
        }
        runCatching {
            val r = RootShell.exec(cmd, timeoutSeconds)
            val tokenOk = successToken == null || r.output.contains(successToken, ignoreCase = true)
            if (!r.isSuccess || !tokenOk) {
                throw RuntimeException(r.output.trim().ifBlank { "Command failed (exit ${r.exitCode})" })
            }
        }
    }
}
