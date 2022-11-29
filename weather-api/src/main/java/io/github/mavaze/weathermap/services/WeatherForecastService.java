package io.github.mavaze.weathermap.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.mavaze.weathermap.clients.OpenWeatherMapApi;
import io.github.mavaze.weathermap.clients.OpenWeatherResponseDTO;
import io.github.mavaze.weathermap.dtos.WeatherForecastResponseDTO;
import lombok.extern.slf4j.Slf4j;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class WeatherForecastService {

    @Value("${open-weather-map.api.appid}")
    private String appId;

    private final ConversionService converter;
    private final OpenWeatherMapApi openWeatherMapApi;
    private final Cache<Object, Object> cache;

    @Autowired
    public WeatherForecastService(final ConversionService converter, final OpenWeatherMapApi openWeatherMapApi,
            final Caffeine<Object, Object> caffeine) {
        this.converter = converter;
        this.openWeatherMapApi = openWeatherMapApi;
        this.cache = caffeine.build();
    }

    @SuppressWarnings("deprecation")
    public WeatherForecastResponseDTO getForecastForCity(final String city) {
        log.info("Sending request to external service to fetch forecast for next 3 days for city [{}]", city);

        Mono<OpenWeatherResponseDTO> forecast = CacheMono.lookup(cache.asMap(), city.toLowerCase())
                .onCacheMissResume(() -> {
                    log.warn("Cache miss for key [#{}] in cache [five-days-forecast]", city);
                    return openWeatherMapApi.queryOpenWeatherMapFor5Day3HourlyForecast(appId, city)
                            .cast(Object.class);
                })
                .cast(OpenWeatherResponseDTO.class);

        return converter.convert(forecast.block(), WeatherForecastResponseDTO.class);
    }
}
