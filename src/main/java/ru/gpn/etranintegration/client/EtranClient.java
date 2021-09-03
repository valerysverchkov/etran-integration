package ru.gpn.etranintegration.client;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.xml.transform.StringSource;
import ru.gpn.etranintegration.model.etran.InvoiceRequest;
import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusRequest;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;
import ru.gpn.etranintegration.model.exception.AddTokenException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;

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

    @Setter
    private TransformerFactory transformerFactory;

    public InvoiceStatusResponse getInvoiceStatus(InvoiceStatusRequest invoiceStatusRequest, String token) {
        return sendAndReceive(uriInvoiceStatus, invoiceStatusRequest, token);
    }

    public InvoiceResponse getInvoice(InvoiceRequest invoiceRequest, String token) {
        return sendAndReceive(uriInvoice, invoiceRequest, token);
    }

    private <T> T sendAndReceive(String uri, Object request, String token) {
        try {
            return  (T) getWebServiceTemplate().marshalSendAndReceive(
                    uri,
                    request,
                    webServiceMessage -> {
                        try {
                            SoapMessage soapMessage = (SoapMessage) webServiceMessage;
                            SoapHeader soapHeader = soapMessage.getSoapHeader();
                            StringSource stringSource = new StringSource("<token>" + token + "</token>");
                            Transformer transformer = transformerFactory.newTransformer();
                            transformer.transform(stringSource, soapHeader.getResult());
                        } catch (Exception e) {
                            throw new AddTokenException("Add token in header error.", e);
                        }
                    }
            );
        } catch (ClassCastException e) {
            log.error("Cast class Error.", e);
        } catch (AddTokenException e) {
            log.error(e.getMessage(), e);
        } catch (WebServiceClientException e) {
            log.error("Etran web service error.", e);
        }
        return null;
    }

}
