# Plugin Variants Resolver

Minimal Kotlin + Spring Boot service modeling **plugin versions per OS/architecture** and resolving the best match for a client.

## Run
```bash
./gradlew bootRun
````

App starts on [http://localhost:8080](http://localhost:8080).

## API

* **PUT /api/plugins** – create or update plugin
* **PUT /api/versions** – add version with variants
* **POST /api/resolve/{pluginId}** – find best version/variant for given `os` and `arch`

## Test

Run all unit and integration tests:
```bash
./gradlew test
```