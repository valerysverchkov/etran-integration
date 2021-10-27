package ru.gpn.etranintegration.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

@Configuration
class MessageConfig {

    @Bean
    public XmlMapper xmlMapper(MappingJackson2XmlHttpMessageConverter converter) {
        return (XmlMapper) converter.getObjectMapper();
    }

    @Bean
    public XPath xPath() {
        return XPathFactory.newInstance().newXPath();
    }

}
