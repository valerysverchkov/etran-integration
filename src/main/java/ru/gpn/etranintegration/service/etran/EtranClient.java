package ru.gpn.etranintegration.service.etran;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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

    public InvoiceStatusResponse getInvoiceStatus(InvoiceStatusRequest invoiceStatusRequest) {
        Object objInvoiceStatusResponse = getWebServiceTemplate().marshalSendAndReceive(
                uriInvoiceStatus,
                invoiceStatusRequest,
                new SoapActionCallback(invoiceStatusAction)
        );
        if (objInvoiceStatusResponse instanceof InvoiceStatusResponse) {
            return (InvoiceStatusResponse) objInvoiceStatusResponse;
        } else {
            //TODO: or catch ClassCastException??
            log.error("Response is not cast to response class. Response: {}", objInvoiceStatusResponse);
            return null;
        }
    }

    public InvoiceResponse getInvoice(InvoiceRequest invoiceRequest) {
        Object objInvoiceStatusResponse = getWebServiceTemplate().marshalSendAndReceive(
                uriInvoice,
                invoiceRequest,
                new SoapActionCallback(invoiceAction));
        if (objInvoiceStatusResponse instanceof InvoiceResponse) {
            return (InvoiceResponse) objInvoiceStatusResponse;
        } else {
            //TODO: or catch ClassCastException??
            log.error("Response is not cast to response class. Response: {}", objInvoiceStatusResponse);
            return null;
        }
    }

}
