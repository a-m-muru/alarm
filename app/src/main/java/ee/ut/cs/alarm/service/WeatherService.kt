package ee.ut.cs.alarm.service

import android.util.Log
import ee.ut.cs.alarm.data.Weather
import kotlin.math.cos
import kotlin.math.asin
import kotlin.math.sqrt
import kotlin.math.PI

interface WeatherService {
    suspend fun getDefaultWeatherObservation(): Weather?
    suspend fun getNearestWeatherObservation(lat: Double, lng: Double): Weather?
}


class WeatherServiceImpl(private val api: WeatherAPI) : WeatherService {
    companion object {
        const val EARTH_R = 6371
        const val LOG_TAG = "WeatherService"

        // Tallinn defaultism
        const val DEFAULT_NAME = "Tallinn-Harku"
    }

    private fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLng = (lng2 - lng1) * PI / 180.0

        return 2 * EARTH_R * asin(sqrt((1 - cos(dLat) + cos(lat1) * cos(lat2) * (1 - cos(dLng))) / 2))
    }

    override suspend fun getDefaultWeatherObservation(): Weather? {
        val resp = api.getWeatherObservations()
        if (resp.code() != 200) {
            Log.e(LOG_TAG, String.format("Could not fetch weather data - non-200 response code %d", resp.code()))
            return null
        }

        val observations = resp.body()
        val stations = observations?.stations ?: listOf()
        for (station in stations) {
            if (station.name == DEFAULT_NAME)
                return station
        }

        if (stations.size > 0)
            return stations.get(0)
        return null
    }

    override suspend fun getNearestWeatherObservation(lat: Double, lng: Double): Weather? {
        val resp = api.getWeatherObservations()
        if (resp.code() != 200)
            return null

        val observations = resp.body()
        val stations = observations?.stations ?: listOf()

        var currentDistance = Double.MAX_VALUE
        var currentIndex = 0
        for (i in 0..<stations.size) {
            val dist = haversine(lat, lng, stations.get(i).latitude, stations.get(i).longitude)
            if (dist < currentDistance && stations.get(i).airTemperature != null) {
                currentDistance = dist
                currentIndex = i
            }
        }

        if (currentDistance != Double.MAX_VALUE)
            return stations.get(currentIndex)
        return null
    }
}