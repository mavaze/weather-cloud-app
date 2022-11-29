package io.github.mavaze.weathermap;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.github.mavaze.weathermap.support.StubUtils.stubOpenApiConnectionReset;
import static io.github.mavaze.weathermap.support.StubUtils.stubOpenApiInvalidResponseContent;
import static io.github.mavaze.weathermap.support.StubUtils.stubOpenApiResultNotFound;
import static io.github.mavaze.weathermap.support.StubUtils.stubOpenApiWithSuccessResponse;
import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
// import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.github.mavaze.weathermap.support.ApplicationContextTestConfiguration;
import io.github.mavaze.weathermap.support.WireMockExtensions;

@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = { ApplicationContextTestConfiguration.class })
public class WeatherForecastApiTest {

    @LocalServerPort
    private int port;

    @Value("http://localhost:${local.server.port}/api/forecast/query")
    private String baseForcastQueryUrl;

    @RegisterExtension
    static WireMockExtension openWeatherMapApi = WireMockExtensions.forOpenWeatherMapApi();

    @BeforeAll
    public static void setup() {
        DateTimeUtils.setCurrentMillisFixed(DateTime.parse("2022-11-21T10:00:00Z").getMillis());
    }

    @AfterAll
    public static void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    void verifyFetchAndTransformResponseFromOpenWeatherMapApi() throws Exception {
        stubOpenApiWithSuccessResponse(openWeatherMapApi);

        // @formatter:off
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer read-user-token")
            .log().all()
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
            .prettyPeek()
        .then()
            .statusCode(OK.value())
            .body("city.name", is("Sydney"))
            .body("days", aMapWithSize(equalTo(3)))
            .body("days['2022-11-24'].minTemp", equalTo(200.37f));
        // @formatter:on

        openWeatherMapApi.verify(1, getRequestedFor(urlMatching("/data/2.5/forecast.*")));
    }

    @Test
    void verifyOpenWeatherApiFailurePopulatesErrorDto() throws IOException {
        stubOpenApiConnectionReset(openWeatherMapApi);

        // @formatter:off
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer read-user-token")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
            .prettyPeek()
        .then()
            .statusCode(INTERNAL_SERVER_ERROR.value())
            .body("message", is("Failed to connect external service..."))
            .body("error-code", equalTo(500));
        // @formatter:on

        openWeatherMapApi.verify(1, getRequestedFor(urlMatching("/data/2.5/forecast.*")));
    }

    @Test
    void verifyUserNotAuthorizedToAccessApi() throws Exception {
        stubOpenApiConnectionReset(openWeatherMapApi);

        // @formatter:off
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer unauthorized-user-token")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
            .prettyPeek()
        .then()
            .statusCode(FORBIDDEN.value());
        // @formatter:on

        openWeatherMapApi.verify(0, getRequestedFor(urlMatching("/data/2.5/forecast.*")));
    }

    @Test
    void verifyConversionFailureForInvalidOpenApiResponse() throws Exception {
        stubOpenApiInvalidResponseContent(openWeatherMapApi);

        // @formatter:off
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer read-user-token")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
            .prettyPeek()
        .then()
            .statusCode(BAD_REQUEST.value())
            .body("message", is("Failed to convert response object..."))
            .body("error-code", equalTo(400));
        // @formatter:on

        openWeatherMapApi.verify(1, getRequestedFor(urlMatching("/data/2.5/forecast.*")));
    }

    @Test
    void verifyResultNotFoundForGivenInputCity() throws Exception {
        stubOpenApiResultNotFound(openWeatherMapApi);

        // @formatter:off
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer read-user-token")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
            .prettyPeek()
        .then()
            .statusCode(NOT_FOUND.value())
            .body("message", is("Result not found..."))
            .body("error-code", equalTo(404));
        // @formatter:on

        openWeatherMapApi.verify(1, getRequestedFor(urlMatching("/data/2.5/forecast.*")));

    }

    @Test
    void verifyCachingSkipsApiCallOnMultipleQueries() throws Exception {
        stubOpenApiWithSuccessResponse(openWeatherMapApi);

        // @formatter:off
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer read-user-token")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
        .then()
            .statusCode(OK.value());
        
        // Call same query with same city second time
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer read-user-token")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
        .then()
            .statusCode(OK.value());

        // Call same query with same case sensitive city name third time
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer read-user-token")
        .when()
            .get(baseForcastQueryUrl + "?city=sYdnEy")
        .then()
            .statusCode(OK.value());
        // @formatter:on

        openWeatherMapApi.verify(1, getRequestedFor(urlMatching("/data/2.5/forecast.*")));
    }
}
