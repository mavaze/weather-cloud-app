package io.github.mavaze.weathermap.converters;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
// import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.mavaze.weathermap.clients.OpenWeatherResponseDTO;
import io.github.mavaze.weathermap.dtos.WeatherAdvice;
import io.github.mavaze.weathermap.dtos.WeatherForecastResponseDTO;

public class WeatherClientToResponseConverterTest {

    private OpenWeatherResponseDTO source;
    private static ObjectMapper mapper = new ObjectMapper();

    public WeatherClientToResponseConverterTest() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @BeforeAll
    public static void setup() {
        DateTimeUtils.setCurrentMillisFixed(DateTime.parse("2022-11-19T19:19:19").getMillis());
    }

    @BeforeEach
    public void preTest() throws Exception {
        InputStream fileStream = WeatherClientToResponseConverterTest.class.getClassLoader()
                .getResourceAsStream("sample.json");
        source = mapper.readValue(fileStream, OpenWeatherResponseDTO.class);
    }

    @AfterAll
    public static void teardown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    void verifyConverterFiltersThreeDaysForecastData() throws Exception {
        // given
        WeatherClientToResponseConverter converter = new WeatherClientToResponseConverter();

        // when
        WeatherForecastResponseDTO dest = converter.convert(source);

        // then
        assertNotNull(dest);
        assertThat(dest.getDays(), aMapWithSize(3));
        assertNull(dest.getDays().get("2022-11-20"));
        assertEquals(287.36f, dest.getDays().get("2022-11-21").getMinTemp());
        assertEquals(294.3f, dest.getDays().get("2022-11-21").getMaxTemp());
        assertThat(dest.getDays().get("2022-11-21").getAdvices(), hasItem(WeatherAdvice.HIGH_WINDS));
        assertThat(dest.getDays().get("2022-11-21").getAdvices(), hasItem(WeatherAdvice.THUNDERSTORM));
        assertThat(dest.getDays().get("2022-11-21").getAdvices(), hasItem(WeatherAdvice.HIGH_TEMP));
    }

    @Test
    void verifyChangeInThresholdsChangesAdvises() throws Exception {
        // given
        WeatherClientToResponseConverter converter = new WeatherClientToResponseConverter(2, 300, 1, 3600);

        // when
        WeatherForecastResponseDTO dest = converter.convert(source);

        // then
        assertNotNull(dest);
        assertThat(dest.getDays(), aMapWithSize(2));
        assertThat(dest.getDays().get("2022-11-21").getAdvices(), hasItem(WeatherAdvice.HIGH_WINDS));
        assertThat(dest.getDays().get("2022-11-21").getAdvices(), hasItem(WeatherAdvice.THUNDERSTORM));
        assertThat(dest.getDays().get("2022-11-21").getAdvices(), not(hasItem(WeatherAdvice.HIGH_TEMP)));
    }

    @Test
    void verifyNextDayIsEvaluatedWhenLocalTimeExceedsCutoffSet() throws Exception {
        // given
        WeatherClientToResponseConverter converter = new WeatherClientToResponseConverter(2, 300, 1, 1800);

        // when
        WeatherForecastResponseDTO dest = converter.convert(source);

        // then
        assertNotNull(dest);
        assertNull(dest.getDays().get("2022-11-20"),
                "Current local time has already crossed cutoff. Forecast MUST NOT start from present day");
    }
}
