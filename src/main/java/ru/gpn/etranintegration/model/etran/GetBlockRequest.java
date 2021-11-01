package ru.gpn.etranintegration.model.etran;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetBlockRequest {

    @JsonProperty("Login")
    private String login;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("Text")
    private Object message;

}
