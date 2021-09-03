package ru.gpn.etranintegration.service.etran;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gpn.etranintegration.client.EtranClient;
import ru.gpn.etranintegration.model.etran.*;
import ru.gpn.etranintegration.service.esb.EsbAuthService;
import ru.gpn.etranintegration.service.util.DateUtils;

import java.time.LocalDateTime;

/**
 * This service is responsible for calling ETRAN service via ESB service.
 */
@Service
@RequiredArgsConstructor
class EtranServiceImpl implements EtranService {

    private final EsbAuthService esbAuthService;
    private final EtranClient etranClient;

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @return Numbers of invoices for the current day
     */
    @Override
    public InvoiceStatusResponse getInvoiceStatus(LocalDateTime currentDay, String login, String password) {
        String token = esbAuthService.getToken();
        return etranClient.getInvoiceStatus(prepareInvoiceStatusRequest(currentDay), token);
    }

    /**
     * @param invoiceId Invoice number
     * @return Full data on the invoice
     */
    @Override
    public InvoiceResponse getInvoice(String invoiceId, String login, String password) {
        String token = esbAuthService.getToken();
        return etranClient.getInvoice(prepareInvoiceRequest(invoiceId), token);
    }

    /**
     * @param invoiceId Invoice number
     * @return Filled request for invoice
     */
    private InvoiceRequest prepareInvoiceRequest(String invoiceId) {
        InvoiceRequest invoiceRequest = new InvoiceRequest();
        invoiceRequest.setInvNumber(invoiceId);
        return invoiceRequest;
    }

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @return Filled request for invoice status
     */
    private InvoiceStatusRequest prepareInvoiceStatusRequest(LocalDateTime currentDay) {
        InvoiceStatusRequest invoiceStatusRequest = new InvoiceStatusRequest();
        LocalDateTime fromDate = currentDay.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime toDate = currentDay.withHour(23).withMinute(59).withSecond(59);
        invoiceStatusRequest.setFromDate(DateUtils.convertToValueAttribute(fromDate));
        invoiceStatusRequest.setToDate(DateUtils.convertToValueAttribute(toDate));
        return invoiceStatusRequest;
    }

}
