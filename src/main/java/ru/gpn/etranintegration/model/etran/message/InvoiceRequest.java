package ru.gpn.etranintegration.model.etran.message;

import lombok.Data;
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
    private ValueAttribute invoiceId;
    @XmlElement(name = "invNumber")
    private ValueAttribute invNumber;
    @XmlElement(name = "useValid")
    private Object useValid;

}
