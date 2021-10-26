package ru.gpn.etranintegration.service.process;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gpn.etranintegration.config.EtranAuthConfig;
import ru.gpn.etranintegration.model.etran.auth.EtranAuthorization;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.Invoice;
import ru.gpn.etranintegration.model.etran.message.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;
import ru.gpn.etranintegration.service.cache.CacheService;
import ru.gpn.etranintegration.service.esb.EsbAuthService;
import ru.gpn.etranintegration.service.etran.EtranService;
import ru.gpn.etranintegration.service.ibpd.IbpdService;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceProcessTest {

    private static final String ID1 = "ID1";
    private static final String AUTH = "logpass";
    private static final String TOKEN = "token";

    @InjectMocks
    private InvoiceProcess invoiceProcess;

    @Mock
    private EsbAuthService esbAuthService;

    @Mock
    private EtranService etranService;

    @Mock
    private IbpdService ibpdService;

    @Mock
    private CacheService cacheService;

    @Mock
    private EtranAuthConfig etranAuthConfig;

    @Test
    void whenEtranServiceReturnedEmptyInvoiceIdsThenTerminateProcessedWithoutHandlingInvoices() {
        InvoiceStatusResponse invoiceStatusResponse = new InvoiceStatusResponse();
        invoiceStatusResponse.setInvoice(new ArrayList<>());

        ArrayList<EtranAuthorization> etranAuthorizations = new ArrayList<>();
        EtranAuthorization etranAuthorization = new EtranAuthorization();
        etranAuthorization.setLogin(AUTH);
        etranAuthorization.setPassword(AUTH);
        etranAuthorizations.add(etranAuthorization);

        when(etranAuthConfig.getCredential()).thenReturn(etranAuthorizations);
        when(esbAuthService.getToken()).thenReturn(TOKEN);
        when(etranService.getInvoiceStatus(any(LocalDateTime.class), eq(AUTH), eq(AUTH), eq(TOKEN)))
                .thenReturn(invoiceStatusResponse);

        invoiceProcess.processing();

        verify(ibpdService, times(0)).getLastOperDateByInvoiceId(anyString());
    }

    @Test
    void whenEtranServiceReturnedInvoiceIdsAndIbpdServiceReturnedEmptyIdsAndLastOperDateFromIbpdByIdIsNullThenSetNewInvoiceByIdsFromEtran() {
        InvoiceResponse invoiceResponse = prepareMockMethodAndObject(null);

        invoiceProcess.processing();

        verify(ibpdService).setNewInvoice(invoiceResponse);
    }

    @Test
    void whenEtranServiceReturnedInvoiceIdsAndIbpdServiceReturnedIdsAndLastOperDateFromIbpdByIdIsNotNullThenNothingIsExecuted() {
        InvoiceResponse invoiceResponse = prepareMockMethodAndObject(LocalDateTime.now().plusHours(1L));

        invoiceProcess.processing();

        verify(ibpdService, times(0)).setNewInvoice(invoiceResponse);
        verify(ibpdService, times(0)).updateInvoice(invoiceResponse);
    }

    @Test
    void whenEtranServiceReturnedInvoiceIdsAndIbpdServiceReturnedIdsAndLastOperDateFromIbpdByIdIsNotNullThenUpdateInvoiceByIdsFromEtran() {
        InvoiceResponse invoiceResponse = prepareMockMethodAndObject(LocalDateTime.now().minusHours(1L));

        invoiceProcess.processing();

        verify(ibpdService).updateInvoice(invoiceResponse);
    }

    private InvoiceResponse prepareMockMethodAndObject(LocalDateTime lastOperDate) {
        InvoiceStatusResponse invoiceStatusResponse = new InvoiceStatusResponse();
        ArrayList<Invoice> invoices = new ArrayList<>();
        Invoice invoice = new Invoice();
        ValueAttribute invoiceId = new ValueAttribute();
        invoiceId.setValue(ID1);
        invoice.setInvoiceId(invoiceId);
        ValueAttribute lastOperDateValue = new ValueAttribute();
        lastOperDateValue.setValue(lastOperDate != null ? lastOperDate.toString() : null);
        invoice.setInvoiceLastOper(lastOperDateValue);
        invoices.add(invoice);
        invoiceStatusResponse.setInvoice(invoices);

        ArrayList<EtranAuthorization> etranAuthorizations = new ArrayList<>();
        EtranAuthorization etranAuthorization = new EtranAuthorization();
        etranAuthorization.setLogin(AUTH);
        etranAuthorization.setPassword(AUTH);
        etranAuthorizations.add(etranAuthorization);

        when(etranAuthConfig.getCredential()).thenReturn(etranAuthorizations);
        when(esbAuthService.getToken()).thenReturn(TOKEN);
        when(etranService.getInvoiceStatus(any(LocalDateTime.class), eq(AUTH), eq(AUTH), eq(TOKEN)))
                .thenReturn(invoiceStatusResponse);
        when(cacheService.getLastOperDateByInvoiceId(ID1))
                .thenReturn(lastOperDate != null ? lastOperDate.minusHours(1L).toString() : null);

        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setInvNumber(ID1);
        invoiceResponse.setLastOperDate(LocalDateTime.now());
        when(etranService.getInvoice(ID1, AUTH, AUTH, TOKEN)).thenReturn(invoiceResponse);

        when(ibpdService.getLastOperDateByInvoiceId(ID1)).thenReturn(lastOperDate);
        return invoiceResponse;
    }

}