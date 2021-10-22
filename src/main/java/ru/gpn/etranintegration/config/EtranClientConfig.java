package ru.gpn.etranintegration.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.gpn.etranintegration.client.EtranClient;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;
import javax.xml.transform.TransformerFactory;

@Configuration
class EtranClientConfig {

    @Value("${service.etran.uri.url}")
    private String etranUrl;

    @Value("${service.etran.uri.path.invoice}")
    private String etranPathInvoice;

    @Value("${service.etran.uri.path.invoiceStatus}")
    private String etranPathInvoiceStatus;

    @Bean
    public EtranClient etranClient() {
        EtranClient etranClient = new EtranClient();
        etranClient.setDefaultUri(etranUrl);
        etranClient.setUriInvoice(etranUrl + etranPathInvoice);
        etranClient.setUriInvoiceStatus(etranUrl + etranPathInvoiceStatus);
        etranClient.setMarshaller(marshallerEtranClient());
        return etranClient;
    }

    @Bean
    public Jaxb2Marshaller marshallerEtranClient() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(GetBlockRequest.class.getPackageName());
        return marshaller;
    }

    @Bean
    public XmlMapper xmlMapper(MappingJackson2XmlHttpMessageConverter converter) {
        return (XmlMapper) converter.getObjectMapper();
    }

}
