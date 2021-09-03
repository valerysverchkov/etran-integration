package ru.gpn.etranintegration.service.cache;

import lombok.RequiredArgsConstructor;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
class CacheServiceImpl implements CacheService {

    private final RemoteCacheManager remoteCacheManager;

    @Value("${infinispan.remote.cacheName}")
    private String cacheName;

    @Override
    public List<String> getInvoiceIds() {
        getEtranIntCache();
        return null;
    }

    private RemoteCache<Object, Object> getEtranIntCache() {
        return remoteCacheManager.getCache(cacheName);
    }

    @Override
    public void reloadInvoiceIds(List<String> invoiceIds) {
    }

}
