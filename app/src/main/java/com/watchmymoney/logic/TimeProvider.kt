package com.watchmymoney.logic

object TimeProvider {
    var currentTimeMillisProvider: () -> Long = { System.currentTimeMillis() }

    fun currentTimeMillis(): Long = currentTimeMillisProvider()
}
