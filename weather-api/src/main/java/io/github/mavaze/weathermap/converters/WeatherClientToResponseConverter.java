package io.github.mavaze.weathermap.converters;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import io.github.mavaze.weathermap.clients.OpenWeatherResponseDTO;
import io.github.mavaze.weathermap.clients.OpenWeatherResponseDTO.QuarterlyForecast;
import io.github.mavaze.weathermap.clients.OpenWeatherResponseDTO.QuarterlyForecast.Condition;
import io.github.mavaze.weathermap.clients.OpenWeatherResponseDTO.QuarterlyForecast.Summary;
import io.github.mavaze.weathermap.clients.OpenWeatherResponseDTO.QuarterlyForecast.Weather;
import io.github.mavaze.weathermap.clients.OpenWeatherResponseDTO.QuarterlyForecast.Wind;
import io.github.mavaze.weathermap.dtos.WeatherAdvice;
import io.github.mavaze.weathermap.dtos.WeatherForecastResponseDTO;
import io.github.mavaze.weathermap.dtos.WeatherForecastResponseDTO.City;
import io.github.mavaze.weathermap.dtos.WeatherForecastResponseDTO.WeatherForecast;
import io.github.mavaze.weathermap.dtos.WeatherForecastResponseDTO.WeatherForecast.WeatherForecastBuilder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class WeatherClientToResponseConverter implements
        Converter<OpenWeatherResponseDTO, WeatherForecastResponseDTO> {

    @Value("${app.api.forecast.period}")
    private int forecastPeriod = 3;

    @Value("${app.api.forecast.thresholds.high-temp}")
    private int highTempWarningThreshold = 40;

    @Value("${app.api.forecast.thresholds.wind-speed}")
    private int windSpeedWarningThreshold = 10;

    @Override
    @Nullable
    public WeatherForecastResponseDTO convert(final OpenWeatherResponseDTO source) {
        log.trace("Converting external open weather api response: {}", source);

        DateTimeZone offset = DateTimeZone.forOffsetMillis(source.getCity().getTimezone() * 1000);
        DateTime offsetDateTime = DateTime.now(offset);

        final Map<String, WeatherForecast> days = new LinkedHashMap<>();

        for (int i = 0; i < forecastPeriod; i++) {
            offsetDateTime = offsetDateTime.plusDays(1).withTimeAtStartOfDay();

            final String dateText = offsetDateTime.toString("YYYY-MM-dd");
            log.trace("Weather forecast is being built for date: {}", dateText);

            final long startPeriod = offsetDateTime.getMillis() / 1000;
            final long endPeriod = offsetDateTime.plusDays(1).getMillis() / 1000;

            Supplier<Stream<QuarterlyForecast>> forecastStreamSupplier = () -> source.getList().stream()
                    .filter(forecast -> forecast.getDt() >= startPeriod && forecast.getDt() < endPeriod)
                    .peek(forecast -> log.trace("Selecting event for date {}",
                            new DateTime(forecast.getDt() * 1000, offset)));

            final Optional<Float> minTemp = findMinTempOfDay(forecastStreamSupplier);
            final Optional<Float> maxTemp = findMaxTempOfDay(forecastStreamSupplier);
            final Optional<Float> windSpeed = findMaxWindSpeedOfDay(forecastStreamSupplier);

            final List<WeatherAdvice> advices = new ArrayList<>();
            maxTemp.filter(temp -> temp > highTempWarningThreshold)
                    .ifPresent(temp -> advices.add(WeatherAdvice.HIGH_TEMP));
            windSpeed.filter(ws -> ws > windSpeedWarningThreshold)
                    .ifPresent(ws -> advices.add(WeatherAdvice.HIGH_WINDS));

            findWeatherConditionsOfDay(forecastStreamSupplier).forEach(condition -> {
                switch (condition) {
                    case RAIN:
                        advices.add(WeatherAdvice.RAIN);
                        break;
                    case THUNDERSTORM:
                        advices.add(WeatherAdvice.THUNDERSTORM);
                        break;
                    default:
                        // do nothing
                }
            });

            WeatherForecastBuilder builder = WeatherForecast.builder();
            minTemp.ifPresent(t -> builder.minTemp(t));
            maxTemp.ifPresent(t -> builder.maxTemp(t));
            WeatherForecast build = builder.advices(advices).build();
            days.put(dateText, build);
        }

        final WeatherForecastResponseDTO response = WeatherForecastResponseDTO.builder()
                .city(City.builder()
                        .name(source.getCity().getName())
                        .offset(source.getCity().getTimezone())
                        .build())
                .days(days).build();

        log.debug("Response successfully converted to {}", response);
        return response;
    }

    private Set<Condition> findWeatherConditionsOfDay(
            final Supplier<Stream<QuarterlyForecast>> forecastStreamSupplier) {
        return forecastStreamSupplier.get()
                .map(QuarterlyForecast::getWeather)
                .flatMap(List::stream)
                .map(Weather::getMain)
                .collect(toSet());
    }

    private Optional<Float> findMinTempOfDay(final Supplier<Stream<QuarterlyForecast>> forecastStreamSupplier) {
        return forecastStreamSupplier.get()
                .map(QuarterlyForecast::getMain)
                .map(Summary::getTempMin)
                .min(Float::compareTo);
    }

    private Optional<Float> findMaxTempOfDay(final Supplier<Stream<QuarterlyForecast>> forecastStreamSupplier) {
        return forecastStreamSupplier.get()
                .map(QuarterlyForecast::getMain)
                .map(Summary::getTempMax)
                .max(Float::compareTo);
    }

    private Optional<Float> findMaxWindSpeedOfDay(final Supplier<Stream<QuarterlyForecast>> forecastStreamSupplier) {
        return forecastStreamSupplier.get()
                .map(QuarterlyForecast::getWind)
                .map(Wind::getSpeed)
                .max(Float::compareTo);
    }
}
