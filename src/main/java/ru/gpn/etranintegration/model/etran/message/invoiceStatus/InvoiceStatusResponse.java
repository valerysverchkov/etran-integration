package ru.gpn.etranintegration.model.etran.message.invoiceStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;

import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@JsonRootName("invoiceStatusReply")
public class InvoiceStatusResponse {

    @JsonProperty("invoice")
    private List<Invoice> invoice;

    private boolean errorAuth;

}
