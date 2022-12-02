package io.github.mavaze.weathermap.support;

// import static io.restassured.matcher.RestAssuredMatchers.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
// import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER;
import static java.nio.charset.Charset.defaultCharset;
import static lombok.AccessLevel.PRIVATE;
import static org.mockito.ArgumentMatchers.isA;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StreamUtils.copyToString;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.github.mavaze.weathermap.WeatherForecastApiTest;
import io.github.mavaze.weathermap.dtos.ErrorResponseDTO;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
public final class StubUtils {

    // CAUTION: MockedStatic only works in same thread only, so its usage is limited.
    public static void setFixedTimeAndExecute(String dateFormat, @NonNull Runnable runnable) throws Exception {
        final OffsetDateTime modifiedNow = OffsetDateTime
                .now(Clock.fixed(Instant.parse(dateFormat), ZoneOffset.UTC));
        try (MockedStatic<OffsetDateTime> mockedStatic = Mockito.mockStatic(OffsetDateTime.class);) {
            mockedStatic.when(() -> OffsetDateTime.now(isA(ZoneId.class)))
                    .thenReturn(modifiedNow);
            runnable.run();
        }
    }

    public static MockedStatic<OffsetDateTime> setFixedSystemClock(String dateFormat) {
        final OffsetDateTime modifiedNow = OffsetDateTime
                .now(Clock.fixed(Instant.parse(dateFormat), ZoneOffset.UTC));
        MockedStatic<OffsetDateTime> mockedStatic = Mockito.mockStatic(OffsetDateTime.class);

        mockedStatic.when(OffsetDateTime::now).thenReturn(modifiedNow);

        mockedStatic.when(() -> OffsetDateTime.now(isA(ZoneId.class)))
                .thenReturn(modifiedNow);
        return mockedStatic;
    }

    public static void stubOpenApiWithSuccessResponse(WireMockExtension openWeatherMapApi) throws IOException {
        openWeatherMapApi.stubFor(get(urlMatching("/data/2.5/forecast.*"))
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(copyToString(WeatherForecastApiTest.class.getClassLoader()
                                .getResourceAsStream("sample.json"), defaultCharset()))));
    }

    public static void stubOpenApiConnectionReset(WireMockExtension openWeatherMapApi) {
        openWeatherMapApi.stubFor(get(urlMatching("/data/2.5/forecast.*"))
                .willReturn(aResponse().withStatus(SERVICE_UNAVAILABLE.value())
                        .withFixedDelay(2000).withFault(CONNECTION_RESET_BY_PEER)));
    }

    public static void stubOpenApiInvalidResponseContent(WireMockExtension openWeatherMapApi) {
        openWeatherMapApi.stubFor(get(urlMatching("/data/2.5/forecast.*"))
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("{\"city\": {\"timezone\": \"GMT+530\"}}")));
    }

    public static void stubOpenApiResultNotFound(WireMockExtension openWeatherMapApi) {
        openWeatherMapApi.stubFor(get(urlMatching("/data/2.5/forecast.*"))
                .willReturn(aResponse().withStatus(NOT_FOUND.value())
                        .withBody(toJson(new ErrorResponseDTO(
                                404, "Result not found...", "{\"cod\": \"404\", \"message\": \"city not found\"}")))));

    }

    @SneakyThrows
    public static String toJson(@NonNull Object object) {
        return new ObjectMapper().writeValueAsString(object);
    }
}
