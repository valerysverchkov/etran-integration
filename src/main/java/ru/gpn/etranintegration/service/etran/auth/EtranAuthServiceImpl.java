package ru.gpn.etranintegration.service.etran.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gpn.etranintegration.model.etran.EtranAuthRequest;

@RequiredArgsConstructor
@Service
class EtranAuthServiceImpl implements EtranAuthService {

    private final RestTemplate templateEtranAuth;

    @Value("${service.etran.auth.uri}")
    private String uri;

    @Value("${service.etran.auth.grantType}")
    private String grantType;

    @Value("${service.etran.auth.username}")
    private String username;

    @Value("${service.etran.auth.password}")
    private String password;

    @Value("${service.etran.auth.clientId}")
    private String clientId;

    @Value("${service.etran.auth.clientSecret}")
    private String clientSecret;

    @Override
    public String getToken() {
        ResponseEntity<String> responseEntityToken = templateEtranAuth.exchange(uri, HttpMethod.POST,
                prepareEtranAuthRequest(), String.class);
        //TODO: add error handler
        return responseEntityToken.getBody();
    }

    private HttpEntity<String> prepareEtranAuthRequest() {
        EtranAuthRequest etranAuthRequest = new EtranAuthRequest();
        etranAuthRequest.setGrantType(grantType);
        etranAuthRequest.setUsername(username);
        etranAuthRequest.setPassword(password);
        etranAuthRequest.setClientId(clientId);
        etranAuthRequest.setClientSecret(clientSecret);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return new HttpEntity<>(etranAuthRequest.toString(), httpHeaders);
    }

}
