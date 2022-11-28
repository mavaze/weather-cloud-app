package io.github.mavaze.weathermap.config;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.OAUTH2;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

// @formatter:off
@OpenAPIDefinition(info = @Info(title = "Weather API backed by OpenWeatherMap", 
        description = "Weather prediction application", version = "v1.0.0", license = @License(
                name = "Apache 2.0", url = "https://github.com/mavaze/weather-cloud-app/blob/develop/LICENSE")), 
        servers = {@Server(url = "http://weather-cloud-api:9090")},
        externalDocs = @ExternalDocumentation(description = "Weather prediction application",
                url = "https://github.com/mavaze/weather-cloud-app/wiki"))        
@SecurityScheme(name = "security_auth", type = OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(
                authorizationUrl = "http://weather-cloud-security:9000/oauth2/authorize",
                tokenUrl = "http://weather-cloud-security:9000/oauth2/token", scopes = {
                        @OAuthScope(name = "read", description = "read scope"),
                        @OAuthScope(name = "write", description = "write scope") })))
// @formatter:on
public class OpenApiConfig {

        // @Bean
        // public OpenAPI weatherAppOpenAPI() {
        //    return new OpenAPI().info(new Info()
        //         .title("Weather API backed by OpenWeatherMap")
        //         .description("Weather prediction application")
        //         .version("v1.0.0")
        //         .license(new License().name("Apache 2.0")
        //                 .url("https://github.com/mavaze/weather-cloud-app/blob/develop/LICENSE")))
        //         .servers(Arrays.asList(new Server().url("http://weather-cloud-api:9090")))
        //         .externalDocs(new ExternalDocumentation()
        //                 .description("Weather prediction application")
        //                 .url("https://github.com/mavaze/weather-cloud-app/wiki"));
        // }
}
