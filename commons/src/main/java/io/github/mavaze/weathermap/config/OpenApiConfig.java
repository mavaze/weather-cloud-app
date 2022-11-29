package io.github.mavaze.weathermap.config;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.OAUTH2;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

// @formatter:off
@SecurityScheme(name = "security_auth", type = OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(
        authorizationUrl = "${springdoc.oauth-flow.authorization-url}",
        tokenUrl = "${springdoc.oauth-flow.token-url}", scopes = {
                @OAuthScope(name = "read", description = "read scope"),
                @OAuthScope(name = "write", description = "write scope") })))
// @formatter:on
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI weatherAppOpenAPI(DiscoveryClient discoveryClient) {
        List<Server> servers = discoveryClient.getInstances("weather-api")
                .stream().map(ServiceInstance::getUri).map(URI::toString)
                .map(url -> new Server().url(url)).collect(toList());
        return new OpenAPI().info(new Info()
                .title("Weather API backed by OpenWeatherMap [mavaze]")
                .description("Weather prediction application")
                .version("v1.0.0")
                .license(new License().name("Apache 2.0")
                        .url("https://github.com/mavaze/weather-cloud-app/blob/develop/LICENSE")))
                .servers(servers)
                .externalDocs(new ExternalDocumentation()
                        .description("Weather prediction application")
                        .url("https://github.com/mavaze/weather-cloud-app/wiki"));
    }
}
