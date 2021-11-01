package ru.gpn.etranintegration.model.etran.message.invoiceStatus;

import lombok.Data;

@Data
public class Invoice {

    private String invoiceId;

    private String invNumber;

    private String invoiceLastOper;

}
