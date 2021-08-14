package ru.gpn.etranintegration.service.etran;

import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;

import java.time.LocalDateTime;

public interface EtranService {

    InvoiceStatusResponse getInvoiceStatus(LocalDateTime currentDay);

    InvoiceResponse getInvoice(String invoiceIds);

}
