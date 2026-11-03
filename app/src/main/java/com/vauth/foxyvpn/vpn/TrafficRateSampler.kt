package com.vauth.foxyvpn.vpn

internal data class TrafficRates(
    val uplinkBytesPerSecond: Long,
    val downlinkBytesPerSecond: Long,
)

internal class TrafficRateSampler {
    private var lastSampledAtNanos: Long = 0L
    private var lastUplinkTotal: Long = 0L
    private var lastDownlinkTotal: Long = 0L

    fun sample(
        uplinkTotalBytes: Long,
        downlinkTotalBytes: Long,
        nowNanos: Long = System.nanoTime(),
    ): TrafficRates {
        val prevTime = lastSampledAtNanos
        val prevUp = lastUplinkTotal
        val prevDown = lastDownlinkTotal

        lastSampledAtNanos = nowNanos
        lastUplinkTotal = uplinkTotalBytes
        lastDownlinkTotal = downlinkTotalBytes

        if (prevTime == 0L || nowNanos <= prevTime) {
            return TrafficRates(0L, 0L)
        }
        val elapsedNanos = (nowNanos - prevTime).coerceAtLeast(1_000_000L)
        val deltaUp = (uplinkTotalBytes - prevUp).coerceAtLeast(0L)
        val deltaDown = (downlinkTotalBytes - prevDown).coerceAtLeast(0L)
        val upBps = (deltaUp * 1_000_000_000L) / elapsedNanos
        val downBps = (deltaDown * 1_000_000_000L) / elapsedNanos
        return TrafficRates(upBps, downBps)
    }

    fun reset() {
        lastSampledAtNanos = 0L
        lastUplinkTotal = 0L
        lastDownlinkTotal = 0L
    }
}
