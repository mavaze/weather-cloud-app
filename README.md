# weather-cloud-app

## High Level Diagram

![High Level Diagram](https://raw.githubusercontent.com/mavaze/weather-cloud-app/develop/High_Level_Design.jpg)

[WIP] Activity Flow Diagram

- Structure: Multi module maven project employing latest spring cloud dependencies
- Documentation: Swagger UI on top of API Gateway
- Security: Spring oauth2 authorization server
- Secrets: Secrets like open-waether-map api appid is kept encrypted in properties file
- Client side service discovery
- Testing: Wiremock + Rest Assured
- Profile based configuration (prod, dev)
- Performance: use of reactive components and caching of responses with city name as key  

## Local Execution

* Please add in /etc/hosts below hosts for local resolution ...
  ```
    127.0.1.1       skylark nodeagent weather-cloud-api weather-cloud-gateway weather-cloud-security
    127.0.2.1       weather-api-stage weather-gateway-stage weather-security-stage
    127.0.3.1       weather-api-prod weather-gateway-prod weather-security-prod
  ```


```bash
$ cd weather-cloud-app/build
$ vi .env
# -- comment out properties for default deployment --
# -- with existing properties applications will be accessible by the name of 'stage' --
$ sh deploy.sh
# -- OR --
$ mvn clean install
$ docker-compose build
$ docker-compose up -d
```

## API Documentation with Swagger UI

* Access swagger-ui on http://weather-cloud-gateway:8080 (this is required for CORS)
* Login (hardcoded U: user, P: password) and select scope 'read' to access weather forecast api.

```bash
curl -X 'GET' \
  'http://weather-cloud-api:9090/api/forecast/query?city=London' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer <jwt-token>'
```
```json
{
  "city": {
    "name": "Sydney",
    "offset": 39600
  },
  "days": {
    "2022-11-25": {
      "minTemp": 280.24,
      "maxTemp": 280.24,
      "advices": [
        "Use sunscreen lotion"
      ]
    },
    "2022-11-26": {
      "minTemp": 280.14,
      "maxTemp": 280.14,
      "advices": [
        "Use sunscreen lotion",
        "Carry umbrella"
      ]
    },
    "2022-11-27": {
      "minTemp": 282.32,
      "maxTemp": 282.32,
      "advices": [
        "Use sunscreen lotion",
        "Carry umbrella"
      ]
    }
  }
}
```

## Continuous Integration

* Currently CI is achieved with github actions. Following code commit/merge in `develop` branch an action triggers maven build along with test execution.
* Additionally CI is achieved with Jenkinsfile pipeline in order to integrate with Jenkins
* Jacoco code coverage and integration with sonarcloud for sonar analysis.
  * Plugins needed: Maven3, JDK11, Sonar Server (and Scanner), Docker (WIP)
  * For local sonar report, update ~/.m2/settings.xml with following ...
    ```
    <profile>
        <id>sonar</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <!-- Optional URL to server. Default value is http://localhost:9000 -->
            <sonar.host.url>https://sonarcloud.io</sonar.host.url>
            <sonar.login>sonarcloud-api-key</sonar.login>
            <sonar.organization>mavaze-github</sonar.organization>
            <sonar.password/>
        </properties>
    </profile>
    ```
    and execute command `mvn clean verify sonar:sonar -Pcoverage`
  * And for Jenkins update 'SonarQube installations' in Manage COnfiguration section, for server url, token, additional mandatory arguments like sonar.organization and sonar.projectKey etc.

## Continuous Delivery
* Artificats with SNAPSHOT versions will not be generated.
* Before release, versions need to be bumbed up with no mention of SNAPSHOT.
  ```bash
  mvn release:clean release:prepare
  ```
  Above command updates pom.xml of all modules and pushes the changes to repo and tag it.
* Manually generate github release from generated tag.
* [WIP] Following release will trigger github action which will push docker images to mavaze.jfrog.io artifactory.

## Encrypting secrets

In Spring cloud based microservices we generally delegate this responsibility to config server which provides 2 endpoints to encrypt and decrypt the keys. In order to not to introduce new service for this POC, an alternative method has been followed ...

```bash
# Download spring boot cli and install spring cloud cli
tar xvfz spring-boot-cli-2.7.5-bin.tar.gz
spring install org.springframework.cloud:spring-cloud-cli:3.1.1

# Generate JKS certificate
$ keytool -genkeypair -alias mytestkey -keyalg RSA -dname "CN=Mayuresh Vaze,OU=LearnByCoding,O=Workspace,L=Pune,ST=Maharashtra,C=IN" -keypass changeme -keystore server.jks -storepass changeme -keysize 2048
Warning:
The JKS keystore uses a proprietary format. It is recommended to migrate to PKCS12 which is an industry standard format using "keytool -importkeystore -srckeystore server.jks -destkeystore server.jks -deststoretype pkcs12".

# Migrate JKS certificate to PKC12
$ keytool -importkeystore -srckeystore server.jks -destkeystore server.jks -deststoretype pkcs12
Import command completed:  1 entries successfully imported, 0 entries failed or cancelled
Warning:
Migrated "server.jks" to Non JKS/JCEKS. The JKS keystore is backed up as "server.jks.old".

$ openssl pkcs12 -in server.jks -out server.pem
$ openssl rsa -in server.pem -pubout > server.pub

# Use spring boot CLI to encrypt the key
$ spring encrypt <property's secret value e.g. 'mysecretpassword'> -key @server.pub -p open-weather-map.api.appid
open-weather-map.api.appid={cipher}AQA9vS7k6STBbSxremLisfNYzcrw7VnrvPhnGA0MDPO6GfM/H/PhJi2odR+iO8XtblzMp9INdZt8Sdjvfmzxb7KWeSDdwFnWVt+/VqCviXfHqWRduzYeXPQ3cdB/0/u74wunXVohV8+uYCVQ2tRtpC+OwDZn/5+28JzYJz9egg61N6FwZ9y5URLhHJzpaBIAfn9eVekDfGlM7bGBlOftLP+F4+R5BC9zOzuc2Tpkzaa2Hi+u/7PAkLDF/i7nET6qrIuo5fdT9MahRMMOsGs7TEQP5jAAT6/EtewZ3djYqA3PyBSOjjWZY6DvUcd8ErPsQvkHkrD2HF1vZQAdd9zYQnZxNG9N/nhSn19hBlzyg+Pl8kA275dSNNflSCie11esS4kfifmQW7c3EF2+OUBDvvA4
```
It is strongly recommended to install config-server to avoid all this hassle and more control on externalized configuration.

## Hard coded values
* Gateway credentials:
  * User: gateway
  * Password: secret
* In memory demo user:
  * User: user
  * Password: password
  * Permission: read
* Simple discovery client with host details fixed in application.yml (this will go away with integration with eureka)

## Future Scope
* Integrate with Eureka/Consul server for service discovery
* Integrate with config server for externalization of configuration
* Implement circuit breaker using resilience4j around call to OpenWeatherMap API.
* Enhance Jenkins pipeline to build docker image and push it to dockerhub or mavaze.jfrog.io
* Tuning parameters like jvm, timeouts, gzip, cors, cache etc.
* More parameters fo deployment script

## References
* [weather-cloud-app](https://github.com/mavaze/weather-cloud-app) - This very application
* [Bahubali](https://github.com/mavaze/bahubali) - Showcasing app with no libraries other than standard JDK
* [HTTP2 Mock Server](https://github.com/mavaze/http2-mockserver) - A requirement in day to day work triggered this implmentation
* [Revolute Banking](https://github.com/mavaze/revolut-banking) - Uses jersey instead of spring framework