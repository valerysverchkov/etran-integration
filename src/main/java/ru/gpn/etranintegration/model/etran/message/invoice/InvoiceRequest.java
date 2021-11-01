package ru.gpn.etranintegration.model.etran.message.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Data
public class InvoiceRequest {

    @JsonProperty("invoiceID")
    private ValueAttribute invoiceId;

    @JsonProperty("invNumber")
    private ValueAttribute invNumber;

    @JsonProperty("useValid")
    private Object useValid;

}
