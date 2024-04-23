FROM mozilla/sbt:latest as build
WORKDIR /app

COPY ./project /app/project
COPY ./build.sbt /app
COPY ./src /app/src

RUN sbt assembly

FROM openjdk:8-jre-slim
WORKDIR /app

COPY --from=build /app/target/scala-*/JHex-assembly-*.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]