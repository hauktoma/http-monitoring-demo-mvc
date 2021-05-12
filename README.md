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

## API-KEYS
App populates the DB with model users for simplicity and demonstration purposes.

Available api-keys:

```
    const val USER_API_KEY_1 = "93f39e2f-80de-4033-99ee-249d92736a25"
    const val USER_API_KEY_2 = "dcb20f8a-5657-4f1b-9f7f-ce65739b359e"
```
Example POST call to running service:
```
curl --location --request POST 'localhost:8080/api/v1/monitoredEndpoint' \
--header 'Authorization: ApiKey 93f39e2f-80de-4033-99ee-249d92736a25' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "MyUrl",
    "url": "https://www.url.cz",
    "monitoredInterval": "PT30S"
}'
```
