package io.github.eduardods3.norteia.core.geo

/** A plain WGS-84 geographic position. Immutable value type. */
data class LatLon(val latitude: Double, val longitude: Double) {
    init {
        require(latitude in -90.0..90.0) { "latitude out of range: $latitude" }
        require(longitude in -180.0..180.0) { "longitude out of range: $longitude" }
    }
}
