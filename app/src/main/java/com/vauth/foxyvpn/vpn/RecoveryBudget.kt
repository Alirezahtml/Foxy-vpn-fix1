package com.vauth.foxyvpn.vpn

internal class RecoveryBudget(
    private val maxReconnectsInWindow: Int = 4,
    private val windowMillis: Long = 120_000L,
) {
    private val timestamps = ArrayDeque<Long>()

    @Synchronized
    fun tryAcquire(nowMillis: Long = System.currentTimeMillis()): Boolean {
        while (timestamps.isNotEmpty() && nowMillis - timestamps.first() > windowMillis) {
            timestamps.removeFirst()
        }
        if (timestamps.size >= maxReconnectsInWindow) {
            return false
        }
        timestamps.addLast(nowMillis)
        return true
    }

    @Synchronized
    fun reset() {
        timestamps.clear()
    }
}
