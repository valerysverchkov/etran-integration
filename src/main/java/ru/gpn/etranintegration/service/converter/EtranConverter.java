package ru.gpn.etranintegration.service.converter;

import org.springframework.http.ResponseEntity;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.GetBlockResponse;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import java.time.LocalDateTime;

public interface EtranConverter {

    GetBlockRequest convertToInvoiceStatusRequest(LocalDateTime currentDay, String login, String password);

    InvoiceStatusResponse convertToInvoiceStatusResponse(ResponseEntity<GetBlockResponse> responseEntity);

    GetBlockRequest convertToInvoiceRequest(String invoiceId, String invoiceNum, String login, String password);

    InvoiceResponse convertToInvoiceResponse(ResponseEntity<GetBlockResponse> responseEntity);

}
