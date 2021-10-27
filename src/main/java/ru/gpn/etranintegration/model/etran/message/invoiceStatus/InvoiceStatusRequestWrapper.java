package ru.gpn.etranintegration.model.etran.message.invoiceStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InvoiceStatusRequestWrapper {

    @JsonProperty("invoiceStatus")
    private InvoiceStatusRequest invoiceStatusRequest;

}
