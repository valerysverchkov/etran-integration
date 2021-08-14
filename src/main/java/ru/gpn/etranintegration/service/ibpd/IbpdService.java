package ru.gpn.etranintegration.service.ibpd;

import ru.gpn.etranintegration.model.etran.InvoiceResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface IbpdService {

    List<String> getInvoiceIds();

    LocalDateTime getLastOperDateByInvoiceId(String invNumber);

    void setNewInvoice(InvoiceResponse invoiceFromEtran);

    void updateInvoice(InvoiceResponse invoiceFromEtran);
}
