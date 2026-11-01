package com.aura.ambientdashboard.data

import android.util.Log
import com.aura.ambientdashboard.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WeatherSnapshot(
    val locationName: String,
    val temperatureC: Double,
    val condition: String,
    val weatherCode: Int,
    val humidityPercent: Int?,
    val windKph: Double?,
    val fetchedAtMs: Long
) {
    val summary: String
        get() = buildString {
            append(condition)
            humidityPercent?.let { append(" · humidity $it%") }
            windKph?.let { append(" · wind ${"%.0f".format(it)} km/h") }
        }
}

object WeatherCodes {

    fun describe(code: Int): String = when {
        code == 0 -> "Clear sky"
        code in 1..3 -> "Partly cloudy"
        code == 45 || code == 48 -> "Fog"
        code in 51..57 -> "Drizzle"
        code in 61..67 -> "Rain"
        code in 71..77 -> "Snow"
        code in 80..82 -> "Rain showers"
        code == 85 || code == 86 -> "Snow showers"
        code >= 95 -> "Thunderstorm"
        else -> "Unsettled"
    }

    fun iconRes(code: Int): Int = when {
        code == 0 -> R.drawable.ic_weather_clear
        code in 71..77 || code == 85 || code == 86 -> R.drawable.ic_weather_snow
        code in 51..67 || code in 80..82 || code >= 95 -> R.drawable.ic_weather_rain
        else -> R.drawable.ic_weather_cloud
    }
}

class WeatherRepository {

    suspend fun fetchByCity(city: String): WeatherSnapshot? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(city.trim(), "UTF-8")
            val geo = JSONObject(
                read("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=en&format=json")
            )
            val results = geo.optJSONArray("results") ?: return@withContext null
            if (results.length() == 0) return@withContext null
            val first = results.getJSONObject(0)
            val name = buildString {
                append(first.optString("name"))
                val country = first.optString("country_code")
                if (country.isNotEmpty()) append(", $country")
            }
            fetch(first.getDouble("latitude"), first.getDouble("longitude"), name)
        } catch (e: Exception) {
            Log.w(TAG, "City weather lookup failed", e)
            null
        }
    }

    suspend fun fetch(latitude: Double, longitude: Double, locationName: String): WeatherSnapshot? =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject(
                    read("https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude" +
                        "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m")
                )
                val current = json.getJSONObject("current")
                WeatherSnapshot(
                    locationName = locationName,
                    temperatureC = current.getDouble("temperature_2m"),
                    condition = WeatherCodes.describe(current.optInt("weather_code", -1)),
                    weatherCode = current.optInt("weather_code", -1),
                    humidityPercent = if (current.has("relative_humidity_2m")) current.getInt("relative_humidity_2m") else null,
                    windKph = if (current.has("wind_speed_10m")) current.getDouble("wind_speed_10m") else null,
                    fetchedAtMs = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.w(TAG, "Weather fetch failed", e)
                null
            }
        }

    private fun read(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        try {
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TAG = "WeatherRepository"
    }
}
