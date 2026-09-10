package io.github.eduardods3.norteia.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Everything the compass screen needs to drive the location-permission flow. */
class LocationPermissionController(
    val granted: Boolean,
    val rationaleVisible: Boolean,
    val permanentlyDenied: Boolean,
    val request: () -> Unit,
    val confirmRationale: () -> Unit,
    val dismissRationale: () -> Unit,
)

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Foreground location permission, handled the way Play wants it: a plain
 * rationale before the system dialog, and a Settings shortcut once the user
 * has hard-denied it. Re-checks on every resume so granting from Settings is
 * picked up without a restart.
 */
@Composable
fun rememberLocationPermissionController(onGrantedChange: (Boolean) -> Unit): LocationPermissionController {
    val context = LocalContext.current
    val activity = context as? Activity

    var granted by remember { mutableStateOf(context.hasLocationPermission()) }
    var rationaleVisible by remember { mutableStateOf(false) }
    var requestedOnce by remember { mutableStateOf(false) }

    val permanentlyDenied = requestedOnce && !granted && activity != null &&
        !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        requestedOnce = true
        val ok = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        granted = ok
        onGrantedChange(ok)
    }

    LifecycleResumeEffect(Unit) {
        val now = context.hasLocationPermission()
        if (now != granted) {
            granted = now
            onGrantedChange(now)
        } else {
            onGrantedChange(now)
        }
        onPauseOrDispose { }
    }

    return LocationPermissionController(
        granted = granted,
        rationaleVisible = rationaleVisible,
        permanentlyDenied = permanentlyDenied,
        request = {
            if (granted) onGrantedChange(true) else rationaleVisible = true
        },
        confirmRationale = {
            rationaleVisible = false
            if (permanentlyDenied) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            } else {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }
        },
        dismissRationale = { rationaleVisible = false },
    )
}
