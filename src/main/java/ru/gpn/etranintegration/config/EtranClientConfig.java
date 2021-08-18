package ru.gpn.etranintegration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.gpn.etranintegration.client.EtranClient;

@Configuration
class EtranClientConfig {

    @Value("${service.etran.uri.url}")
    private String etranUrl;

    @Value("${service.etran.uri.path.invoice}")
    private String etranPathInvoice;

    @Value("${service.etran.uri.action.invoice}")
    private String etranActionInvoice;

    @Value("${service.etran.uri.path.invoiceStatus}")
    private String etranPathInvoiceStatus;

    @Value("${service.etran.uri.action.invoiceStatus}")
    private String etranActionInvoiceStatus;

    @Bean
    public EtranClient etranClient() {
        EtranClient etranClient = new EtranClient();
        etranClient.setDefaultUri(etranUrl);
        etranClient.setUriInvoice(etranUrl + etranPathInvoice);
        etranClient.setInvoiceAction(etranUrl + etranActionInvoice);
        etranClient.setUriInvoiceStatus(etranUrl + etranPathInvoiceStatus);
        etranClient.setInvoiceStatusAction(etranUrl + etranActionInvoiceStatus);
        etranClient.setMarshaller(marshaller());
        return etranClient;
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("");
        return marshaller;
    }

}
