package ru.gpn.etranintegration.service.etran;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.InputSource;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.GetBlockResponse;
import ru.gpn.etranintegration.model.etran.message.Error;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceRequest;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceRequestWrapper;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusRequest;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusRequestWrapper;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.util.DateUtils;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * This service is responsible for calling ETRAN service via ESB service.
 */
@Service
@Slf4j
class EtranServiceImpl implements EtranService {

    private static final String TOKEN_HEADER = "Bearer ";
    private static final String ERROR_TAG = "<error version=\"1.0\">";
    private static final String AUTH_ERROR = "401";
    private static final Set<String> NOT_REPEAT_ERROR = Set.of("1", "2", "3", "4", "100", AUTH_ERROR, "406");

    @Value("${service.esb.etran.cntRequest}")
    private int requestCnt;

    @Value("${service.esb.etran.uri.invoiceStatus}")
    private String invoiceStatusUrl;

    @Value("${service.esb.etran.uri.invoice}")
    private String invoiceUrl;

    private final RestTemplate restEsbEtran;
    private final XmlMapper xmlMapper;
    private final XPath xPath;

    public EtranServiceImpl(@Qualifier("restEsbEtran") RestTemplate restEsbEtran,
                            XmlMapper xmlMapper,
                            XPath xPath) {
        this.restEsbEtran = restEsbEtran;
        this.xmlMapper = xmlMapper;
        this.xPath = xPath;
    }

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @return Numbers of invoices for the current day
     */
    @Override
    public InvoiceStatusResponse getInvoiceStatus(LocalDateTime currentDay, String login, String password, String token) {
        HttpEntity<GetBlockRequest> blockRequestEntity;
        try {
            blockRequestEntity = prepareInvoiceStatusRequest(currentDay, login, password, token);
        } catch (JsonProcessingException e) {
            log.error("Invoice status request build error.", e);
            return null;
        }
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

            try {
                String message = responseEntity.getBody().getMessage();
                if (message.contains(ERROR_TAG)) {
                    Error error = xmlMapper.readValue(message, Error.class);
                    if (isNotNeedRepeatRequest(error)) {
                        log.error(
                                "Etran returned an error in witch retrying request would not yield any result. Response: {}",
                                message
                        );
                        return getErrorInvoiceStatusResponse(error);
                    }
                } else {
                    return xmlMapper.readValue(message, InvoiceStatusResponse.class);
                }
            } catch (Exception e) {
                log.error("Convert invoice status response error.", e);
                return null;
            }
        }
        return null;
    }

    /**
     * @param invoiceId Invoice number
     * @return Full data on the invoice
     */
    @Override
    public InvoiceResponse getInvoice(String invoiceId, String login, String password, String token) {
        HttpEntity<GetBlockRequest> blockRequestEntity;
        try {
            blockRequestEntity = prepareInvoiceRequest(invoiceId, login, password, token);
        } catch (JsonProcessingException e) {
            log.error("Invoice status request build error.", e);
            return null;
        }

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

            try {
                String message = responseEntity.getBody().getMessage();
                if (message.contains(ERROR_TAG)) {
                    Error error = xmlMapper.readValue(message, Error.class);
                    if (isNotNeedRepeatRequest(error)) {
                        log.error(
                                "Etran returned an error in witch retrying request would not yield any result. Response: {}",
                                message
                        );
                        return getErrorInvoiceResponse(error);
                    }
                } else {
                    return getSuccessInvoice(message, invoiceId);
                }
            } catch (Exception e) {
                log.error("Convert invoice response error.", e);
            }
        }
        return null;
    }

    /**
     * @param invoiceId Invoice number
     * @param login Login for authentication in ETRAN
     * @param password Password for authentication in ETRAN
     * @return Filled and marshalled request for invoice
     */
    private HttpEntity<GetBlockRequest> prepareInvoiceRequest(String invoiceId, String login,
                                                  String password, String token) throws JsonProcessingException {
        InvoiceRequest invoiceRequest = new InvoiceRequest();
        ValueAttribute invoiceIdValue = new ValueAttribute();
        invoiceIdValue.setValue(invoiceId);
        invoiceRequest.setInvoiceId(invoiceIdValue);
        InvoiceRequestWrapper invoiceRequestWrapper = new InvoiceRequestWrapper();
        invoiceRequestWrapper.setInvoiceRequest(invoiceRequest);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, TOKEN_HEADER + token);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        return new HttpEntity<>(
                fillGetBlockRequest(invoiceRequestWrapper, login, password),
                httpHeaders);
    }

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @param login Login for authentication in ETRAN
     * @param password Password for authentication in ETRAN
     * @return Filled and marshalled request for invoice status
     */
    private HttpEntity<GetBlockRequest> prepareInvoiceStatusRequest(LocalDateTime currentDay, String login,
                                                        String password, String token) throws JsonProcessingException {
        InvoiceStatusRequest invoiceStatusRequest = new InvoiceStatusRequest();
        LocalDateTime fromDate = currentDay.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime toDate = currentDay.withHour(23).withMinute(59).withSecond(59);
        invoiceStatusRequest.setFromDate(DateUtils.convertToValueAttribute(fromDate));
        invoiceStatusRequest.setToDate(DateUtils.convertToValueAttribute(toDate));
        InvoiceStatusRequestWrapper invoiceStatusRequestWrapper = new InvoiceStatusRequestWrapper();
        invoiceStatusRequestWrapper.setInvoiceStatusRequest(invoiceStatusRequest);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, TOKEN_HEADER + token);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        return new HttpEntity<>(
                fillGetBlockRequest(invoiceStatusRequestWrapper, login, password),
                httpHeaders);
    }

    /**
     * @param message Object message request
     * @param login Login for authentication in ETRAN
     * @param password Password for authentication in ETRAN
     * @return Filled full request message with authentication info for ETRAN
     */
    private static GetBlockRequest fillGetBlockRequest(Object message, String login, String password) {
        GetBlockRequest getBlockRequest = new GetBlockRequest();
        getBlockRequest.setMessage(message);
        getBlockRequest.setLogin(login);
        getBlockRequest.setPassword(password);
        return getBlockRequest;
    }

    /**
     * @param error Error that ETRAN returned
     * @return InvoiceStatusResponse with filled error auth tag
     */
    private static InvoiceStatusResponse getErrorInvoiceStatusResponse(Error error) {
        InvoiceStatusResponse invoiceStatusResponse = new InvoiceStatusResponse();
        invoiceStatusResponse.setErrorAuth(AUTH_ERROR.equalsIgnoreCase(error.getErrorStatusCode().getValue()));
        return invoiceStatusResponse;
    }

    /**
     * @param error Error that ETRAN returned
     * @return InvoiceResponse with filled error auth tag
     */
    private static InvoiceResponse getErrorInvoiceResponse(Error error) {
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setErrorAuth(AUTH_ERROR.equalsIgnoreCase(error.getErrorStatusCode().getValue()));
        return invoiceResponse;
    }

    private InvoiceResponse getSuccessInvoice(String message, String invoiceId) throws XPathExpressionException {
        InputSource messageSource = new InputSource(message);
        String lastOperDate = (String) xPath.evaluate("//getInvoiceReply/invLastOper/@value", messageSource, XPathConstants.STRING);
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setInvoiceId(invoiceId);
        invoiceResponse.setLastOperDate(DateUtils.convertToLocalDateTime(lastOperDate));
        invoiceResponse.setErrorAuth(false);
        invoiceResponse.setMessage(message);
        return invoiceResponse;
    }

    private static boolean isNotNeedRepeatRequest(ResponseEntity<GetBlockResponse> responseEntity) {
        return !responseEntity.getStatusCode().is2xxSuccessful()
                && (responseEntity.getStatusCodeValue() == 400
                || responseEntity.getStatusCodeValue() == 404);
    }

    /**
     * @param error Error that ETRAN returned
     * @return need to re-request to ETRAN
     */
    private static boolean isNotNeedRepeatRequest(Error error) {
        return NOT_REPEAT_ERROR.contains(error.getErrorCode().getValue())
                || NOT_REPEAT_ERROR.contains(error.getErrorStatusCode().getValue());
    }

}
