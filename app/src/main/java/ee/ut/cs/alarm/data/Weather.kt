package ee.ut.cs.alarm.data

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false, name = "observations")
data class Observations(
    @field:ElementList(inline = true, entry = "station")
    var stations: List<Weather> = mutableListOf()
)

@Root(name = "station", strict = false)
data class Weather(
    @field:Element(name = "name",  required = false)
    var name: String = "",

    @field:Element(name = "wmocode",  required = false)
    var wmoCode: Int? = null,

    @field:Element(name = "longitude",  required = false)
    var longitude: Double = 0.0,

    @field:Element(name = "latitude",  required = false)
    var latitude: Double = 0.0,
    @field:Element(name = "phenomenon",  required = false)
    var phenomenon: String? = null,
    @field:Element(name = "visibility",  required = false)
    var visibility: Float? = null,
    @field:Element(name = "precipitations",  required = false)
    var precipitations: Float? = null,
    @field:Element(name = "airpressure",  required = false)
    var airPressure: Float? = null,

    @field:Element(name = "relativehumidity",  required = false)
    var relativeHumidity: Float? = null,

    @field:Element(name = "airtemperature",  required = false)
    var airTemperature: Float? = null,

    @field:Element(name = "winddirection",  required = false)
    var windDirection: Float? = null,

    @field:Element(name = "windspeed",  required = false)
    var windSpeed: Float? = null,

    @field:Element(name = "windspeedmax",  required = false)
    var windSpeedMax: Float? = null,

    @field:Element(name = "waterlevel_eh2000",  required = false)
    var waterLevelEH2000: Float? = null,

    @field:Element(name = "watertemperature",  required = false)
    var waterTemperature: Float? = null,

    @field:Element(name = "uvindex",  required = false)
    var uvIndex: Float? = null,

    @field:Element(name = "sunshineduration",  required = false)
    var sunshineDuration: Int? = null,

    @field:Element(name = "globalradiation",  required = false)
    var globalRadiation: Float? = null
)
