package ru.gpn.etranintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EtranIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtranIntegrationApplication.class, args);
    }

}
