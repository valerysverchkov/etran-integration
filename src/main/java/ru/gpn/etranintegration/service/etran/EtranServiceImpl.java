package ru.gpn.etranintegration.service.etran;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.GetBlockResponse;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.converter.EtranConverter;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;

/**
 * This service is responsible for calling ETRAN service via ESB service.
 */
@Service
@Slf4j
class EtranServiceImpl implements EtranService {

    @Value("${service.esb.etran.cntRequest}")
    private int requestCnt;

    @Value("${service.esb.etran.uri.invoiceStatus}")
    private String invoiceStatusUrl;

    @Value("${service.esb.etran.uri.invoice}")
    private String invoiceUrl;

    private final RestTemplate restEsbEtran;
    private final EtranConverter etranConverter;

    public EtranServiceImpl(@Qualifier("restEsbEtran") RestTemplate restEsbEtran,
                            EtranConverter etranConverter) {
        this.restEsbEtran = restEsbEtran;
        this.etranConverter = etranConverter;
    }

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @return Numbers of invoices for the current day
     */
    @Override
    public InvoiceStatusResponse getInvoiceStatus(LocalDateTime currentDay, String login, String password, String token) {
        HttpEntity<GetBlockRequest> blockRequestEntity = prepareInvoiceStatusRequest(currentDay, login, password, token);
        ResponseEntity<GetBlockResponse> responseEntity;
        for (int i = 0; i < requestCnt; i++) {
            try {
                responseEntity = restEsbEtran.exchange(
                        invoiceStatusUrl,
                        HttpMethod.POST,
                        blockRequestEntity,
                        GetBlockResponse.class
                );
            } catch (RestClientException e) {
                if (e.getCause() instanceof SocketTimeoutException) continue;
                log.error("Invoice status request to ETRAN error.", e);
                return null;
            }

            log.info("Response from ETRAN received: {}", responseEntity);

            if (isNotNeedRepeatRequest(responseEntity)) {
                log.error("ESB returned http status code: {}", responseEntity.getStatusCode());
                return null;
            }

            if (responseEntity.getBody() == null || responseEntity.getBody().getMessage() == null) {
                log.error("Invoice status response is null");
                continue;
            }

            return etranConverter.convertToInvoiceStatusResponse(responseEntity);
        }
        return null;
    }

    /**
     * @param invoiceId Invoice number
     * @return Full data on the invoice
     */
    @Override
    public InvoiceResponse getInvoice(String invoiceId, String invoiceNum, String login, String password, String token) {
        HttpEntity<GetBlockRequest> blockRequestEntity = prepareInvoiceRequest(invoiceId, invoiceNum, login, password, token);

        ResponseEntity<GetBlockResponse> responseEntity;
        for (int i = 0; i < requestCnt; i++) {
            try {
                responseEntity = restEsbEtran.exchange(
                        invoiceUrl,
                        HttpMethod.POST,
                        blockRequestEntity,
                        GetBlockResponse.class
                );
            } catch (RestClientException e) {
                if (e.getCause() instanceof SocketTimeoutException) continue;
                log.error("Invoice request to ETRAN error.", e);
                return null;
            }

            log.info("Response from ETRAN received: {}", responseEntity);

            if (isNotNeedRepeatRequest(responseEntity)) {
                log.error("ESB returned http status code: {}", responseEntity.getStatusCode());
                return null;
            }

            if (responseEntity.getBody() == null || responseEntity.getBody().getMessage() == null) {
                log.error("Invoice response is null");
                continue;
            }
            return etranConverter.convertToInvoiceResponse(responseEntity);
        }
        return null;
    }

    /**
     * @param invoiceId Invoice id
     * @param invoiceNum Invoice number
     * @param login Login for authentication in ETRAN
     * @param password Password for authentication in ETRAN
     * @return Filled request for invoice
     */
    private HttpEntity<GetBlockRequest> prepareInvoiceRequest(String invoiceId, String invoiceNum, String login,
                                                  String password, String token) {
        GetBlockRequest invoiceRequest = etranConverter.convertToInvoiceRequest(invoiceId, invoiceNum, login, password);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(token);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        return new HttpEntity<>(invoiceRequest, httpHeaders);
    }

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @param login Login for authentication in ETRAN
     * @param password Password for authentication in ETRAN
     * @return Filled request for invoice status
     */
    private HttpEntity<GetBlockRequest> prepareInvoiceStatusRequest(LocalDateTime currentDay, String login,
                                                        String password, String token) {

        GetBlockRequest invoiceStatusRequest = etranConverter.convertToInvoiceStatusRequest(currentDay, login, password);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(token);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        return new HttpEntity<>(invoiceStatusRequest, httpHeaders);
    }

    private static boolean isNotNeedRepeatRequest(ResponseEntity<GetBlockResponse> responseEntity) {
        return !responseEntity.getStatusCode().is2xxSuccessful()
                && (responseEntity.getStatusCodeValue() == 400
                || responseEntity.getStatusCodeValue() == 404);
    }

}
