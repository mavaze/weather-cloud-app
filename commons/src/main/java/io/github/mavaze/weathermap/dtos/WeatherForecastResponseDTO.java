package io.github.mavaze.weathermap.dtos;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherForecastResponseDTO {

    private City city;
    private Map<String, WeatherForecast> days;

    @Data
    @Builder
    public static class City {
        private String name;
        private int offset;
    }

    @Data
    @Builder
    public static class WeatherForecast {
        private float minTemp;
        private float maxTemp;
        private List<WeatherAdvice> advices;
    }
}
