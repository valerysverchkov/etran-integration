package ru.gpn.etranintegration.client;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.CommonsHttpConnection;
import org.springframework.ws.transport.http.HttpUrlConnection;
import org.springframework.xml.transform.StringSource;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.GetBlockResponse;
import ru.gpn.etranintegration.model.exception.AddTokenException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

@Slf4j
public class EtranClient extends WebServiceGatewaySupport {

    private static final String TOKEN_KEY = "token";

    @Setter
    private String uriInvoiceStatus;

    @Setter
    private String uriInvoice;

    public GetBlockResponse getInvoiceStatus(GetBlockRequest invoiceStatusRequest, String token) {
        return sendAndReceiveWithHttpHeader(uriInvoiceStatus, invoiceStatusRequest, token);
    }

    public GetBlockResponse getInvoice(GetBlockRequest invoiceRequest, String token) {
        return sendAndReceiveWithHttpHeader(uriInvoice, invoiceRequest, token);
    }

    private GetBlockResponse sendAndReceiveWithHttpHeader(String uri, GetBlockRequest request, String token) {
        try {
            return (GetBlockResponse) getWebServiceTemplate().marshalSendAndReceive(
                    uri,
                    request,
                    webServiceMessage -> {
                        try {
                            TransportContext transportContext = TransportContextHolder.getTransportContext();
                            HttpUrlConnection connection = (HttpUrlConnection) transportContext.getConnection();
                            connection.getConnection().addRequestProperty(TOKEN_KEY, token);
                        } catch (Exception e) {
                            throw new AddTokenException("Add token in header error.", e);
                        }
                    }
            );
        } catch (ClassCastException e) {
            log.error("Cast response Error.", e);
        } catch (AddTokenException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
