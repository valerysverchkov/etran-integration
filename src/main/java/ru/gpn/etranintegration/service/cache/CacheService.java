package ru.gpn.etranintegration.service.cache;

import java.util.List;

public interface CacheService {

    List<String> getInvoiceIds();

    void reloadInvoiceIds(List<String> invoiceIds);
}
