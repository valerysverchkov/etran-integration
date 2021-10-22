package ru.gpn.etranintegration.model.etran.auth;

import lombok.Data;

@Data
public class EtranAuthorization {

    private String login;
    private String password;
    private boolean valid;

}
