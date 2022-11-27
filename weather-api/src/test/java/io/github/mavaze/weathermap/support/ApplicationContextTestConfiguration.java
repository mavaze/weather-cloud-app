package io.github.mavaze.weathermap.support;

import static java.time.Instant.now;
import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.google.common.collect.ImmutableMap;

@TestConfiguration
public class ApplicationContextTestConfiguration {

    @Bean
    public JwtDecoder jwtDecoder() {
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                final Map<String, Object> claims = new HashMap<>();
                claims.put("sub", "user");

                switch (token) {
                    case "read-user-token":
                        claims.put("scope", singletonList("read"));
                        break;
                    case "unauthorized-user-token":
                        claims.put("scope", singletonList("something-else"));
                        break;
                    default:
                        throw new JwtException("Invalid token");
                }
                return new Jwt(token, now(), now().plusSeconds(180),
                        ImmutableMap.of("alg", "none", "kid", UUID.randomUUID()),
                        claims);
            }
        };
    }
}
