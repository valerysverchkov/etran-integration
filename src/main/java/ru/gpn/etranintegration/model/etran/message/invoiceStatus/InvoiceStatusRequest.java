package ru.gpn.etranintegration.model.etran.message.invoiceStatus;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@Data
@XmlRootElement(name = "invoiceStatus")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"fromDate", "toDate"})
public class InvoiceStatusRequest {

    @XmlElement
    private ValueAttribute fromDate;

    @XmlElement
    private ValueAttribute toDate;

}
