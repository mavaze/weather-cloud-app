package io.github.mavaze.weathermap.clients;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(name = "open-weather-map", url = "${open-weather-map.feign.url}")
public interface OpenWeatherMapApi {

    @GetMapping("/data/2.5/forecast")
    @Cacheable(cacheNames = { "five-days-forecast" }, key = "{#city.toLowerCase()}")
    Mono<OpenWeatherResponseDTO> queryOpenWeatherMapFor5Day3HourlyForecast(
            @RequestParam(name = "appid", required = true) String appId,
            @RequestParam(name = "q", required = true) String city);
}
