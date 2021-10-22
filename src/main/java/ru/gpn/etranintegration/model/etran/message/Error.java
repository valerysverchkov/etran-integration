package ru.gpn.etranintegration.model.etran.message;

import lombok.Data;

@Data
public class Error {

    private ValueAttribute errorCode;

    private ValueAttribute errorMessage;

    private ValueAttribute errorStatusCode;

}
