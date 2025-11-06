package ee.ut.cs.alarm

import ee.ut.cs.alarm.data.Observations
import ee.ut.cs.alarm.data.Weather
import ee.ut.cs.alarm.service.WeatherAPI
import ee.ut.cs.alarm.service.WeatherServiceImpl
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import retrofit2.Response
import org.junit.Assert.*

class WeatherServiceTest {
    @Mock
    private lateinit var resp: Response<Observations>
    @Mock
    private lateinit var api: WeatherAPI

    private fun mockApi(temp: Float?): WeatherAPI {
        val resp = mock<Response<Observations>> {
            onBlocking { code() } doReturn 200
            onBlocking { body() } doReturn Observations(
                listOf(
                    Weather(name = "S1", latitude = 58.4031746408143, longitude = 26.698386405071687, airTemperature = temp),
                    Weather(name = "S2", latitude = 58.39092684814824, longitude = 26.698157264630773, airTemperature = temp),
                    Weather(name = "S3", latitude = 58.379395626334684, longitude = 26.75635893662323, airTemperature = temp),
                    Weather(name = "S4", latitude = 58.370023732308724, longitude =  26.710530848440193, airTemperature = temp)
                )
            )
        }

         return mock<WeatherAPI> {
            onBlocking { getWeatherObservations() } doReturn resp
        }
    }

    @Test
    fun testLocationBasedCalculation() {
        val api = mockApi(10.0f)
        val service = WeatherServiceImpl(api)
        runBlocking {
            var weather = service.getNearestWeatherObservation(58.40293452891551, 26.710072567558363)
            assertEquals("S1", weather!!.name)
            weather = service.getNearestWeatherObservation(58.39092684814824, 26.704802337417313)
            assertEquals("S2", weather!!.name)
            weather = service.getNearestWeatherObservation(58.3791553524551, 26.77606501454194)
            assertEquals("S3", weather!!.name)
            weather = service.getNearestWeatherObservation(58.36113014692809, 26.711218269762938)
            assertEquals("S4", weather!!.name)
        }
    }

    @Test
    fun testLocationBasedCalculationWithNoAirTemp() {
        val api = mockApi(null)
        val service = WeatherServiceImpl(api)
        runBlocking {
            assertNull(service.getNearestWeatherObservation(58.40293452891551, 26.710072567558363))
        }
    }
}