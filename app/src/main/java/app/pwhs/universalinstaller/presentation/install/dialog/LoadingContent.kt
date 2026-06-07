package app.pwhs.universalinstaller.presentation.install.dialog

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Skeleton placeholder rendered while we parse the APK. Mirrors the Prepare-stage
 * layout (icon + title + subtitle + chip row + button) so the transition to real
 * content doesn't shift focus — content fades in at roughly the position the
 * placeholders occupied.
 */
@Composable
fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Icon placeholder
        ShimmerBox(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        // Title line
        ShimmerBox(modifier = Modifier.width(180.dp).height(20.dp), shape = RoundedCornerShape(6.dp))
        Spacer(modifier = Modifier.height(8.dp))
        // Subtitle line
        ShimmerBox(modifier = Modifier.width(140.dp).height(14.dp), shape = RoundedCornerShape(6.dp))
        Spacer(modifier = Modifier.height(20.dp))
        // Chip row (2 chips)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(modifier = Modifier.width(80.dp).height(28.dp), shape = RoundedCornerShape(14.dp))
            ShimmerBox(modifier = Modifier.width(64.dp).height(28.dp), shape = RoundedCornerShape(14.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Button row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(20.dp))
            ShimmerBox(modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(20.dp))
        }
    }
}

@Composable
private fun ShimmerBox(modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer-alpha",
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.2f)),
    )
}

// Unused helper retained for callers that may still depend on a fixed-height
// skeleton card from earlier iterations. Safe to delete in a future pass.
@Suppress("unused")
private fun shimmerDp(): Dp = 16.dp
