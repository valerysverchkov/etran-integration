package ru.gpn.etranintegration.model.etran;

import lombok.Data;

@Data
public class EtranAuthRequest {

    private String grantType;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;

    @Override
    public String toString() {
        return "grant_type=" + grantType +
                "&username=" + username +
                "&password=" + password +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;
    }

}
