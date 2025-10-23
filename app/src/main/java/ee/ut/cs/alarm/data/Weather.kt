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
                return "-1"
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

            val NAME_STATION = "Tartu-Tõravere"

            val S_WRONG = -1
            val S_SEARCHING = 1
            val S_SEARCHING_STATION = 2
            val S_IN_STATION = 3

            var state = S_WRONG

            val url = URL("https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php")
            val txt = url.readText();
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(StringReader(txt))

            var temperature: Float = -1f
            var phenomenon: String = ""
            var windspeed: Float = -1f

            var eventType = parser.eventType
            while (eventType != xpp.END_DOCUMENT) {

                // trying to find the station we're interested in
                if (state == S_WRONG && eventType == xpp.START_TAG && parser.text == TAG_OBSERVATIONS) {
                    state = S_SEARCHING
                } else if (state == S_SEARCHING && eventType == xpp.START_TAG && parser.text == TAG_STATION) {
                    state = S_SEARCHING_STATION
                } else if (state == S_SEARCHING_STATION && eventType == xpp.START_TAG && parser.text == TAG_NAME) {
                    val evt = parser.next()
                    if (evt != xpp.TEXT) continue // name was empty??
                    if (parser.text == NAME_STATION) {
                        // found correct station
                        state = S_IN_STATION
                    }
                }

                // looking for data in the station
                else if (state == S_IN_STATION) {
                    if (eventType == xpp.END_TAG && parser.text == TAG_STATION) {
                        // done parsing the intended station
                        state = S_WRONG
                        break
                    } else if (eventType == xpp.START_TAG) {

                        if (parser.text == TAG_TEMPERATURE) {
                            temperature = getTagInfo(parser, TAG_TEMPERATURE).toFloat()
                        } else if (parser.text == TAG_PHENOMENON) {
                            phenomenon = getTagInfo(parser, TAG_PHENOMENON)
                        } else if (parser.text == TAG_WINDSPEED) {
                            windspeed = getTagInfo(parser, TAG_WINDSPEED).toFloat()
                        }
                    }
                }

                eventType = parser.next()
            }
            return Weather(temperature, phenomenon, windspeed)

        }
    }
}