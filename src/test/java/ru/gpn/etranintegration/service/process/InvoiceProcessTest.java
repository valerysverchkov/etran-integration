package ru.gpn.etranintegration.service.process;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gpn.etranintegration.config.EtranAuthConfig;
import ru.gpn.etranintegration.config.EtranAuthorization;
import ru.gpn.etranintegration.model.etran.Invoice;
import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;
import ru.gpn.etranintegration.model.etran.ValueAttribute;
import ru.gpn.etranintegration.service.cache.CacheService;
import ru.gpn.etranintegration.service.etran.EtranService;
import ru.gpn.etranintegration.service.ibpd.IbpdService;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceProcessTest {

    private static final String ID1 = "ID1";
    private static final String AUTH = "logpass";

    @InjectMocks
    private InvoiceProcess invoiceProcess;

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

        when(etranAuthConfig.getEtranAuthorizations()).thenReturn(etranAuthorizations);
        when(etranService.getInvoiceStatus(any(LocalDateTime.class), eq(AUTH), eq(AUTH)))
                .thenReturn(invoiceStatusResponse);

        invoiceProcess.processing();

        verify(ibpdService, times(0)).getInvoiceIds();
    }

    @Test
    void whenEtranServiceReturnedInvoiceIdsAndIbpdServiceReturnedEmptyIdsAndLastOperDateFromIbpdByIdIsNullThenSetNewInvoiceByIdsFromEtran() {
        InvoiceStatusResponse invoiceStatusResponse = new InvoiceStatusResponse();
        ArrayList<Invoice> invoices = new ArrayList<>();
        Invoice invoice = new Invoice();
        ValueAttribute valueAttribute = new ValueAttribute();
        valueAttribute.setValue(ID1);
        invoice.setInvoiceId(valueAttribute);
        invoices.add(invoice);
        invoiceStatusResponse.setInvoice(invoices);

        ArrayList<EtranAuthorization> etranAuthorizations = new ArrayList<>();
        EtranAuthorization etranAuthorization = new EtranAuthorization();
        etranAuthorization.setLogin(AUTH);
        etranAuthorization.setPassword(AUTH);
        etranAuthorizations.add(etranAuthorization);

        when(etranAuthConfig.getEtranAuthorizations()).thenReturn(etranAuthorizations);
        when(etranService.getInvoiceStatus(any(LocalDateTime.class), eq(AUTH), eq(AUTH)))
                .thenReturn(invoiceStatusResponse);

        //when(ibpdService.getInvoiceIds()).thenReturn(new ArrayList<>());

        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setInvNumber(ID1);
        invoiceResponse.setLastOperDate(LocalDateTime.now());
        when(etranService.getInvoice(ID1, AUTH, AUTH)).thenReturn(invoiceResponse);

        when(ibpdService.getLastOperDateByInvoiceId(ID1)).thenReturn(null);

        invoiceProcess.processing();

        verify(ibpdService).setNewInvoice(invoiceResponse);
    }

    @Test
    void whenEtranServiceReturnedInvoiceIdsAndIbpdServiceReturnedIdsAndLastOperDateFromIbpdByIdIsNullThenSetNewInvoiceByIdsFromEtran() {

    }

}