package ru.gpn.etranintegration.service.ibpd;

import ru.gpn.etranintegration.model.etran.message.InvoiceResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface IbpdService {

    LocalDateTime getLastOperDateByInvoiceId(String invNumber);

    void setNewInvoice(InvoiceResponse invoiceFromEtran);

    void updateInvoice(InvoiceResponse invoiceFromEtran);
}
