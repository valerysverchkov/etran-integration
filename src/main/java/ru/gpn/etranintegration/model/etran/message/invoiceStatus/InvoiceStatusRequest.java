package ru.gpn.etranintegration.model.etran.message.invoiceStatus;

import lombok.Data;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;
import javax.xml.bind.annotation.XmlElement;

@Data
public class InvoiceStatusRequest {

    @XmlElement
    private ValueAttribute fromDate;

    @XmlElement
    private ValueAttribute toDate;

}
