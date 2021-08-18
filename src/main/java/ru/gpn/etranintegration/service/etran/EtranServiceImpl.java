package ru.gpn.etranintegration.service.etran;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gpn.etranintegration.client.EtranClient;
import ru.gpn.etranintegration.model.etran.InvoiceRequest;
import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusRequest;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.etran.auth.EtranAuthService;

import java.time.LocalDateTime;

/**
 * This service is responsible for calling ETRAN service via ESB service.
 */
@Service
@RequiredArgsConstructor
class EtranServiceImpl implements EtranService {

    private final EtranAuthService etranAuthService;
    private final EtranClient etranClient;

    @Value("${service.etran.login}")
    private char[] login;

    @Value("${service.etran.password}")
    private char[] password;

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @return Numbers of invoices for the current day
     */
    @Override
    public InvoiceStatusResponse getInvoiceStatus(LocalDateTime currentDay) {
        String token = etranAuthService.getToken();
        return etranClient.getInvoiceStatus(prepareInvoiceStatusRequest(currentDay), token);
    }

    /**
     * @param invoiceId Invoice number
     * @return Full data on the invoice
     */
    @Override
    public InvoiceResponse getInvoice(String invoiceId) {
        String token = etranAuthService.getToken();
        return etranClient.getInvoice(prepareInvoiceRequest(invoiceId), token);
    }

    /**
     * @param invoiceId Invoice number
     * @return Filled request for invoice
     */
    private InvoiceRequest prepareInvoiceRequest(String invoiceId) {
        InvoiceRequest invoiceRequest = new InvoiceRequest();
        invoiceRequest.setLogin(String.valueOf(login));
        invoiceRequest.setPassword(String.valueOf(password));
        invoiceRequest.setInvNumber(invoiceId);
        return invoiceRequest;
    }

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @return Filled request for invoice status
     */
    private InvoiceStatusRequest prepareInvoiceStatusRequest(LocalDateTime currentDay) {
        InvoiceStatusRequest invoiceStatusRequest = new InvoiceStatusRequest();
        invoiceStatusRequest.setLogin(String.valueOf(login));
        invoiceStatusRequest.setPassword(String.valueOf(password));
        LocalDateTime fromDate = currentDay.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime toDate = currentDay.withHour(23).withMinute(59).withSecond(59);
        invoiceStatusRequest.setFromDate(fromDate);
        invoiceStatusRequest.setToDate(toDate);
        return invoiceStatusRequest;
    }

}
