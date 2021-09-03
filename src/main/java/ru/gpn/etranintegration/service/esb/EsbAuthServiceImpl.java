package ru.gpn.etranintegration.service.esb;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gpn.etranintegration.model.esb.EsbAuthRequest;

@RequiredArgsConstructor
@Service
class EsbAuthServiceImpl implements EsbAuthService {

    private final RestTemplate templateEtranAuth;

    @Value("${service.esb.auth.uri}")
    private String uri;

    @Value("${service.esb.auth.grantType}")
    private String grantType;

    @Value("${service.esb.auth.username}")
    private String username;

    @Value("${service.esb.auth.password}")
    private String password;

    @Value("${service.esb.auth.clientId}")
    private String clientId;

    @Value("${service.esb.auth.clientSecret}")
    private String clientSecret;

    @Override
    public String getToken() {
        ResponseEntity<String> responseEntityToken = templateEtranAuth.exchange(uri, HttpMethod.POST,
                prepareEsbAuthRequest(), String.class);
        //TODO: add error handler
        return responseEntityToken.getBody();
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
