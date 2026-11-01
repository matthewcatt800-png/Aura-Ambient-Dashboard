package com.aura.ambientdashboard.ambient

enum class AmbientPalette(
    val base: Int,
    val haloA: Int,
    val haloB: Int,
    val particleColor: Int
) {
    MIDNIGHT(0xFF0A0E1A.toInt(), 0x40105A78.toInt(), 0x382A1E68.toInt(), 0x88BFE9F5.toInt()),
    AMOLED(0xFF000000.toInt(), 0x260E4E6E.toInt(), 0x1E241A5E.toInt(), 0x66BFE9F5.toInt()),
    DUSK(0xFF130F2E.toInt(), 0x44302E8C.toInt(), 0x3A5E3A96.toInt(), 0x88D7CBF5.toInt());
}
