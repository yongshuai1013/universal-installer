package app.pwhs.tv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.pwhs.core.domain.ThemeMode

/**
 * The shared onboarding screen ([app.pwhs.core.presentation.onboarding.OnboardingScreen]) is built
 * with mobile `androidx.compose.material3` and reads *that* [MaterialTheme]. The TV app otherwise
 * only provides the tv-material3 theme, so onboarding would fall back to the baseline light-purple
 * Material scheme — the first screen every user sees, clashing with the orange/navy brand and
 * ignoring dark mode.
 *
 * This bridges the TV brand palette into a mobile [androidx.compose.material3.ColorScheme] and
 * honors the chosen light/dark mode. It lives in `:tv` so `:core` (and therefore the mobile build)
 * is untouched.
 */
private val OnboardingLightColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outlineVariant = Color(0xFFCBD5E1),
    error = ErrorLight,
    onError = OnErrorLight,
)

private val OnboardingDarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outlineVariant = Color(0xFF334155),
    error = ErrorDark,
    onError = OnErrorDark,
)

@Composable
fun OnboardingThemeBridge(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    MaterialTheme(
        colorScheme = if (dark) OnboardingDarkColors else OnboardingLightColors,
        content = content,
    )
}
