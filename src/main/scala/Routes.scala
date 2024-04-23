import MyAppError._
import cats._
import cats.effect._
import cats.syntax.all._
import io.circe.generic.auto._
import mouse.all._
import org.http4s.HttpRoutes
import org.typelevel.log4cats.Logger
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.interceptor.decodefailure.DefaultDecodeFailureHandler
import sttp.tapir.server.interceptor.exception.DefaultExceptionHandler
import sttp.tapir.server.interceptor.reject.DefaultRejectHandler
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Routes {

  private def endpoints[F[_]: Async: Logger: WeatherAlg] = List(healthCheck[F], getCoordsEp[F])

  private def swaggerEndpoints[F[_]: Async: Logger: WeatherAlg] =
    SwaggerInterpreter().fromServerEndpoints[F](endpoints[F], "JHex", "1.0")

  def routes[F[_]: Async: Logger: WeatherAlg]: F[HttpRoutes[F]] =
    Sync[F].delay(Http4sServerInterpreter[F](defaultServerOptions[F]).toRoutes(endpoints[F]))

  def routesSwagger[F[_]: Async: Logger: WeatherAlg]: F[HttpRoutes[F]] =
    Sync[F].delay(Http4sServerInterpreter[F](defaultServerOptions[F]).toRoutes(swaggerEndpoints[F]))

  private lazy val errorResponseMapping: EndpointOutput.OneOf[MyAppError, MyAppError] =
    oneOf[MyAppError](
      oneOfVariant(
        StatusCode.NotFound,
        jsonBody[EmptyForecast].description("Forecast is empty")
      ),
      oneOfVariant(
        StatusCode.ServiceUnavailable,
        jsonBody[ApiErrorResponse].description("Api is unable to provide data")
      )
    )

  private def healthy[F[_]: Monad]: F[String] = Monad[F].pure("Healthy")
  private def healthCheck[F[_]: Monad] = endpoint.in("healthcheck").out(stringBody).serverLogicSuccess(_ => healthy)

  private val apiV1Ep: Endpoint[Unit, Unit, MyAppError, Unit, Any] = endpoint
    .in("api" / "v1.0")
    .errorOut(errorResponseMapping)

  private def getCoordsEp[F[_]: WeatherAlg: MonadThrow: Logger] =
    apiV1Ep
      .description("Get weather at coordinates")
      .get
      .in("weather-at")
      .in(weatherAtInput)
      .out(jsonBody[WeatherCondition])
      .serverLogic((WeatherAlg[F].getWeatherFromAPI _).andThen(handleAppErrors[F, WeatherCondition]))

  private val weatherAtInput = query[Double]("lat")
    .validate(Validator.min(-90.0).and(Validator.max(90.0)))
    .and(query[Double]("long").validate(Validator.min(-180.0).and(Validator.max(180.0))))
    .mapTo[Coordinates]
    .map[Coordinates] { (x: Coordinates) =>
      Coordinates(
        truncateAt(x.latitude, 4),
        truncateAt(x.longitude, 4)
      )
    }(identity)

  private def handleAppErrors[F[_]: MonadThrow: Logger, T](f: F[T]): F[Either[MyAppError, T]] =
    ApplicativeThrow[F].attemptNarrow[MyAppError, T](f).leftFlatMapF { appErr =>
      Logger[F].error(s"Got known error: ${appErr.getLocalizedMessage}") *> ApplicativeThrow[F].pure(Left(appErr))
    }

  private def defaultServerOptions[F[_]: Sync: Logger]: Http4sServerOptions[F] =
    Http4sServerOptions
      .customiseInterceptors[F]
      .rejectHandler(DefaultRejectHandler[F])
      .decodeFailureHandler(DefaultDecodeFailureHandler[F])
      .exceptionHandler(DefaultExceptionHandler[F])
      .serverLog(
        Http4sServerOptions
          .defaultServerLog[F]
          .copy(
            logLogicExceptions = true,
            logWhenReceived = true,
            logWhenHandled = true
          )
      )
      .options

  private def truncateAt(n: Double, p: Int): Double = { val s = math pow (10, p); (math floor n * s) / s }

}
