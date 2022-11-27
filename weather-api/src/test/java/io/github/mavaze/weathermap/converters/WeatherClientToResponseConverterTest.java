package io.github.mavaze.weathermap.converters;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
// import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.mavaze.weathermap.clients.OpenWeatherResponseDTO;
import io.github.mavaze.weathermap.dtos.WeatherAdvice;
import io.github.mavaze.weathermap.dtos.WeatherForecastResponseDTO;

public class WeatherClientToResponseConverterTest {

    private static ObjectMapper mapper = new ObjectMapper();

    public WeatherClientToResponseConverterTest() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @BeforeAll
    public static void preTest() {
        DateTimeUtils.setCurrentMillisFixed(DateTime.parse("2022-11-20T16:16:16").getMillis());
    }

    @AfterAll
    public static void postTest() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void verify() throws Exception {
        // given
        WeatherClientToResponseConverter converter = new WeatherClientToResponseConverter();
        InputStream fileStream = WeatherClientToResponseConverterTest.class.getClassLoader()
                .getResourceAsStream("sample.json");
        OpenWeatherResponseDTO source = mapper.readValue(fileStream, OpenWeatherResponseDTO.class);

        // when
        WeatherForecastResponseDTO dest = converter.convert(source);

        // then
        assertNotNull(dest);
        assertThat(dest.getDays(), aMapWithSize(3));
        assertEquals(dest.getDays().get("2022-11-22").getMinTemp(), 284.77f);
        assertEquals(dest.getDays().get("2022-11-22").getMaxTemp(), 293.64f);
        assertThat(dest.getDays().get("2022-11-22").getAdvices(), hasItem(WeatherAdvice.HIGH_TEMP));
    }

    @Test
    public void verifyChangeInThresholdsChangesAdvises() throws Exception {
        // given
        WeatherClientToResponseConverter converter = new WeatherClientToResponseConverter(2, 300, 1);
        InputStream fileStream = WeatherClientToResponseConverterTest.class.getClassLoader()
                .getResourceAsStream("sample.json");
        OpenWeatherResponseDTO source = mapper.readValue(fileStream, OpenWeatherResponseDTO.class);

        // when
        WeatherForecastResponseDTO dest = converter.convert(source);

        // then
        assertNotNull(dest);
        assertThat(dest.getDays(), aMapWithSize(2));
        assertThat(dest.getDays().get("2022-11-22").getAdvices(), hasItem(WeatherAdvice.HIGH_WINDS));
        assertThat(dest.getDays().get("2022-11-22").getAdvices(), not(hasItem(WeatherAdvice.HIGH_TEMP)));
    }
}
