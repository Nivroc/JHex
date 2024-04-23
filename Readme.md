
## Starting ##

This is a regular sbt project and can be started with 'sbt run' and tested with 'sbt test' <br>
For docker container please use 'docker build -t my-scala-app .' and 'docker run -p 8080:8080 my-scala-app' <br>
Protocol is exactly as described in the problem statement.<br>
Swagger docs are available at /docs in the browser.<br>
Exercise is done in a lightweight IO-only tagless-final manner with hints at proper dependency injection for future development, using only typelevel-affiliated libraries as requested in the task.


## Out of scope ##
- Production-ready wiring
- Performance, GC and JVM tuning

## TODO ##
- String -> Http4s Uri
- Some more test cases
- add json schema verification against API endpoints
- make logging contextual
- add tracing
- add metrics
- add CORS
- add auth(in case of personal profile)
- optics to work with nested responses

## Ideas ##
- Add forecast endpoints
- Add personal profiles with dashboards
- Add alert subscription with notifications
- Add caching for registered users(in case if API is down and for performance)
- Add timezone verification
- Add dashboards with popular cities
- Add rate limiting if this is directly client facing
- Add monitor location function to personal space


## Example ##

/api/v1.0/weather-at?lat=40.58291023834855&long=-88.16947759799667

```json
{
  "condition": "Sunny",
  "feel": "A bit chilly, a jacket is a good idea",
  "alerts": [
    {
      "severity": "Moderate",
      "certainty": "Observed",
      "event": "Special Weather Statement",
      "headline": "Special Weather Statement issued April 22 at 10:33AM CDT by NWS Chicago IL"
    },
    {
      "severity": "Minor",
      "certainty": "Likely",
      "event": "Frost Advisory",
      "headline": "Frost Advisory issued April 22 at 7:47AM CDT until April 22 at 8:00AM CDT by NWS Chicago IL"
    },
    {
      "severity": "Moderate",
      "certainty": "Observed",
      "event": "Special Weather Statement",
      "headline": "Special Weather Statement issued April 22 at 5:32AM CDT by NWS Chicago IL"
    },
    {
      "severity": "Minor",
      "certainty": "Likely",
      "event": "Frost Advisory",
      "headline": "Frost Advisory issued April 22 at 3:20AM CDT until April 22 at 8:00AM CDT by NWS Chicago IL"
    },
    {
      "severity": "Minor",
      "certainty": "Likely",
      "event": "Frost Advisory",
      "headline": "Frost Advisory issued April 21 at 8:37PM CDT until April 22 at 8:00AM CDT by NWS Chicago IL"
    },
    {
      "severity": "Minor",
      "certainty": "Likely",
      "event": "Frost Advisory",
      "headline": "Frost Advisory issued April 21 at 2:29PM CDT until April 22 at 8:00AM CDT by NWS Chicago IL"
    },
    {
      "severity": "Minor",
      "certainty": "Likely",
      "event": "Frost Advisory",
      "headline": "Frost Advisory issued April 21 at 7:47AM CDT until April 21 at 8:00AM CDT by NWS Chicago IL"
    },
    {
      "severity": "Minor",
      "certainty": "Likely",
      "event": "Frost Advisory",
      "headline": "Frost Advisory issued April 21 at 3:16AM CDT until April 21 at 8:00AM CDT by NWS Chicago IL"
    },
    {
      "severity": "Minor",
      "certainty": "Likely",
      "event": "Frost Advisory",
      "headline": "Frost Advisory issued April 20 at 8:47PM CDT until April 21 at 8:00AM CDT by NWS Chicago IL"
    }
  ]
}
```