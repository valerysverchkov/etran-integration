package ru.gpn.etranintegration.service.process;

public interface Process {

    void processing();

    void processingByInvoiceId(String invoiceId);

}
