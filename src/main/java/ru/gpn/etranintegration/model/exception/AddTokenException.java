package ru.gpn.etranintegration.model.exception;

import org.springframework.ws.client.WebServiceClientException;

public class AddTokenException extends WebServiceClientException {
    public AddTokenException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
