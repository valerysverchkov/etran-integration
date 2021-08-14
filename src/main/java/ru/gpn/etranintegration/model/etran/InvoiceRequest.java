package ru.gpn.etranintegration.model.etran;

import lombok.Data;

@Data
public class InvoiceRequest {

    private String login;
    private String password;
    private String invNumber;
    private Boolean useValid = Boolean.TRUE;

}
