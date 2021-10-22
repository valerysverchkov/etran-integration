package ru.gpn.etranintegration.service.etran;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.xml.sax.InputSource;
import ru.gpn.etranintegration.client.EtranClient;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.GetBlockResponse;
import ru.gpn.etranintegration.model.etran.message.Error;
import ru.gpn.etranintegration.model.etran.message.InvoiceRequest;
import ru.gpn.etranintegration.model.etran.message.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusRequest;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.util.DateUtils;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * This service is responsible for calling ETRAN service via ESB service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
class EtranServiceImpl implements EtranService {

    private static final String ERROR_TAG = "<error version=\"1.0\">";
    private static final String AUTH_ERROR = "401";
    private static final Set<String> NOT_REPEAT_ERROR = Set.of("1", "2", "3", "4", "100", AUTH_ERROR, "406");

    @Value("${service.etran.cntRequest}")
    private int requestCnt;

    private final EtranClient etranClient;
    private final XmlMapper xmlMapper;
    private final XPath xPath = XPathFactory.newInstance().newXPath();

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @return Numbers of invoices for the current day
     */
    @Override
    public InvoiceStatusResponse getInvoiceStatus(LocalDateTime currentDay, String login, String password, String token) {
        GetBlockRequest blockRequest;
        try {
            blockRequest = prepareInvoiceStatusRequest(currentDay, login, password);
        } catch (JsonProcessingException e) {
            log.error("Invoice status request build error.", e);
            return null;
        }
        GetBlockResponse invoiceStatus;
        for (int i = 0; i < requestCnt; i++) {
            try {
                invoiceStatus = etranClient.getInvoiceStatus(blockRequest, token);
            } catch (WebServiceClientException e) {
                if (e.getCause() instanceof SocketTimeoutException) continue;
                log.error("Invoice status request to ETRAN error.", e);
                return null;
            }

            if (isNullResponse(invoiceStatus)) {
                log.error("Invoice status response is null.");
                continue;
            }

            try {
                String message = invoiceStatus.getMessage();
                if (message.contains(ERROR_TAG)) {
                    Error error = xmlMapper.readValue(message, Error.class);
                    if (isNotNeedRepeatRequest(error)) {
                        log.error(
                                "Etran returned an error in witch retrying request would not yield any result. Response: {}",
                                invoiceStatus
                        );
                        return getErrorInvoiceStatusResponse(error);
                    }
                } else {
                    return xmlMapper.readValue(message, InvoiceStatusResponse.class);
                }
            } catch (Exception e) {
                log.error("Convert invoice status response error.", e);
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
        GetBlockRequest blockRequest;
        try {
            blockRequest = prepareInvoiceRequest(invoiceId, login, password);
        } catch (JsonProcessingException e) {
            log.error("Invoice request build error.", e);
            return null;
        }

        GetBlockResponse invoice;
        for (int i = 0; i < requestCnt; i++) {
            try {
                invoice = etranClient.getInvoice(blockRequest, token);
            } catch (WebServiceClientException e) {
                if (e.getCause() instanceof SocketTimeoutException) continue;
                log.error("Invoice request to ETRAN error.", e);
                return null;
            }

            if (isNullResponse(invoice)) {
                log.error("Invoice response is null.");
                continue;
            }

            try {
                String message = invoice.getMessage();
                if (message.contains(ERROR_TAG)) {
                    Error error = xmlMapper.readValue(message, Error.class);
                    if (isNotNeedRepeatRequest(error)) {
                        log.error(
                                "Etran returned an error in witch retrying request would not yield any result. Response: {}",
                                invoice
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
    private GetBlockRequest prepareInvoiceRequest(String invoiceId, String login, String password) throws JsonProcessingException {
        InvoiceRequest invoiceRequest = new InvoiceRequest();
        ValueAttribute invNumberValue = new ValueAttribute();
        invNumberValue.setValue(invoiceId);
        invoiceRequest.setInvNumber(invNumberValue);
        String messageStr = xmlMapper.writeValueAsString(invoiceRequest);
        return fillGetBlockRequest(messageStr, login, password);
    }

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @param login Login for authentication in ETRAN
     * @param password Password for authentication in ETRAN
     * @return Filled and marshalled request for invoice status
     */
    private GetBlockRequest prepareInvoiceStatusRequest(LocalDateTime currentDay, String login, String password) throws JsonProcessingException {
        InvoiceStatusRequest invoiceStatusRequest = new InvoiceStatusRequest();
        LocalDateTime fromDate = currentDay.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime toDate = currentDay.withHour(23).withMinute(59).withSecond(59);
        invoiceStatusRequest.setFromDate(DateUtils.convertToValueAttribute(fromDate));
        invoiceStatusRequest.setToDate(DateUtils.convertToValueAttribute(toDate));
        String messageStr = xmlMapper.writeValueAsString(invoiceStatusRequest);
        return fillGetBlockRequest(messageStr, login, password);
    }

    /**
     * @param message Marshalled message request
     * @param login Login for authentication in ETRAN
     * @param password Password for authentication in ETRAN
     * @return Filled full request message with authentication info for ETRAN
     */
    private static GetBlockRequest fillGetBlockRequest(String message, String login, String password) {
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

    /**
     * @param error Error that ETRAN returned
     * @return need to re-request to ETRAN
     */
    private static boolean isNotNeedRepeatRequest(Error error) {
        return NOT_REPEAT_ERROR.contains(error.getErrorCode().getValue())
                || NOT_REPEAT_ERROR.contains(error.getErrorStatusCode().getValue());
    }

    private static boolean isNullResponse(GetBlockResponse invoiceStatus) {
        return invoiceStatus == null || invoiceStatus.getMessage() == null;
    }

    private InvoiceResponse getSuccessInvoice(String message, String invoiceId) throws XPathExpressionException {
        InputSource messageSource = new InputSource(message);
        String lastOperDate = (String) xPath.evaluate("//getInvoiceReply/invLastOper/@value", messageSource, XPathConstants.STRING);
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setInvNumber(invoiceId);
        invoiceResponse.setLastOperDate(DateUtils.convertToLocalDateTime(lastOperDate));
        invoiceResponse.setErrorAuth(false);
        invoiceResponse.setMessage(message);
        return invoiceResponse;
    }

}
