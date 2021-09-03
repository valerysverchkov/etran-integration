package ru.gpn.etranintegration.service.etran;

import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;

import java.time.LocalDateTime;

public interface EtranService {

    InvoiceStatusResponse getInvoiceStatus(LocalDateTime currentDay, String login, String password);

    InvoiceResponse getInvoice(String invoiceIds, String login, String password);

}
