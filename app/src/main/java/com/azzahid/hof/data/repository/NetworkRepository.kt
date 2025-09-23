package com.azzahid.hof.data.repository

import java.net.Inet4Address
import java.net.NetworkInterface

interface NetworkRepository {
    fun getNetworkAddresses(port: String): List<Pair<String, String>>
    fun getLocalIpAddress(): String
}

class AndroidNetworkRepository : NetworkRepository {
    override fun getNetworkAddresses(port: String): List<Pair<String, String>> {
        return buildList {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                for (networkInterface in interfaces) {
                    if (!networkInterface.isLoopback && networkInterface.isUp) {
                        for (address in networkInterface.inetAddresses) {
                            if (!address.isLoopbackAddress && address.hostAddress?.contains(':') == false) {
                                val url = "http://${address.hostAddress}:$port"
                                val interfaceName = networkInterface.displayName
                                add(url to interfaceName)
                            }
                        }
                    }
                }
                add("http://localhost:$port" to "Localhost")
            } catch (e: Exception) {
                add("http://localhost:$port" to "Localhost")
            }
        }
    }

    override fun getLocalIpAddress(): String = try {
        NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
            ?.hostAddress ?: "127.0.0.1"
    } catch (_: Exception) {
        "127.0.0.1"
    }
}