package io.github.mavaze.weathermap;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.github.mavaze.weathermap.support.StubUtils.setFixedTimeAndExecute;
import static io.github.mavaze.weathermap.support.StubUtils.stubOpenApiConnectionReset;
import static io.github.mavaze.weathermap.support.StubUtils.stubOpenApiWithSuccessResponse;
import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
// import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.github.mavaze.weathermap.support.WireMockExtensions;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WeatherForecastApiTest {

    @LocalServerPort
    private int port;

    @Value("http://localhost:${local.server.port}/api/forecast/query")
    private String baseForcastQueryUrl;

    @RegisterExtension
    static WireMockExtension openWeatherMapApi = WireMockExtensions.forOpenWeatherMapApi();

    @BeforeEach
    public void preTest() {

    }

    @AfterEach
    public void postTest() {
    }

    @Test
    @Disabled("till fixing clock in right way and implementing mock oauth2 security")
    public void verifyFetchAndTransformResponseFromOpenWeatherMapApi() throws Exception {
        stubOpenApiWithSuccessResponse(openWeatherMapApi);

        setFixedTimeAndExecute("2022-11-22T10:00:00Z", () -> {
            // @formatter:off
            given()
                .config(config().jsonConfig(jsonConfig()))
                .header("Content-Type", APPLICATION_JSON_VALUE)
                // .header("Authorization", "Bearer test-auth-bearer")
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
        });
    }

    @Test
    @Disabled("till implementing mock oauth2 security")
    public void verifyOpenWeatherApiFailurePopulatesErrorDto() throws IOException {
        stubOpenApiConnectionReset(openWeatherMapApi);

        // @formatter:off
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            // .header("Authorization", "Bearer test-auth-bearer")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
            .prettyPeek()
        .then()
            .statusCode(INTERNAL_SERVER_ERROR.value())
            .body("message", is("Failed to connect external service!!!"))
            .body("error-code", equalTo(500));
        // @formatter:on

        openWeatherMapApi.verify(1, getRequestedFor(urlMatching("/data/2.5/forecast.*")));
    }

    @Test
    @Disabled("till implementing mock oauth2 security")
    public void verifyUserNotAuthorizedToAccessApi() throws Exception {
        stubOpenApiConnectionReset(openWeatherMapApi);

        // @formatter:off
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            // .header("Authorization", "Bearer test-auth-bearer")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
            .prettyPeek()
        .then()
            .statusCode(FORBIDDEN.value());
        // @formatter:on

        openWeatherMapApi.verify(0, getRequestedFor(urlMatching("/data/2.5/forecast.*")));
    }

    @Test
    @Disabled("till implementing mock oauth2 security")
    public void verifyCachingSkipsApiCallOnMultipleQueries() throws Exception {
        stubOpenApiWithSuccessResponse(openWeatherMapApi);

        // @formatter:off
        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            // .header("Authorization", "Bearer test-auth-bearer")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
        .then()
            .statusCode(OK.value());
        
        // Call same query with same city second time

        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            // .header("Authorization", "Bearer test-auth-bearer")
        .when()
            .get(baseForcastQueryUrl + "?city=Sydney")
        .then()
            .statusCode(OK.value());

        // Call same query with same case sensitive city name third time

        given()
            .config(config().jsonConfig(jsonConfig()))
            .header("Content-Type", APPLICATION_JSON_VALUE)
            // .header("Authorization", "Bearer test-auth-bearer")
            .log().all()
        .when()
            .get(baseForcastQueryUrl + "?city=sYdnEy")
        .then()
            .statusCode(OK.value());

        // @formatter:on

        openWeatherMapApi.verify(1, getRequestedFor(urlMatching("/data/2.5/forecast.*")));
    }
}
