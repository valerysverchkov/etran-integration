package ru.gpn.etranintegration.service.cache;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
class CacheServiceImpl implements CacheService {

    @Override
    public List<String> getInvoiceIds() {
        return null;
    }

    @Override
    public void reloadInvoiceIds(List<String> invoiceIds) {
    }

}
