package ru.gpn.etranintegration.model.etran.message.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InvoiceRequestWrapper {

    @JsonProperty("getInvoice")
    private InvoiceRequest invoiceRequest;

}
