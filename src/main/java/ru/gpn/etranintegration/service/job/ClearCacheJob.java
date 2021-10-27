package ru.gpn.etranintegration.service.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gpn.etranintegration.service.cache.CacheService;

@Component
@Slf4j
@RequiredArgsConstructor
class ClearCacheJob implements Job {

    private final CacheService cacheService;

    @Scheduled(cron = "${scheduled.cron.clearCache}")
    @Override
    public void schedule() {
        log.info("Start clear cache");
        try {
            cacheService.clearAll();
        } catch (Exception e) {
            log.error("Error clear cache", e);
            return;
        }
        log.info("End clear cache");
    }

}
