package ru.gpn.etranintegration.service.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gpn.etranintegration.config.EtranAuthConfig;
import ru.gpn.etranintegration.config.EtranAuthorization;
import ru.gpn.etranintegration.model.etran.Invoice;
import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;
import ru.gpn.etranintegration.model.etran.ValueAttribute;
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
    private final EtranAuthConfig etranAuthConfigs;

    /**
     * This method processes actual invoices from ETRAN
     */
    @Override
    public void processing() {
        log.info("Start Invoice processing");
        for (EtranAuthorization etranAuthorization : etranAuthConfigs.getEtranAuthorizations()) {
            log.info("Invoice process with etran login: {}", etranAuthorization.getLogin());
            InvoiceStatusResponse invoiceStatusFromEtran = etranService.getInvoiceStatus(
                    LocalDateTime.now(),
                    etranAuthorization.getLogin(),
                    etranAuthorization.getPassword()
            );
            if (CollectionUtils.isEmpty(invoiceStatusFromEtran.getInvoice())) {
                log.info("End Invoice processing. Invoice ids from ETRAN is empty.");
                return;
            }
            List<String> invoiceIds = getInvoiceIds(invoiceStatusFromEtran);
            log.info("Invoice ids from ETRAN: {}", invoiceIds);
            for (String invoiceId : invoiceIds) {
                InvoiceResponse invoiceFromEtran = etranService.getInvoice(
                        invoiceId,
                        etranAuthorization.getLogin(),
                        etranAuthorization.getPassword()
                );
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
            //cacheService.reloadInvoiceIds(invoiceStatusFromEtran.get());
            log.info("End Invoice processing");
        }
    }

    /**
     * @param invoiceStatusFromEtran Response from ETRAN with invoice identifiers
     * @return Collected list of invoice identifiers
     */
    private static List<String> getInvoiceIds(InvoiceStatusResponse invoiceStatusFromEtran) {
        return invoiceStatusFromEtran.getInvoice()
                .stream()
                .map(Invoice::getInvoiceId)
                .map(ValueAttribute::getValue)
                .collect(Collectors.toList());
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
