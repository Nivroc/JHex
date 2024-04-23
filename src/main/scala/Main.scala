import cats.data.ReaderT
import cats.effect._
import cats.mtl._
import cats.syntax.all._
import com.comcast.ip4s.IpLiteralSyntax
import org.http4s.HttpRoutes
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig._
import pureconfig.generic.auto._

object Main extends IOApp {

  type MyApp[A] = ReaderT[IO, AppConfig, A]

  override def run(args: List[String]): IO[ExitCode] = runF[MyApp].run(ConfigSource.default.loadOrThrow[AppConfig])

  private def runF[F[_]: Async: Ask[*[_], AppConfig]]: F[ExitCode] =
    for {
      implicit0(l: Logger[F]) <- Slf4jLogger.create[F]
      _ <- l.info("Starting the app.")
      client = EmberClientBuilder
        .default[F]
        .build
        .map(
          org.http4s.client.middleware.Logger[F](
            logHeaders = false,
            logBody = true,
            logAction = Some(x => Logger[F].debug(x))
          )
        )
      implicit0(api: WeatherAPIAlg[F]) <- client.use(WeatherAPIAlg.make[F])
      implicit0(w: WeatherAlg[F]) <- WeatherAlg.makeReal[F](api)
      rts <- Routes.routes[F]
      rtsSwagger <- Routes.routesSwagger[F]
      _ <- server[F](rts <+> rtsSwagger)
    } yield ExitCode.Success

  def server[F[_]: Async](routes: HttpRoutes[F]): F[Unit] =
    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build
      .use(_ => Async[F].never[Unit])

}
