package ru.gpn.etranintegration.service.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.gpn.etranintegration.config.EtranAuthConfig;
import ru.gpn.etranintegration.model.etran.auth.EtranAuthorization;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.Invoice;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.cache.CacheService;
import ru.gpn.etranintegration.service.esb.EsbAuthService;
import ru.gpn.etranintegration.service.etran.EtranService;
import ru.gpn.etranintegration.service.ibpd.IbpdService;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
class InvoiceProcess implements Process {

    private final EtranService etranService;
    private final IbpdService ibpdService;
    private final CacheService cacheService;
    private final EtranAuthConfig etranAuthConfigs;
    private final EsbAuthService esbAuthService;

    /**
     * This method processes actual invoices from ETRAN
     */
    @Override
    public void processing() {
        log.info("Start Invoice processing");
        final String token = esbAuthService.getToken();
        if (token == null) {
            log.info("End Invoice processing. Token for ESB not received.");
            return;
        }
        for (EtranAuthorization etranAuthorization : etranAuthConfigs.getCredential()) {
            if (!etranAuthorization.isValid()) {
                log.error("Login {} is bad. Process with this login has been terminated.", etranAuthorization.getLogin());
                continue;
            }
            log.info("Invoice process with ETRAN login: {}", etranAuthorization.getLogin());
            InvoiceStatusResponse invoiceStatusFromEtran = etranService.getInvoiceStatus(
                    LocalDateTime.now(),
                    etranAuthorization.getLogin(),
                    etranAuthorization.getPassword(),
                    token
            );
            if (invoiceStatusFromEtran == null || CollectionUtils.isEmpty(invoiceStatusFromEtran.getInvoice())) {
                log.info("End Invoice processing with login: {}. Invoice ids from ETRAN is null or empty.",
                        etranAuthorization.getLogin()
                );
                continue;
            }
            if (invoiceStatusFromEtran.isErrorAuth()) {
                log.error("Login {} is bad. Process with this login has been terminated.", etranAuthorization.getLogin());
                etranAuthorization.setValid(false);
                continue;
            }
            for (Invoice invoice : invoiceStatusFromEtran.getInvoice()) {
                log.info("Process by invoiceId: {}", invoice.getInvoiceId().getValue());
                if (needLoadInvoice(invoice)) {
                    boolean successLoad = loadInvoiceById(token, etranAuthorization, invoice.getInvoiceId().getValue());
                    if (successLoad) {
                        cacheService.setLastOperDateByInvoiceId(
                                invoice.getInvoiceId().getValue(),
                                invoice.getInvoiceLastOper().getValue()
                        );
                    }
                }
            }
            log.info("End Invoice processing with login: {}. ", etranAuthorization.getLogin());
        }
        log.info("End Invoice processing.");
    }

    /**
     * This method processes actual invoice from ETRAN by invoice number.
     *
     * @param invoiceId Invoice number for load invoice
     */
    @Override
    public void processingByInvoiceId(String invoiceId) {
        log.info("Start Invoice processing by id: {}", invoiceId);
        final String token = esbAuthService.getToken();
        if (token == null) {
            log.info("End Invoice processing. Token for ESB not received.");
            return;
        }
        for (EtranAuthorization etranAuthorization : etranAuthConfigs.getCredential()) {
            if (etranAuthorization.isValid()) {
                log.error("Login {} is bad. Process with this login has been terminated.", etranAuthorization.getLogin());
                continue;
            }
            loadInvoiceById(token, etranAuthorization, invoiceId);
        }
        log.info("End Invoice processing by id: {}", invoiceId);
    }

    /**
     * This method checks for last operation date invoice in CacheService and, based on received date,
     * decides whether to load invoice.
     *
     * @param invoice Object containing invoice number and last operation date for this invoice
     * @return Flag of need to update invoice with given number
     */
    private boolean needLoadInvoice(Invoice invoice) {
        String lastOperDateByInvoiceId = cacheService.getLastOperDateByInvoiceId(invoice.getInvoiceId().getValue());
        return lastOperDateByInvoiceId == null ||
                !lastOperDateByInvoiceId.equalsIgnoreCase(invoice.getInvoiceLastOper().getValue());
    }

    /**
     * @param token Jwt token for authorization in ESB service
     * @param etranAuthorization Authorization data for ETRAN service
     * @param invoiceId Invoice number for ETRAN
     * @return success of loading invoice into IBPD service
     */
    private boolean loadInvoiceById(String token, EtranAuthorization etranAuthorization, String invoiceId) {
        InvoiceResponse invoiceFromEtran = etranService.getInvoice(
                invoiceId,
                etranAuthorization.getLogin(),
                etranAuthorization.getPassword(),
                token
        );
        if (invoiceFromEtran.isErrorAuth()) {
            log.error("Login {} is bad. Process with this login has been terminated.", etranAuthorization.getLogin());
            etranAuthorization.setValid(false);
            return false;
        }
        LocalDateTime lastOperDateFromIbpd = ibpdService.getLastOperDateByInvoiceId(invoiceFromEtran.getInvoiceId());
        if (lastOperDateFromIbpd == null) {
            log.info("Set new invoice in IBPD with id: {}", invoiceId);
            ibpdService.setNewInvoice(invoiceFromEtran);
            return true;
        }
        if (lastOperDateFromIbpd.isBefore(invoiceFromEtran.getLastOperDate())){
            log.info("Update invoice in IBPD with id: {}", invoiceId);
            ibpdService.updateInvoice(invoiceFromEtran);
        }
        return true;
    }

}
