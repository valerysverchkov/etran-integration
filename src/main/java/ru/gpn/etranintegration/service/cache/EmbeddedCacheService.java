package ru.gpn.etranintegration.service.cache;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmbeddedCacheService implements CacheService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @Override
    public String getLastOperDateByInvoiceId(String invoiceId) {
        return cache.get(invoiceId);
    }

    @Override
    public void setLastOperDateByInvoiceId(String invoiceId, String lastOperDate) {
        cache.put(invoiceId, lastOperDate);
    }

}
