import MyAppError.ApiErrorResponse
import cats.effect.{Async, Concurrent}
import cats.syntax.all._
import cats.tagless.finalAlg
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.client.Client

@finalAlg
trait WeatherAPIAlg[F[_]] {
  def callAPIService[T: EntityDecoder[F, *]](url: String): F[T]
}

object WeatherAPIAlg {

  implicit def AADecoder[F[_]: Concurrent]: EntityDecoder[F, ActiveAlerts] = jsonOf[F, ActiveAlerts]
  implicit def APIDecoder[F[_]: Concurrent]: EntityDecoder[F, PointsAPIResponse] = jsonOf[F, PointsAPIResponse]
  implicit def FHDecoder[F[_]: Concurrent]: EntityDecoder[F, ForecastHourly] = jsonOf[F, ForecastHourly]
  implicit def ErrDecoder[F[_]: Concurrent]: EntityDecoder[F, ApiErrorResponse] = jsonOf[F, ApiErrorResponse]

  def make[F[_]: Async](client: Client[F]): F[WeatherAPIAlg[F]] =
    Async[F].delay(new WeatherAPIAlg[F] {
      override def callAPIService[T: EntityDecoder[F, *]](url: String): F[T] =
        client.expectOr[T](url)(_.as[ApiErrorResponse] >>= Async[F].raiseError)
    })
}
