package ru.gpn.etranintegration.service.process;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.cache.CacheService;
import ru.gpn.etranintegration.service.etran.EtranService;
import ru.gpn.etranintegration.service.ibpd.IbpdService;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceProcessTest {

    private static final String ID1 = "ID1";

    @InjectMocks
    private InvoiceProcess invoiceProcess;

    @Mock
    private EtranService etranService;

    @Mock
    private IbpdService ibpdService;

    @Mock
    private CacheService cacheService;

    @Test
    void whenEtranServiceReturnedEmptyInvoiceIdsThenTerminateProcessedWithoutHandlingInvoices() {
        InvoiceStatusResponse invoiceStatusResponse = new InvoiceStatusResponse();
        invoiceStatusResponse.setInvoiceIds(new ArrayList<>());
        when(etranService.getInvoiceStatus(any(LocalDateTime.class))).thenReturn(invoiceStatusResponse);

        invoiceProcess.processing();

        verify(ibpdService, times(0)).getInvoiceIds();
    }

    @Test
    void whenEtranServiceReturnedInvoiceIdsAndIbpdServiceReturnedEmptyIdsAndLastOperDateFromIbpdByIdIsNullThenSetNewInvoiceByIdsFromEtran() {
        InvoiceStatusResponse invoiceStatusResponse = new InvoiceStatusResponse();
        ArrayList<String > invoiceIds = new ArrayList<>();
        invoiceIds.add(ID1);
        invoiceStatusResponse.setInvoiceIds(invoiceIds);
        when(etranService.getInvoiceStatus(any(LocalDateTime.class))).thenReturn(invoiceStatusResponse);

        when(ibpdService.getInvoiceIds()).thenReturn(new ArrayList<>());

        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setInvNumber(ID1);
        invoiceResponse.setLastOperDate(LocalDateTime.now());
        when(etranService.getInvoice(ID1)).thenReturn(invoiceResponse);

        when(ibpdService.getLastOperDateByInvoiceId(ID1)).thenReturn(null);

        invoiceProcess.processing();

        verify(ibpdService).setNewInvoice(invoiceResponse);
    }

    @Test
    void whenEtranServiceReturnedInvoiceIdsAndIbpdServiceReturnedIdsAndLastOperDateFromIbpdByIdIsNullThenSetNewInvoiceByIdsFromEtran() {

    }

}