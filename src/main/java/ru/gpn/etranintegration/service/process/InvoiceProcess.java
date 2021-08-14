package ru.gpn.etranintegration.service.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.cache.CacheService;
import ru.gpn.etranintegration.service.etran.EtranService;
import ru.gpn.etranintegration.service.ibpd.IbpdService;
import ru.gpn.etranintegration.service.util.CollectionUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
class InvoiceProcess implements Process {

    private final EtranService etranService;
    private final IbpdService ibpdService;
    private final CacheService cacheService;

    /**
     * This method processes actual invoices from ETRAN
     */
    @Override
    public void processing() {
        log.info("Start Invoice processing");
        InvoiceStatusResponse invoiceStatusFromEtran = etranService.getInvoiceStatus(LocalDateTime.now());
        if (CollectionUtils.isEmpty(invoiceStatusFromEtran.getInvoiceIds())) {
            log.info("End Invoice processing. Invoice ids from ETRAN is empty.");
            return;
        }
        //List<String> invoiceIdsFromIbpd = ibpdService.getInvoiceIds();
        //TODO: remove getUnique and request by each invoice id
        //List<String> uniqueInvoiceIds = getUniqueInvoiceIds(invoiceStatusFromEtran.getInvoiceIds(), invoiceIdsFromIbpd);
        log.debug("Invoice ids from ETRAN: {}", invoiceStatusFromEtran.getInvoiceIds());
        //TODO: can be replaced with multithreaded?
        for (String invoiceId : invoiceStatusFromEtran.getInvoiceIds()) {
            InvoiceResponse invoiceFromEtran = etranService.getInvoice(invoiceId);
            LocalDateTime lastOperDateFromIbpd = ibpdService.getLastOperDateByInvoiceId(invoiceFromEtran.getInvNumber());
            if (lastOperDateFromIbpd == null) {
                log.debug("Set new invoice in IBPD with id: {}", invoiceFromEtran.getInvNumber());
                ibpdService.setNewInvoice(invoiceFromEtran);
                continue;
            }
            if (lastOperDateFromIbpd.isBefore(invoiceFromEtran.getLastOperDate())){
                log.debug("Update invoice in IBPD with id: {}", invoiceFromEtran.getInvNumber());
                ibpdService.updateInvoice(invoiceFromEtran);
            }
        }
        //second operation, because reload new unique slice should occur after receiving invoices
        cacheService.reloadInvoiceIds(invoiceStatusFromEtran.getInvoiceIds());
        log.info("End Invoice processing");
    }


    /**
     * @param invoiceIdsFromEtran Invoice numbers that were returned by IBPD
     * @param invoiceIdsFromIbpd Invoice numbers that were returned by IBPD
     * @return List of unique invoice numbers that have appeared in ETRAN from a previous request
     */
    private static List<String> getUniqueInvoiceIds(List<String> invoiceIdsFromEtran, List<String> invoiceIdsFromIbpd) {
        return invoiceIdsFromEtran.stream()
                .filter(invoiceId -> !invoiceIdsFromIbpd.contains(invoiceId))
                .collect(Collectors.toList());
    }

}
