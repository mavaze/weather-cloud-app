package io.github.mavaze.weathermap.contract;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.mavaze.weathermap.dtos.ErrorResponseDTO;
import io.github.mavaze.weathermap.dtos.WeatherForecastResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient("weather-api")
@SecurityRequirement(name = "security_auth", scopes = { "read", "write" })
public interface WeatherForecastApi {

    @Operation(summary = "Query for 3 days of weather forecast")
    @ApiResponse(responseCode = "200", description = "Success", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherForecastResponseDTO.class)) })
    @ApiResponse(responseCode = "400", description = "Invalid request parameter", content = @Content)
    @ApiResponse(responseCode = "401", description = "User must be logged in first", content = @Content)
    @ApiResponse(responseCode = "403", description = "Not authorized to perform this query", content = @Content)
    @ApiResponse(responseCode = "404", description = "Result not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "External service may not be reachable", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)) })
    @GetMapping("/query")
    Mono<WeatherForecastResponseDTO> queryWeatherForecast(@RequestParam("city") String city);
}
