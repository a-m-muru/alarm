package ee.ut.cs.alarm.service

import ee.ut.cs.alarm.data.Observations
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET

interface WeatherAPI {
    @GET("/ilma_andmed/xml/observations.php")
    suspend fun getWeatherObservations(): Response<Observations>
}

object WeatherAPIHelper {
    const val BASE_URL = "https://www.ilmateenistus.ee"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
    }
}