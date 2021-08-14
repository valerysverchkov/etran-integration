package ru.gpn.etranintegration.service.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gpn.etranintegration.service.process.Process;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvoiceJobTest {

    @InjectMocks
    private InvoiceJob invoiceJob;

    @Mock
    private Process invoiceProcess;

    @Test
    void whenScheduleInvoiceJobThenCalledInvoiceProcess(){
        invoiceJob.schedule();
        verify(invoiceProcess).processing();
    }

}