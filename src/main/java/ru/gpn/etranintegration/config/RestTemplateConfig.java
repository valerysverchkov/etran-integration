package ru.gpn.etranintegration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
class RestTemplateConfig {

    @Value("${service.esb.timeOut}")
    private int esbAuthTimeOut;
    @Value("${service.ibpd.timeOut}")
    private int ibpdTimeOut;

    @Bean
    public RestTemplate restEsbAuth() {
        return new RestTemplateBuilder()
                .setReadTimeout(Duration.ofSeconds(esbAuthTimeOut))
                .setConnectTimeout(Duration.ofSeconds(esbAuthTimeOut))
                .build();
    }

    @Bean
    public RestTemplate restIbpd() {
        return new RestTemplateBuilder()
                .setReadTimeout(Duration.ofSeconds(ibpdTimeOut))
                .setConnectTimeout(Duration.ofSeconds(ibpdTimeOut))
                .build();
    }

}
