package ru.gpn.etranintegration.model.etran;

import lombok.Data;
import java.util.List;

@Data
public class InvoiceStatusResponse {

    private List<String> invoiceIds;

}
