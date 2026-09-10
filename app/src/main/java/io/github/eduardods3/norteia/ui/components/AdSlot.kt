package io.github.eduardods3.norteia.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Reserved space for a future banner ad.
 *
 * WHY THIS EXISTS IN V1 (which ships with NO ads):
 *  - The slot is a fixed 56 dp band pinned to the very bottom of the screen,
 *    OUTSIDE the compass touch target and below every interactive control, so
 *    turning ads on later cannot cause an accidental tap or shift the layout.
 *  - Until then it shows a muted wordmark + version, so the space looks
 *    intentional rather than empty.
 *
 * HOW TO TURN ON A BANNER LATER (full steps in docs/MONETIZATION.md):
 *  1. Add the SDK:  implementation("com.google.android.gms:play-services-ads:<v>")
 *     and the UMP consent SDK.
 *  2. Add INTERNET + ACCESS_NETWORK_STATE and the AdMob APPLICATION_ID
 *     <meta-data> to AndroidManifest.xml.
 *  3. In NorteiaApp.onCreate(): request UMP consent, then MobileAds.initialize().
 *  4. Replace the body of this composable with an AndroidView { AdView(...) }
 *     using an ADAPTIVE banner sized to this width. Keep [AD_SLOT_HEIGHT] as
 *     the min height so nothing jumps while the ad loads.
 *  5. Never use interstitials/rewarded here — this is a glance-and-go utility;
 *     an unexpected full-screen ad is both bad UX and a policy risk.
 */
val AD_SLOT_HEIGHT = 56.dp

@Composable
fun AdSlot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AD_SLOT_HEIGHT)
            .heightIn(min = AD_SLOT_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "NORTEIA",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            textAlign = TextAlign.Center,
        )
        // When ads are live, delete the Text above and place the AdView here.
    }
}
