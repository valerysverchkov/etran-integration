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
@XmlRootElement(name = "getInvoice")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"invoiceId", "invNumber", "useValid"})
public class InvoiceRequest {

    @XmlElement(name = "invoiceID")
    @JsonProperty("invoiceID")
    private ValueAttribute invoiceId;

    @XmlElement(name = "invNumber")
    @JsonProperty("invNumber")
    private ValueAttribute invNumber;

    @XmlElement(name = "useValid")
    @JsonProperty("useValid")
    private Object useValid;

}
