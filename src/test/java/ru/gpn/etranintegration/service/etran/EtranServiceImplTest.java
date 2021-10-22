package ru.gpn.etranintegration.service.etran;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;
import ru.gpn.etranintegration.client.EtranClient;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.GetBlockResponse;
import ru.gpn.etranintegration.model.etran.message.InvoiceRequest;
import ru.gpn.etranintegration.model.etran.message.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusRequest;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.util.DateUtils;

import javax.xml.transform.Result;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtranServiceImplTest {

    private static final String TOKEN = "TOKEN";
    private static final String INVOICE_ID = "ID";
    private static final String AUTH = "logpass";
    private static final String MESSAGE = "message";

    @InjectMocks
    private EtranServiceImpl etranService;
    @Mock
    private EtranClient etranClient;
    @Mock
    private Jaxb2Marshaller messageMarshaller;

    @Captor
    private ArgumentCaptor<GetBlockRequest> blockCaptor;
    @Captor
    private ArgumentCaptor<InvoiceStatusRequest> invoiceStatusCaptor;
    @Captor
    private ArgumentCaptor<InvoiceRequest> invoiceCaptor;
    @Captor
    private ArgumentCaptor<StringSource> stringSourceCaptor;

    @Test
    void whenCallMethodGetInvoiceStatusThenCallEtranAuthServiceAndCallEtranClientWithToken() {
        LocalDateTime now = LocalDateTime.now();

        GetBlockResponse expectedBlock = new GetBlockResponse();
        expectedBlock.setMessage(MESSAGE);
        when(etranClient.getInvoiceStatus(any(GetBlockRequest.class), eq(TOKEN))).thenReturn(expectedBlock);

        InvoiceStatusResponse expected = new InvoiceStatusResponse();
        when(messageMarshaller.unmarshal(any(StringSource.class))).thenReturn(expected);

        InvoiceStatusResponse actual = etranService.getInvoiceStatus(now, AUTH, AUTH, TOKEN);
        Assertions.assertEquals(expected, actual);

        verify(messageMarshaller).marshal(invoiceStatusCaptor.capture(), any(Result.class));
        InvoiceStatusRequest invoiceStatusRequest = invoiceStatusCaptor.getValue();
        Assertions.assertEquals(
                DateUtils.convertToString(now.withHour(0).withMinute(0).withSecond(0)),
                invoiceStatusRequest.getFromDate().getValue()
        );
        Assertions.assertEquals(
                DateUtils.convertToString(now.withHour(23).withMinute(59).withSecond(59)),
                invoiceStatusRequest.getToDate().getValue()
        );

        verify(messageMarshaller).unmarshal(stringSourceCaptor.capture());
        StringSource actualStringSource = stringSourceCaptor.getValue();
        Assertions.assertEquals(MESSAGE, actualStringSource.toString());
    }

    @Test
    void whenCallMethodGetInvoiceThenCallEtranAuthServiceAndCallEtranClientWithToken() {
        GetBlockResponse expectedBlock = new GetBlockResponse();
        expectedBlock.setMessage(MESSAGE);
        when(etranClient.getInvoice(any(GetBlockRequest.class), eq(TOKEN))).thenReturn(expectedBlock);

        InvoiceResponse expected = new InvoiceResponse();
        when(messageMarshaller.unmarshal(any(StringSource.class))).thenReturn(expected);

        InvoiceResponse actual = etranService.getInvoice(INVOICE_ID, AUTH, AUTH, TOKEN);
        Assertions.assertEquals(expected, actual);

        verify(messageMarshaller).marshal(invoiceCaptor.capture(), any(Result.class));
        InvoiceRequest invoiceRequest = invoiceCaptor.getValue();
        Assertions.assertEquals(INVOICE_ID, invoiceRequest.getInvNumber().getValue());

        verify(messageMarshaller).unmarshal(stringSourceCaptor.capture());
        StringSource actualStringSource = stringSourceCaptor.getValue();
        Assertions.assertEquals(MESSAGE, actualStringSource.toString());
    }

}