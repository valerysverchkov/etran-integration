package ru.gpn.etranintegration.service.etran;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gpn.etranintegration.client.EtranClient;
import ru.gpn.etranintegration.model.etran.InvoiceRequest;
import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusRequest;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.esb.EsbAuthService;
import ru.gpn.etranintegration.service.util.DateUtils;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtranServiceImplTest {

    private static final String TOKEN = "TOKEN";
    private static final String INVOICE_ID = "ID";
    private static final String AUTH = "logpass";

    @InjectMocks
    private EtranServiceImpl etranService;
    @Mock
    private EsbAuthService etranAuthService;
    @Mock
    private EtranClient etranClient;

    @Captor
    private ArgumentCaptor<InvoiceStatusRequest> invoiceStatusCaptor;
    @Captor
    private ArgumentCaptor<InvoiceRequest> invoiceCaptor;

    @Test
    void whenCallMethodGetInvoiceStatusThenCallEtranAuthServiceAndCallEtranClientWithToken() {
        LocalDateTime now = LocalDateTime.now();

        when(etranAuthService.getToken()).thenReturn(TOKEN);

        InvoiceStatusResponse expected = new InvoiceStatusResponse();
        when(etranClient.getInvoiceStatus(any(InvoiceStatusRequest.class), eq(TOKEN))).thenReturn(expected);

        InvoiceStatusResponse actual = etranService.getInvoiceStatus(now, AUTH, AUTH);
        Assertions.assertEquals(expected, actual);

        verify(etranClient).getInvoiceStatus(invoiceStatusCaptor.capture(), eq(TOKEN));
        InvoiceStatusRequest actualRequest = invoiceStatusCaptor.getValue();
        Assertions.assertEquals(
                DateUtils.convertToString(now.withHour(0).withMinute(0).withSecond(0)),
                actualRequest.getFromDate().getValue()
        );
        Assertions.assertEquals(
                DateUtils.convertToString(now.withHour(23).withMinute(59).withSecond(59)),
                actualRequest.getToDate().getValue()
        );
    }

    @Test
    void whenCallMethodGetInvoiceThenCallEtranAuthServiceAndCallEtranClientWithToken() {
        when(etranAuthService.getToken()).thenReturn(TOKEN);

        InvoiceResponse expected = new InvoiceResponse();
        when(etranClient.getInvoice(any(InvoiceRequest.class), eq(TOKEN))).thenReturn(expected);

        InvoiceResponse actual = etranService.getInvoice(INVOICE_ID, AUTH, AUTH);
        Assertions.assertEquals(expected, actual);

        verify(etranClient).getInvoice(invoiceCaptor.capture(), eq(TOKEN));
        InvoiceRequest actualRequest = invoiceCaptor.getValue();
        Assertions.assertEquals(INVOICE_ID, actualRequest.getInvNumber());
        Assertions.assertEquals(Boolean.TRUE, actualRequest.getUseValid());
    }

}