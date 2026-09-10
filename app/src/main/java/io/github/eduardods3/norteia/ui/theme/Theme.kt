package io.github.eduardods3.norteia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Norteia is a deliberately single-look, always-dark app: a compass you glance
 * at outdoors wants maximum contrast and OLED-friendly black, day or night.
 * [darkThemeOverride] exists only so previews/tests can pin the value.
 */
@Composable
fun NorteiaTheme(
    @Suppress("UNUSED_PARAMETER") darkThemeOverride: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalNorteiaColors provides NorteiaExtraColors()) {
        MaterialTheme(
            colorScheme = DarkColors,
            typography = NorteiaTypography,
            content = content,
        )
    }
}

/** Shorthand for the semantic colours: `NorteiaTheme.extra.north` etc. */
object NorteiaTheme {
    val extra: NorteiaExtraColors
        @Composable get() = LocalNorteiaColors.current
}
