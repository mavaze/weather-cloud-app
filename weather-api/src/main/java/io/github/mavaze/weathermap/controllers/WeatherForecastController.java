package io.github.mavaze.weathermap.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.mavaze.weathermap.contract.WeatherForecastApi;
import io.github.mavaze.weathermap.dtos.WeatherForecastResponseDTO;
import io.github.mavaze.weathermap.services.WeatherForecastService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/forecast")
public class WeatherForecastController implements WeatherForecastApi {

    @Autowired
    private WeatherForecastService weatherForecastService;

    @GetMapping("/query")
    public Mono<WeatherForecastResponseDTO> queryWeatherForecast(@RequestParam String city) {
        log.info("Weather forecast request received for city [{}]", city);
        final WeatherForecastResponseDTO forecastForCity = weatherForecastService.getForecastForCity(city);
        return Mono.just(forecastForCity);
    }
}
