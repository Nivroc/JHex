import MyAppError._
import WeatherAPIAlg._
import cats.effect._
import cats.mtl._
import cats.syntax.all._
import cats.tagless.finalAlg
import mouse.all._

@finalAlg
trait WeatherAlg[F[_]] {
  def getWeatherFromAPI(coordinates: Coordinates): F[WeatherCondition]
}
object WeatherAlg {

  def makeReal[F[_]: Async: Ask[*[_], AppConfig]](api: WeatherAPIAlg[F]): F[WeatherAlg[F]] =
    Async[F].delay(new WeatherAlg[F] {
      override def getWeatherFromAPI(coordinates: Coordinates): F[WeatherCondition] =
        for {
          AppConfig(pointsEP, alertsEP, alertsOn) <- Ask[F, AppConfig].ask
          pointsUri = s"$pointsEP${coordinates.latitude},${coordinates.longitude}"
          alertsUri = s"$alertsEP${coordinates.latitude},${coordinates.longitude}"
          PointsAPIResponse(ZoneProps(forecastHourly)) <- api.callAPIService[PointsAPIResponse](pointsUri)
          forecastUrl <- Async[F].fromOption(forecastHourly, EmptyForecast("Forecast is empty"))
          forecastReq = api.callAPIService[ForecastHourly](forecastUrl)
          alertsReq = api.callAPIService[ActiveAlerts](alertsUri)
          (ForecastHourly(ForecastHourlyProps(periods)), ActiveAlerts(features)) <- Async[F]
            .both(forecastReq, alertsOn.fold[F[ActiveAlerts]](alertsReq, ActiveAlerts(Seq.empty[AlertFeatures]).pure))
          currentPeriod <- periods.headOption.fold[F[OneHourPeriod]](Async[F].raiseError(EmptyForecast("Forecast is empty")))(_.pure)
          alerts = features.isEmpty.fold(None, features.map(_.properties).some)
        } yield
          WeatherCondition(currentPeriod.shortForecast, TemperatureFeel.fromInt(currentPeriod.temperature).show, alerts)
    })
}
