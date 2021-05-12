# HttpMonitoringDemo-MVC APP

Demo application for monitoring endpoints in SpringMVC and using MySQL.

## Build jar and execute tests

```
./gradlew clean build test
```

## Run with docker

Build image:
```
docker build -t http-monitoring-demo-mvc .
```

Run app with MySql:
```
docker-compose up
```

App should run on `localhost:8080`

## API documentation

API Documentation (swagger/openapi) is by default available at `http://localhost:8080/swagger-ui.html`
