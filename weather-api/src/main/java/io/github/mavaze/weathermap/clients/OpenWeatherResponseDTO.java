package io.github.mavaze.weathermap.clients;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Data;

@Data
public class OpenWeatherResponseDTO {

    private String cod;

    private City city;

    private List<QuarterlyForecast> list;

    @Data
    public static class City {
        String name;
        int timezone;
    }

    @Data
    public static class QuarterlyForecast {

        private long dt;

        private Wind wind;

        private Summary main;

        private List<Weather> weather;

        @Data
        public static class Weather {
            private Condition main;
        }

        @Data
        public static class Summary {
            @JsonProperty("temp_min")
            private float tempMin;

            @JsonProperty("temp_max")
            private float tempMax;
        }

        @Data
        public static class Wind {
            private float speed;
        }

        public static enum Condition {

            THUNDERSTORM("Thunderstorm"),
            DRIZZLE("Drizzle"),
            RAIN("Rain"),
            SNOW("Snow"),
            ATMOSPHERE("Atmosphere"),
            CLEAR("Clear"),
            CLOUDS("Clouds");

            private String name;

            private Condition(String name) {
                this.name = name;
            }

            @JsonValue
            public String getName() {
                return this.name;
            }

            @JsonCreator
            public static Condition forValues(final String name) {
                return Arrays.stream(values())
                        .filter(type -> type.getName().equals(name))
                        .findFirst()
                        .orElse(ATMOSPHERE);
            }
        }
    }
}
