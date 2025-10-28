package ee.ut.cs.alarm.data

import android.util.Log
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URL
import org.xmlpull.v1.XmlPullParser as xpp

val logTag = "WEATHER PARSER"

data class Weather(val temperature: Float, val phenomenon: String, val windspeed: Float) {
    companion object {

        private fun getTagInfo(parser: xpp, tag: String): String {
            val evt = parser.next()
            if (evt != xpp.TEXT) {
                Log.e(logTag, "didn't find $tag in measurements")
                return "-1111"
            } else {
                return parser.text
            }
        }

        fun fromRequest(): Weather {
            val TAG_OBSERVATIONS = "observations"
            val TAG_STATION = "station"
            val TAG_NAME = "name"
            val TAG_PHENOMENON = "phenomenon"
            val TAG_TEMPERATURE = "airtemperature"
            val TAG_WINDSPEED = "windspeed"

            val NAME_STATION = "Tartu-Tõravere" // we look at this station for Tartu weather
            // why don't we look at any other weather?
            // because only weather in Tartu is important

            val S_WRONG = -1
            val S_SEARCHING = 1
            val S_SEARCHING_STATION = 2
            val S_IN_STATION = 3

            var state = S_WRONG

            // using basic Java URL. simple URL GET request, no parameters, no issue
            var txt: String = ""
            val url = URL("https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php")
            try {
                txt = url.readText()
            } catch (e: Exception) {
            }
            Log.d(logTag, "length of gotten text is ${txt.length}")

            // use basic simple xml parsing that is provided with android
            // API is simple and only one thing is needed from it so this is optimal
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(StringReader(txt))

            // these values will be stored. -1111 is used as an arbitrary "unset" value
            var temperature: Float = -1111f
            var phenomenon: String = "-1111"
            var windspeed: Float = -1111f

            var successes = 0

            // xml tree format:
            //   observations         // root element
            //     station            // API lists multiple weather stations, we pick one
            //       name             // name of the station
            //       temperature      // in degrees celsius
            //       windspeed        // in meters/second
            //       phenomenon       // English description of weather
            var eventType = parser.eventType
            while (eventType != xpp.END_DOCUMENT) {

                // trying to find the station we're interested in
                if (state == S_WRONG && eventType == xpp.START_TAG && parser.name == TAG_OBSERVATIONS) {
                    Log.d(logTag, "started searching for station")
                    state = S_SEARCHING
                } else if (state == S_SEARCHING && eventType == xpp.START_TAG && parser.name == TAG_STATION) {
                    Log.d(logTag, "started searching in station")
                    state = S_SEARCHING_STATION
                } else if (state == S_SEARCHING_STATION && eventType == xpp.START_TAG && parser.name == TAG_NAME) {
                    val evt = parser.next()
                    if (evt != xpp.TEXT) {
                        // name was empty? skip
                        eventType = parser.next()
                        continue
                    }
                    if (parser.text == NAME_STATION) {
                        // found correct station, set state
                        Log.d(logTag, "found correct station")
                        state = S_IN_STATION
                    }
                }

                // looking for data in the correct station
                else if (state == S_IN_STATION) {
                    if (eventType == xpp.END_TAG && parser.name == TAG_STATION) {
                        // done parsing the intended station, end
                        state = S_WRONG
                        break
                    } else if (eventType == xpp.START_TAG) {
                        // upon finding the parameters we're looking for, store them
                        if (parser.name == TAG_TEMPERATURE) {
                            temperature = getTagInfo(parser, TAG_TEMPERATURE).toFloat()
                        } else if (parser.name == TAG_PHENOMENON) {
                            phenomenon = getTagInfo(parser, TAG_PHENOMENON)
                        } else if (parser.name == TAG_WINDSPEED) {
                            windspeed = getTagInfo(parser, TAG_WINDSPEED).toFloat()
                        }
                    }
                }

                eventType = parser.next()
            }

            return Weather(temperature, phenomenon, windspeed)

        }
    }

    fun hasError(): Boolean {
        return temperature == -1111F || phenomenon == "-1111" || windspeed == -1111F
    }

    fun getString(): String {
        // if we didn't get weather data, don't display it.
        // if the API integration was more important, we'd notify the user.
        // waking up and weather aren't that importantly correlated so we can do this.
        if (hasError()) return ""
        return "$temperature°C | Wind $windspeed m/s | $phenomenon"
    }
}