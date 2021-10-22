package ru.gpn.etranintegration.service.cache;

public interface CacheService {

    String getLastOperDateByInvoiceId(String invoiceId);

    void setLastOperDateByInvoiceId(String invoiceId, String lastOperDate);
}
