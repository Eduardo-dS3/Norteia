package io.github.eduardods3.norteia.core.geo

/**
 * The coordinate notations the user can cycle through by tapping the
 * coordinates card. Order here is the cycle order.
 */
enum class CoordinateFormat {
    /** Decimal degrees: -23.55052, -46.63389 */
    DECIMAL,

    /** Degrees / minutes / seconds: 23°33'01.87"S  46°38'02.00"W */
    DMS,

    /** UTM: 23K 351430 mE 7394554 mN */
    UTM,

    /** MGRS: 23K PS 51430 94554 */
    MGRS;

    fun next(): CoordinateFormat = entries[(ordinal + 1) % entries.size]

    /** Human-readable label for the small caption on the card. */
    val label: String
        get() = when (this) {
            DECIMAL -> "Graus decimais"
            DMS -> "GMS"
            UTM -> "UTM"
            MGRS -> "MGRS"
        }

    /** Renders [pos] in this notation. Single source of truth for the UI. */
    fun render(pos: LatLon): String = when (this) {
        DECIMAL -> Dms.decimalPair(pos)
        DMS -> Dms.sexagesimalPair(pos)
        UTM -> Utm.format(pos)
        MGRS -> Utm.toMgrs(pos, digits = 5)
    }
}
