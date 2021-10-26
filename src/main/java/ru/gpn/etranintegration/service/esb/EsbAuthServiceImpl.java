package ru.gpn.etranintegration.service.esb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gpn.etranintegration.model.esb.EsbAuthRequest;

@Slf4j
@Service
class EsbAuthServiceImpl implements EsbAuthService {

    @Value("${service.esb.auth.uri}")
    private String uri;

    @Value("${service.esb.auth.requestCnt}")
    private int requestCnt;

    @Value("${service.esb.auth.cred.grantType}")
    private String grantType;

    @Value("${service.esb.auth.cred.username}")
    private String username;

    @Value("${service.esb.auth.cred.password}")
    private String password;

    @Value("${service.esb.auth.cred.clientId}")
    private String clientId;

    @Value("${service.esb.auth.cred.clientSecret}")
    private String clientSecret;

    private final RestTemplate restEsbAuth;

    public EsbAuthServiceImpl(@Qualifier("restEsbAuth") RestTemplate restEsbAuth) {
        this.restEsbAuth = restEsbAuth;
    }

    @Override
    public String getToken() {
        ResponseEntity<String> responseEntityToken;
        HttpEntity<String> requestHttpEntity = prepareEsbAuthRequest();
        log.info("Request to ESB Auth service: {}", requestHttpEntity);
        for (int i = 0; i < requestCnt; i++) {
            try {
                responseEntityToken = restEsbAuth.exchange(uri, HttpMethod.POST, requestHttpEntity, String.class);
            } catch (RestClientException e) {
                log.error("ESB Auth service received error.", e);
                continue;
            }
            if (responseEntityToken.getStatusCode().is2xxSuccessful() && responseEntityToken.getBody() != null) {
                log.info("Response from ESB Auth service: {}", responseEntityToken);
                return responseEntityToken.getBody();
            } else {
                log.error("Generate token for etran error. Response: {}", responseEntityToken);
            }
        }
        return null;
    }

    private HttpEntity<String> prepareEsbAuthRequest() {
        EsbAuthRequest esbAuthRequest = new EsbAuthRequest();
        esbAuthRequest.setGrantType(grantType);
        esbAuthRequest.setUsername(username);
        esbAuthRequest.setPassword(password);
        esbAuthRequest.setClientId(clientId);
        esbAuthRequest.setClientSecret(clientSecret);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return new HttpEntity<>(esbAuthRequest.toString(), httpHeaders);
    }

}
