package ru.gpn.etranintegration.service.converter;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.GetBlockResponse;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceRequest;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceRequestWrapper;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.Invoice;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusRequest;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusRequestWrapper;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.util.DateUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

@Component
@Slf4j
public class EtranConverterImpl implements EtranConverter {

    private static final String ERROR_TAG = "error";
    private static final String ERROR_CODE_TAG = "errorCode";
    private static final String ERROR_STATUS_CODE_TAG = "errorStatusCode";
    private static final String INVOICE_STATUS_RESPONSE_TAG = "invoiceStatusReply";
    private static final String INVOICE_TAG = "invoice";
    private static final String INVOICE_ID_TAG = "invoiceID";
    private static final String INVOICE_NUM_TAG = "invNumber";
    private static final String LAST_OPER_DATE_TAG = "invoiceLastOper";
    private static final String LAST_OPER_DATE_INV_TAG = "invLastOper";
    private static final String INVOICE_RESPONSE_TAG = "getInvoiceReply";
    private static final String VALUE_TAG = "value";
    private static final String AUTH_ERROR = "401";
    private static final Set<String> NOT_REPEAT_ERROR = Set.of("1", "2", "3", "4", "100", AUTH_ERROR, "406");

    /**
     * @param currentDay Date by which the search for invoice numbers is performed in ETRAN
     * @param login Login for authentication in ETRAN
     * @param password Password for authentication in ETRAN
     * @return Filled request for ETRAN
     */
    @Override
    public GetBlockRequest convertToInvoiceStatusRequest(LocalDateTime currentDay, String login, String password) {
        InvoiceStatusRequest invoiceStatusRequest = new InvoiceStatusRequest();
        LocalDateTime fromDate = currentDay.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime toDate = currentDay.withHour(23).withMinute(59).withSecond(59);
        invoiceStatusRequest.setFromDate(DateUtils.convertToValueAttribute(fromDate));
        invoiceStatusRequest.setToDate(DateUtils.convertToValueAttribute(toDate));
        InvoiceStatusRequestWrapper invoiceStatusRequestWrapper = new InvoiceStatusRequestWrapper();
        invoiceStatusRequestWrapper.setInvoiceStatusRequest(invoiceStatusRequest);
        return fillGetBlockRequest(invoiceStatusRequestWrapper, login, password);
    }

    /**
     * @param responseEntity Response invoice status from ETRAN
     * @return Fill InvoiceStatusResponse object with data from responseEntity
     */
    @Override
    public InvoiceStatusResponse convertToInvoiceStatusResponse(ResponseEntity<GetBlockResponse> responseEntity) {
        try {
            LinkedHashMap<String, Object> message = (LinkedHashMap<String, Object>) responseEntity.getBody().getMessage();

            if (message.containsKey(ERROR_TAG)) {
                LinkedHashMap<String, Object> error = (LinkedHashMap<String, Object>) message.get(ERROR_TAG);
                String errorCode = getValue(error, ERROR_CODE_TAG);
                String errorStatusCode = getValue(error, ERROR_STATUS_CODE_TAG);

                if (isNotNeedRepeatRequest(errorCode, errorStatusCode)) {
                    log.error(
                            "Etran returned an error in witch retrying request would not yield any result. Response: {}",
                            message
                    );
                    return getErrorInvoiceStatusResponse(errorStatusCode);
                }
            }

            LinkedHashMap<String, Object> invoiceStatusReply =
                    (LinkedHashMap<String, Object>) message.get(INVOICE_STATUS_RESPONSE_TAG);
            ArrayList<Invoice> invoicesResponse = new ArrayList<>();
            if (invoiceStatusReply.get(INVOICE_TAG) instanceof ArrayList) {
                ArrayList<LinkedHashMap<String, Object>> invoices =
                        (ArrayList<LinkedHashMap<String, Object>>) invoiceStatusReply.get(INVOICE_TAG);
                for (LinkedHashMap<String, Object> invoice : invoices) {
                    invoicesResponse.add(getInvoiceStatusInfo(invoice));
                }
            } else {
                LinkedHashMap<String, Object> invoice = (LinkedHashMap<String, Object>) invoiceStatusReply.get(INVOICE_TAG);
                invoicesResponse.add(getInvoiceStatusInfo(invoice));
            }

            InvoiceStatusResponse invoiceStatusResponse = new InvoiceStatusResponse();
            invoiceStatusResponse.setInvoice(invoicesResponse);
            invoiceStatusResponse.setErrorAuth(false);
            return invoiceStatusResponse;
        } catch (Exception e) {
            log.error("Convert invoice status response error.", e);
            return null;
        }
    }

    /**
     * @param invoiceId Invoice id
     * @param invoiceNum Invoice number
     * @param login Login for authentication in ETRAN
     * @param password Password for authentication in ETRAN
     * @return Filled request for ETRAN
     */
    @Override
    public GetBlockRequest convertToInvoiceRequest(String invoiceId, String invoiceNum, String login, String password) {
        InvoiceRequest invoiceRequest = new InvoiceRequest();
        ValueAttribute invoiceIdValue = new ValueAttribute();
        invoiceIdValue.setValue(invoiceId);
        invoiceRequest.setInvoiceId(invoiceIdValue);
        ValueAttribute invoiceNumValue = new ValueAttribute();
        invoiceNumValue.setValue(invoiceNum);
        invoiceRequest.setInvNumber(invoiceNumValue);
        InvoiceRequestWrapper invoiceRequestWrapper = new InvoiceRequestWrapper();
        invoiceRequestWrapper.setInvoiceRequest(invoiceRequest);
        return fillGetBlockRequest(invoiceRequestWrapper, login, password);
    }

    /**
     * @param responseEntity Response invoice from ETRAN
     * @return Fill InvoiceResponse object with data from responseEntity
     */
    @Override
    public InvoiceResponse convertToInvoiceResponse(ResponseEntity<GetBlockResponse> responseEntity) {
        try {
            LinkedHashMap<String, Object> message = (LinkedHashMap<String, Object>) responseEntity.getBody().getMessage();

            if (message.containsKey(ERROR_TAG)) {
                LinkedHashMap<String, Object> error = (LinkedHashMap<String, Object>) message.get(ERROR_TAG);
                String errorCode = getValue(error, ERROR_CODE_TAG);
                String errorStatusCode = getValue(error, ERROR_STATUS_CODE_TAG);

                if (isNotNeedRepeatRequest(errorCode, errorStatusCode)) {
                    log.error(
                            "Etran returned an error in witch retrying request would not yield any result. Response: {}",
                            message
                    );
                    return getErrorInvoiceResponse(errorStatusCode);
                }
            }

            LinkedHashMap<String, Object> invoiceReply =
                    (LinkedHashMap<String, Object>) message.get(INVOICE_RESPONSE_TAG);

            InvoiceResponse invoiceResponse = new InvoiceResponse();
            invoiceResponse.setInvoiceId(getValue(invoiceReply, INVOICE_ID_TAG));
            invoiceResponse.setInvoiceNum(getValue(invoiceReply, INVOICE_NUM_TAG));
            invoiceResponse.setLastOperDate(DateUtils.convertToLocalDateTime(getValue(invoiceReply, LAST_OPER_DATE_INV_TAG)));
            invoiceResponse.setErrorAuth(false);
            JSONObject invoiceReplyJson = new JSONObject(invoiceReply);
            invoiceResponse.setMessage(invoiceReplyJson.toString());
            return invoiceResponse;
        } catch (Exception e) {
            log.error("Convert invoice response error.", e);
            return null;
        }
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
     * @param map Map where tag is located
     * @param tag Tag name
     * @return Value attribute for a given tag
     */
    private static String getValue(LinkedHashMap<String, Object> map, String tag) {
        LinkedHashMap<String, String> tagMap = (LinkedHashMap<String, String>) map.get(tag);
        return tagMap.get(VALUE_TAG);
    }

    /**
     * @param errorCode Error code that ETRAN returned
     * @param errorStatusCode Error status code that ETRAN returned
     * @return need to re-request to ETRAN
     */
    private static boolean isNotNeedRepeatRequest(String errorCode, String errorStatusCode) {
        return NOT_REPEAT_ERROR.contains(errorCode)
                || NOT_REPEAT_ERROR.contains(errorStatusCode);
    }

    /**
     * @param invoice Map with invoice information
     * @return Fill Invoice object with invoice information
     */
    private static Invoice getInvoiceStatusInfo(LinkedHashMap<String, Object> invoice) {
        Invoice invoiceResponse = new Invoice();
        invoiceResponse.setInvoiceId(getValue(invoice, INVOICE_ID_TAG));
        invoiceResponse.setInvoiceLastOper(getValue(invoice, LAST_OPER_DATE_TAG));
        invoiceResponse.setInvNumber(getValue(invoice, INVOICE_NUM_TAG));
        return invoiceResponse;
    }

    /**
     * @param errorStatusCode Error status code that ETRAN returned
     * @return InvoiceStatusResponse with filled error auth tag
     */
    private static InvoiceStatusResponse getErrorInvoiceStatusResponse(String errorStatusCode) {
        InvoiceStatusResponse invoiceStatusResponse = new InvoiceStatusResponse();
        invoiceStatusResponse.setErrorAuth(AUTH_ERROR.equalsIgnoreCase(errorStatusCode));
        return invoiceStatusResponse;
    }

    /**
     * @param errorStatusCode Error status code that ETRAN returned
     * @return InvoiceResponse with filled error auth tag
     */
    private static InvoiceResponse getErrorInvoiceResponse(String errorStatusCode) {
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setErrorAuth(AUTH_ERROR.equalsIgnoreCase(errorStatusCode));
        return invoiceResponse;
    }

}
