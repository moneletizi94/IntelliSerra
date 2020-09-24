package it.unibo.intelliserra.examples.sensor

import it.unibo.intelliserra.examples.sensor.OpenWeatherService.ImplicitJsonParsers.WeatherJsonReader
import it.unibo.intelliserra.examples.sensor.OpenWeatherService.Location.LocationRequest
import it.unibo.intelliserra.examples.sensor.WeatherService.WeatherData
import spray.json.{JsArray, JsObject, JsString, JsValue, JsonReader}

import scala.io.Source
import scala.util.Try
import spray.json._

trait WeatherService {
  def provider: String
  def currentWeather(): Option[WeatherData]
}

object WeatherService {
  case class WeatherData(state: String /*, temperature: Double, humidity: Double, pressure: Double*/)
}

object OpenWeatherService {

  private val URL = "http://api.openweathermap.org/data/2.5/weather"
  private val API_KEY = "46091eb1ae1a37e9ed5440ed8bb24158"

  object Location {
    final case class LocationRequest(requestUri: String)

    def fromCityName(cityName: String): LocationRequest =
      LocationRequest(s"$URL?q=$cityName")

    def fromCoordinate(coordinate: (Double, Double)): LocationRequest =
      LocationRequest(s"$URL?lat=${coordinate._1}&lon=${coordinate._2}")
  }

  def apply(locationRequest: LocationRequest): OpenWeatherService = new OpenWeatherService(API_KEY, locationRequest)

  private[sensor] class OpenWeatherService(private val apiKey: String,
                                            private val locationRequest: LocationRequest) extends WeatherService {

    override def provider: String = "openweathermap.org"

    override def currentWeather(): Option[WeatherData] = {
      mkStringSource(Source.fromURL(s"${locationRequest.requestUri}&appid=$apiKey"))
        .fold(_ => None, Option(_))
        .map(_.parseJson.convertTo[WeatherData](WeatherJsonReader))
    }

    private def mkStringSource(source: Source): Try[String] =
      Try {
        val response = source.mkString
        source.close()
        response
      }
  }


  object ImplicitJsonParsers {
    implicit object WeatherJsonReader extends JsonReader[WeatherData] {
      override def read(json: JsValue): WeatherData = parseWeatherStatus(json) match {
        case Some(value) => WeatherData(value)
        case None => deserializationError("state not found")
      }

      private def parseWeatherStatus(json: JsValue): Option[String] = json match {
        case JsObject(fields) => fields.get("weather").flatMap {
          case JsArray(Vector(JsObject(weatherFields))) => weatherFields.get("main").map { case JsString(state) => state }
          case _ => None
        }
        case _ => None
      }
      /*
      private def parseMainParameters(json: JsValue): List[Double] =
        json.asJsObject.fields.get("main").fold(List[Double]()) {
          obj => obj.asJsObject.getFields("humidity", "temp", "pressure") map { case JsNumber(value) => value.toDouble } toList
        } */
    }
  }
}
