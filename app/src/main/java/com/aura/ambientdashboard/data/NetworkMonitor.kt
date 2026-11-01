package com.aura.ambientdashboard.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import java.net.Inet4Address
import java.net.NetworkInterface

data class NetworkState(
    val wifiConnected: Boolean,
    val wifiName: String?,
    val wifiLinkSpeedMbps: Int?,
    val mobileConnected: Boolean,
    val mobileType: String?,
    val internetValidated: Boolean,
    val airplaneMode: Boolean
) {
    val connectivitySummary: String
        get() = when {
            airplaneMode -> "Airplane mode"
            wifiConnected && internetValidated -> "Wi-Fi · online"
            wifiConnected -> "Wi-Fi · no internet"
            mobileConnected && internetValidated -> "Mobile · online"
            mobileConnected -> "Mobile · no internet"
            else -> "Offline"
        }
}

object NetworkMonitor {

    fun state(context: Context): NetworkState {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var wifi = false
        var mobile = false
        var validated = false
        for (network in connectivityManager.allNetworks) {
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: continue
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) wifi = true
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) mobile = true
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            ) {
                validated = true
            }
        }

        return NetworkState(
            wifiConnected = wifi,
            wifiName = wifiName(context),
            wifiLinkSpeedMbps = wifiLinkSpeed(context),
            mobileConnected = mobile,
            mobileType = mobileNetworkType(context),
            internetValidated = validated,
            airplaneMode = airplaneMode(context)
        )
    }

    fun localIpv4Address(): String? = try {
        NetworkInterface.getNetworkInterfaces().asSequence()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.asSequence() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress && it.isSiteLocalAddress }
            ?.hostAddress
    } catch (e: Exception) {
        null
    }

    private fun wifiName(context: Context): String? = try {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
            wifiManager?.connectionInfo?.ssid
                ?.trim('"')
                ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    private fun wifiLinkSpeed(context: Context): Int? = try {
        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
        wifiManager?.connectionInfo?.linkSpeed?.takeIf { it > 0 }
    } catch (e: Exception) {
        null
    }

    private fun mobileNetworkType(context: Context): String? = try {
        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        telephonyManager?.let { typeName(it.dataNetworkType) }
    } catch (e: Exception) {
        null
    }

    private fun airplaneMode(context: Context): Boolean = try {
        Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
    } catch (e: Exception) {
        false
    }

    private fun typeName(type: Int): String? = when (type) {
        TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
        else -> null
    }
}
