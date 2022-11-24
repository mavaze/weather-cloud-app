package io.github.mavaze.weathermap.support;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Slf4j
public class WireMockExtensions {

    public enum ServicePort {
        EXT_OPEN_WEATHER_SERVICE_PORT(23900); // same to be used in application-test.yml

        @Getter
        private int port;

        private ServicePort(int port) {
            this.port = port;
        }
    }

    public static WireMockExtension wiremockExtForService(ServicePort servicePort, Extension... extensions) {
        log.info("Assigned port {} for port name {}", servicePort.getPort(), servicePort);
        return WireMockExtension.newInstance().options(wireMockConfig()
                .port(servicePort.getPort())
                .notifier(new Slf4jNotifier(true))
                .extensions(extensions))
                .build();
    }

    public static WireMockExtension forOpenWeatherMapApi() {
        return wiremockExtForService(ServicePort.EXT_OPEN_WEATHER_SERVICE_PORT);
    }

}
