package ru.gpn.etranintegration.client;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import ru.gpn.etranintegration.model.etran.InvoiceRequest;
import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusRequest;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;

@Slf4j
public class EtranClient extends WebServiceGatewaySupport {

    @Setter
    private String uriInvoiceStatus;

    @Setter
    private String invoiceStatusAction;

    @Setter
    private String uriInvoice;

    @Setter
    private String invoiceAction;

    public InvoiceStatusResponse getInvoiceStatus(InvoiceStatusRequest invoiceStatusRequest, String token) {
        return sendAndReceive(uriInvoiceStatus, invoiceStatusRequest, invoiceStatusAction);
    }

    public InvoiceResponse getInvoice(InvoiceRequest invoiceRequest, String token) {
        return sendAndReceive(uriInvoice, invoiceRequest, invoiceAction);
    }

    private <T> T sendAndReceive(String uri, Object request, String soapAction) {
        try {
            return  (T) getWebServiceTemplate().marshalSendAndReceive(
                    uri,
                    request,
                    new SoapActionCallback(soapAction)
            );
        } catch (ClassCastException e) {
            log.error("Response is not cast to response class.", e);
        } catch (WebServiceClientException e) {
            log.error("Etran web service error.", e);
        }
        return null;
    }
}
