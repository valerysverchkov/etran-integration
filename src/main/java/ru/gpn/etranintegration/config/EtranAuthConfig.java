package ru.gpn.etranintegration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@ConfigurationProperties("service.etran.auth")
@Data
@Configuration
public class EtranAuthConfig {

    List<EtranAuthorization> etranAuthorizations;

}
