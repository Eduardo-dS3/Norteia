package io.github.eduardods3.norteia.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.getSystemService
import io.github.eduardods3.norteia.core.geo.LatLon
import java.util.Locale

/**
 * Share / copy / open-in-maps helpers.
 *
 * "Open in maps" fires a standard `geo:` intent, so it uses whatever map app
 * the user already has — no Google Maps SDK, no API key, no cost, and it works
 * with OsmAnd, Organic Maps, Waze, etc. too.
 */
object LocationActions {

    fun copy(context: Context, label: String, text: String) {
        context.getSystemService<ClipboardManager>()
            ?.setPrimaryClip(ClipData.newPlainText(label, text))
        // Android 13+ shows its own copy confirmation; older versions get a toast.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
        }
    }

    fun share(context: Context, pos: LatLon, prettyCoordinates: String) {
        val geo = geoUri(pos)
        val body = buildString {
            append(prettyCoordinates)
            append('\n')
            append(geo)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar localização"))
    }

    fun openInMaps(context: Context, pos: LatLon) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri(pos)))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Nenhum app de mapas instalado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun geoUri(pos: LatLon): String = String.format(
        Locale.US,
        "geo:%f,%f?q=%f,%f",
        pos.latitude, pos.longitude, pos.latitude, pos.longitude,
    )
}
