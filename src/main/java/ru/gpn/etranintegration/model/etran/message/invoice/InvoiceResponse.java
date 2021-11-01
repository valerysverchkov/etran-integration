package ru.gpn.etranintegration.model.etran.message.invoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvoiceResponse {

    private String invoiceId;

    private String invoiceNum;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime lastOperDate;

    private String message;

    private boolean errorAuth;

}
