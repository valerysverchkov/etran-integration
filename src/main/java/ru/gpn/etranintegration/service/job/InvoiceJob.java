package ru.gpn.etranintegration.service.job;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gpn.etranintegration.service.process.Process;

@Component
class InvoiceJob implements Job {

    public InvoiceJob(@Qualifier("invoiceProcess") Process invoiceProcess) {
        this.invoiceProcess = invoiceProcess;
    }

    private final Process invoiceProcess;

    @Scheduled(cron = "${scheduled.cron.invoice}")
    @Override
    public void schedule() {
        invoiceProcess.processing();
    }
}
