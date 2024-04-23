import sbt.Keys.scalacOptions

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

inThisBuild(
  List(
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
)


val V = new {
  val cats = "2.9.0"
  val catsEffect = "3.5.4"
  val http4s = "0.23.26"
  val tapir = "1.10.5"
  val mouse = "1.2.3"
  val pureconfig = "0.17.6"
  val circe = "0.14.1"
  val log4cats = "2.6.0"
  val tagless = "0.16.0"
  val mtl = "1.4.0"
  val scalatest = "3.2.18"
  val mockito = "3.2.10.0"
}

val Deps = new {
  val logback = "ch.qos.logback" % "logback-classic" % "1.2.11"
  val cats = "org.typelevel" %% "cats-core" % V.cats
  val ce = "org.typelevel" %% "cats-effect" % V.catsEffect
  val http4s = Seq("org.http4s" %% "http4s-ember-client",
                   "org.http4s" %% "http4s-circe",
                   "org.http4s" %% "http4s-ember-server",
                   "org.http4s" %% "http4s-dsl").map(_ % V.http4s)
  val tapir = Seq("com.softwaremill.sttp.tapir" %% "tapir-http4s-server",
                  "com.softwaremill.sttp.tapir" %% "tapir-json-circe").map(_ % V.tapir)
  val swagger = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % V.tapir
  val mouse = "org.typelevel" %% "mouse" % V.mouse
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % V.pureconfig
  val circe = Seq("io.circe" %% "circe-core",
                  "io.circe" %% "circe-generic",
                  "io.circe" %% "circe-parser").map(_ % V.circe)
  val log4cats = "org.typelevel" %% "log4cats-slf4j" % V.log4cats
  val tagless = "org.typelevel" %% "cats-tagless-macros" % V.tagless
  val mtl = "org.typelevel" %% "cats-mtl" % V.mtl

  val scalatest = "org.scalatest" %% "scalatest" % V.scalatest % Test
  val mockito = "org.scalatestplus" %% "mockito-3-4" % V.mockito % Test
}

lazy val root = (project in file("."))
  .settings(
    name := "JHex",
    libraryDependencies ++= Seq(
        Deps.logback
      , Deps.cats
      , Deps.ce
      , Deps.mouse
      , Deps.swagger
      , Deps.pureconfig
      , Deps.log4cats
      , Deps.tagless
      , Deps.mtl
      , Deps.scalatest
      , Deps.mockito
    )
    ++ Deps.tapir
    ++ Deps.http4s
    ++ Deps.circe
    ,
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-unchecked",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials",
      "-language:postfixOps",
      "-Ymacro-annotations",
      "-Wconf:cat=unused-locals:s,cat=unused-params:s,any:wv"
    )
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
    MergeStrategy.singleOrError
  case PathList("META-INF", "resources", "webjars", "swagger-ui", _*)               =>
    MergeStrategy.singleOrError
  case PathList("META-INF", _*)                                                     => MergeStrategy.discard
  case x                                                                            =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}