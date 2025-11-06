package ee.ut.cs.alarm

import ee.ut.cs.alarm.service.WeatherAPI
import ee.ut.cs.alarm.service.WeatherAPIHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import retrofit2.create

class WeatherAPITest {
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun TestWeatherAPI() {
        val api = WeatherAPIHelper.getInstance().create<WeatherAPI>()
        runBlocking {
            val resp = api.getWeatherObservations()
            val observations = resp.body()
            assertNotNull(observations)
            assertNotEquals(0, observations?.stations?.size)
            print(observations)
        }
    }
}