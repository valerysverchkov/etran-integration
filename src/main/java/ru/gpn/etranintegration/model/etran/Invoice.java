package ru.gpn.etranintegration.model.etran;

import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
@XmlRootElement(name = "invoice")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"invoiceId", "invNumber", "invoiceStateId", "invoiceState", "invNeedForEcp", "invoiceLastOper"})
public class Invoice {

    @XmlElement(name = "invoiceID")
    private ValueAttribute invoiceId;

    @XmlElement
    private ValueAttribute invNumber;

    @XmlElement(name = "invoiceStateID")
    private ValueAttribute invoiceStateId;

    @XmlElement
    private ValueAttribute invoiceState;

    @XmlElement(name = "invNeedForECP")
    private ValueAttribute invNeedForEcp;

    @XmlElement
    private ValueAttribute invoiceLastOper;

}
