package io.github.mavaze.weathermap.dtos;

import java.util.Arrays;

import javax.naming.InvalidNameException;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WeatherAdvice {

    HIGH_TEMP("Use sunscreen lotion"),
    RAIN("Carry umbrella"),
    HIGH_WINDS("It’s too windy, watch out!"),
    THUNDERSTORM("Don’t step out! A Storm is brewing!");

    private final String message;

    private WeatherAdvice(String message) {
        this.message = message;
    }

    @JsonValue
    public String getMessage() {
        return this.message;
    }

    static WeatherAdvice forValues(String type) throws InvalidNameException {
        return Arrays.stream(WeatherAdvice.values())
                .filter(o -> o.name().equals(type))
                .findFirst()
                .orElseThrow(InvalidNameException::new);
    }
}
