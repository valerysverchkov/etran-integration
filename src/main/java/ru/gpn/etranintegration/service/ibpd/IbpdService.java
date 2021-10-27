package ru.gpn.etranintegration.service.ibpd;

import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;

import java.time.LocalDateTime;

public interface IbpdService {

    LocalDateTime getLastOperDateByInvoiceId(String invNumber);

    void setNewInvoice(InvoiceResponse invoiceFromEtran);

    void updateInvoice(InvoiceResponse invoiceFromEtran);

}
