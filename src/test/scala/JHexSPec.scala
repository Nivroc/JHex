import cats.data.ReaderT
import cats.effect._
import cats.effect.unsafe.IORuntime
import org.http4s._
import org.http4s.client.Client
import org.scalatest.Inside
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait BaseTest extends AsyncFlatSpec with Matchers with Inside with MockitoSugar with TestData {

  type MyTestApp[A] = ReaderT[IO, AppConfig, A]

  implicit val logger: Logger[MyTestApp] = Slf4jLogger.getLogger[MyTestApp]
  implicit val runtime: IORuntime = IORuntime.global

  def mockResponse(j: String): Response[MyTestApp] = Response[MyTestApp](status = Status.Ok).withEntity(j)

  def testWith(client: Client[MyTestApp], coords: Coordinates): MyTestApp[WeatherCondition] =
    for {
      api: WeatherAPIAlg[MyTestApp] <- WeatherAPIAlg.make[MyTestApp](client)
      w: WeatherAlg[MyTestApp] <- WeatherAlg.makeReal[MyTestApp](api)
      result <- w.getWeatherFromAPI(coords)
    } yield result
}

class JHexSPec extends BaseTest {

  "Weather fetching" should "throw decoding exception on invalid body" in {
    val mockClient: Client[MyTestApp] = Client[MyTestApp] { req =>
      req.uri.renderString match {
        case "points.com/0.0,0.0" => Resource.pure(mockResponse(invalidResponse))
      }
    }
    val ex = intercept[InvalidMessageBodyFailure](testWith(mockClient, Coordinates(0.0, 0.0)).run(testConfig).unsafeRunSync())
    ex.getLocalizedMessage should include("Invalid message body: Could not decode JSON")
  }

  "Weather fetching" should "run normally on valid coordinates" in {
    val mockClient: Client[MyTestApp] = Client[MyTestApp] { req =>
      req.uri.renderString match {
        case "points.com/1.0,1.0" => Resource.pure(mockResponse(validResponse))
        case "validforecast.url" => Resource.pure(mockResponse(validResponseHourly))
        case "alerts.com/1.0,1.0" => Resource.pure(mockResponse(validResponseAlerts))
      }
    }
    val result = testWith(mockClient, Coordinates(1.0, 1.0)).run(testConfig).unsafeRunSync()
    result shouldBe WeatherCondition(
      "Partly Cloudy",
      "A bit chilly, a jacket is a good idea",
      Some(List(AlertProperties("extreme", "for sure", "Hurricane", "every man for himself!")))
    )
  }

  "Weather fetching" should "behave correctly on empty alerts" in {
    val mockClient: Client[MyTestApp] = Client[MyTestApp] { req =>
      req.uri.renderString match {
        case "points.com/2.0,2.0" => Resource.pure(mockResponse(validResponse2))
        case "validforecast2.url" => Resource.pure(mockResponse(validResponseHourly))
        case "alerts.com/2.0,2.0" => Resource.pure(mockResponse(emptyAlerts))
      }
    }
    val result = testWith(mockClient, Coordinates(2.0, 2.0)).run(testConfig).unsafeRunSync()
    result shouldBe WeatherCondition(
      "Partly Cloudy",
      "A bit chilly, a jacket is a good idea",
      None
    )
  }

}
