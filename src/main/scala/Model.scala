import cats.Show
import cats.syntax.all._

final case class Coordinates(latitude: Double, longitude: Double)

sealed trait TemperatureFeel
case object Scalding extends TemperatureFeel
case object Hot extends TemperatureFeel
case object Warm extends TemperatureFeel
case object Chilly extends TemperatureFeel
case object Freezing extends TemperatureFeel

object TemperatureFeel {
  def fromInt(t: Int): TemperatureFeel = t match {
    case temp if temp >= 105 => Scalding
    case temp if temp >= 80 && temp < 105 => Hot
    case temp if temp >= 65 && temp < 80 => Warm
    case temp if temp >= 35 && temp < 65 => Chilly
    case temp if temp < 35 => Freezing
  }

  implicit val show: Show[TemperatureFeel] = {
    case Scalding => "Incredibly Hot, stay inside under AC"
    case Hot => "Hot, don't forget sunscreen"
    case Warm => "Warm, good time for a walk"
    case Chilly => "A bit chilly, a jacket is a good idea"
    case Freezing => "Pretty cold, layers, layers, layers"
  }
}

case class WeatherCondition(condition: String, feel: String, alerts: Option[Seq[AlertProperties]])
case class AppConfig(pointsEP: String, alertsEP: String, showAlerts: Boolean)

case class PointsAPIResponse(properties: ZoneProps)
case class ZoneProps(forecastHourly: Option[String])

case class ActiveAlerts(features: Seq[AlertFeatures])
case class AlertFeatures(properties: AlertProperties)
case class AlertProperties(severity: String, certainty: String, event: String, headline: String)

case class ForecastHourly(properties: ForecastHourlyProps)
case class ForecastHourlyProps(periods: Seq[OneHourPeriod])
case class OneHourPeriod(temperature: Int, shortForecast: String)

sealed abstract class MyAppError(msg: String, underlying: Option[Throwable] = None)
    extends Exception(msg, underlying.getOrElse(new Throwable {}))
object MyAppError {
  case class EmptyForecast(error: String) extends MyAppError("Forecast for today is empty")
  case class ApiErrorResponse(title: Option[String], detail: Option[String]) extends MyAppError(s"$title: $detail")
  case class AnticipatedGenericError(msg: String, t: Throwable) extends MyAppError(msg, t.some)
}
