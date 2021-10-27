package ru.gpn.etranintegration.service.etran;

import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;

import java.time.LocalDateTime;

public interface EtranService {

    InvoiceStatusResponse getInvoiceStatus(LocalDateTime currentDay, String login, String password, String token);

    InvoiceResponse getInvoice(String invoiceIds, String login, String password, String token);

}
