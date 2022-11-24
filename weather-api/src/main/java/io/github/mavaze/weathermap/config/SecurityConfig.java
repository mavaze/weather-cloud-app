package io.github.mavaze.weathermap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .authorizeRequests()
                .mvcMatchers("/v3/api-docs/**").permitAll()
                .mvcMatchers("/api/forecast/**").access("hasAuthority('SCOPE_read')")
                .anyRequest().authenticated().and()
                .oauth2ResourceServer().jwt();
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        // corsConfig.addAllowedOrigin("http://nodeagent:8080");
        // corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "OPTION", "HEAD"));
        // corsConfig.addAllowedHeader("x-requested-with");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

}
