trait TestData {

  val testConfig: AppConfig = AppConfig("points.com/", "alerts.com/", showAlerts = true)

  val invalidResponse = """{"content":"Error Mocked content"}"""
  val validResponse = """{"properties":{"forecastHourly" : "validforecast.url"}}"""
  val validResponseHourly: String =
    """{"properties":{"periods":[
      |{"temperature": 60,
      |"shortForecast": "Partly Cloudy"
      |}]}}""".stripMargin
  val validResponseAlerts: String =
    """{"features":[{"properties" :
      |{"severity":"extreme", "certainty": "for sure", "event": "Hurricane", "headline": "every man for himself!"}
      |}]}""".stripMargin

  val validResponse2 = """{"properties":{"forecastHourly" : "validforecast2.url"}}"""
  val emptyAlerts: String =
    """{"features":[]}""".stripMargin

  val validErrorResponse: String = """{
  |     "title": "Unexpected Problem",
  |     "status": 500,
  |     "detail": "An unexpected problem has occurred. If this error continues, please contact support"
  | }""".stripMargin
}
