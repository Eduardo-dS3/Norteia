package io.github.eduardods3.norteia.ui

import android.location.LocationManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.eduardods3.norteia.ui.components.AdSlot
import io.github.eduardods3.norteia.ui.components.AdviceBanner
import io.github.eduardods3.norteia.ui.components.BubbleLevel
import io.github.eduardods3.norteia.ui.components.CardinalHaptics
import io.github.eduardods3.norteia.ui.components.CompassRose
import io.github.eduardods3.norteia.ui.components.CoordinatesCard
import io.github.eduardods3.norteia.ui.components.HeadingReadout
import io.github.eduardods3.norteia.ui.components.LicensesSheet
import io.github.eduardods3.norteia.ui.components.MetricsGrid
import io.github.eduardods3.norteia.ui.components.OptionsSheet
import io.github.eduardods3.norteia.ui.components.PermissionRationaleDialog
import io.github.eduardods3.norteia.ui.components.StatusStrip
import io.github.eduardods3.norteia.ui.components.rememberSmoothedAngle
import io.github.eduardods3.norteia.data.settings.NorthReference

@Composable
fun CompassScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permission = rememberLocationPermissionController { vm.onLocationPermissionResult(it) }

    LifecycleResumeEffect(Unit) {
        val lm = context.getSystemService(LocationManager::class.java)
        vm.onLocationServicesState(lm != null && LocationManagerCompat.isLocationEnabled(lm))
        onPauseOrDispose { }
    }

    var showOptions by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }

    val heading by rememberSmoothedAngle(state.headingDeg)

    CardinalHaptics(state.headingDeg, state.hapticOnCardinals && state.headingAvailable)

    Column(modifier.fillMaxSize()) {
        // --- top bar ------------------------------------------------------
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "NORTEIA",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { showOptions = true }) {
                Icon(Icons.Outlined.Tune, contentDescription = "Opções")
            }
        }

        // --- scrolling content ------------------------------------------
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AdviceBanner(state, Modifier.padding(top = 4.dp))

            Box(contentAlignment = Alignment.Center) {
                CompassRose(
                    headingDeg = heading,
                    sunAzimuthDeg = state.sun?.takeIf { state.showSunMoon }?.position?.azimuthDeg,
                    sunUp = state.sun?.position?.isUp == true,
                    moonAzimuthDeg = state.moon?.takeIf { state.showSunMoon }?.position?.azimuthDeg,
                    moonUp = state.moon?.position?.isUp == true,
                    moonIllumination = state.moon?.illuminatedFraction ?: 0.0,
                    courseDeg = state.courseDeg,
                )
                HeadingReadout(
                    headingDeg = heading,
                    headingAvailable = state.headingAvailable,
                    usingTrueNorth = state.northReference == NorthReference.TRUE,
                    trueNorthAvailable = state.declinationDeg != null,
                    onToggleNorthReference = vm::toggleNorthReference,
                )
            }

            StatusStrip(
                fixQuality = state.fixQuality,
                horizontalAccuracyM = state.horizontalAccuracyM,
                gnss = state.gnss,
                isMock = state.isMockLocation,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            CoordinatesCard(
                state = state,
                onCycleFormat = vm::cycleCoordinateFormat,
                onCopy = {
                    state.position?.let {
                        LocationActions.copy(context, "Coordenadas", state.formattedCoordinates)
                    }
                },
                onShare = {
                    state.position?.let { LocationActions.share(context, it, state.formattedCoordinates) }
                },
                onOpenInMaps = {
                    state.position?.let { LocationActions.openInMaps(context, it) }
                },
                onRequestPermission = permission.request,
            )

            MetricsGrid(state)

            if (state.showLevel) {
                BubbleLevel(state.level)
            }

            Spacer(Modifier.height(4.dp))
        }

        // --- reserved ad space, always at the very bottom -----------------
        AdSlot(Modifier.navigationBarsPadding())
    }

    if (permission.rationaleVisible) {
        PermissionRationaleDialog(
            permanentlyDenied = permission.permanentlyDenied,
            onConfirm = permission.confirmRationale,
            onDismiss = permission.dismissRationale,
        )
    }

    if (showOptions) {
        OptionsSheet(
            state = state,
            onDismiss = { showOptions = false },
            onToggleLevel = vm::setShowLevel,
            onToggleSunMoon = vm::setShowSunMoon,
            onToggleHaptics = vm::setHaptics,
            onOpenLicenses = {
                showOptions = false
                showLicenses = true
            },
        )
    }
    if (showLicenses) {
        LicensesSheet(onDismiss = { showLicenses = false })
    }
}
