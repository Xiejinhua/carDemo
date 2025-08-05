package com.desaysv.psmap.adapter.standard

interface IServiceConnectListener {
    fun onServiceConnected()

    fun onServiceDisconnected()

    fun onServiceDied()
}