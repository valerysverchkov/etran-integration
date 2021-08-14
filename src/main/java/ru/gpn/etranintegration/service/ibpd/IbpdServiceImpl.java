package ru.gpn.etranintegration.service.ibpd;

import org.springframework.stereotype.Service;
import ru.gpn.etranintegration.model.etran.InvoiceResponse;
import java.time.LocalDateTime;
import java.util.List;

@Service
class IbpdServiceImpl implements IbpdService {

    @Override
    public List<String> getInvoiceIds() {
        //TODO: Call ibpd with uri - ???
        return null;
    }

    @Override
    public LocalDateTime getLastOperDateByInvoiceId(String invNumber) {
        //TODO: call ibpd with uri - https://url_to_ibpd_connector/json/ibpd-rest-api/etran_invoices_vw?select=last_oper_date&invoice_id.eq.InvoiceID
        return null;
    }

    @Override
    public void setNewInvoice(InvoiceResponse invoiceFromEtran) {
        /*
        TODO: call POST ibpd with uri - https://url_to_ibpd_connector/json/ibpd-rest-api/etran_invoices_vw
        json - {‘inv_info’:’message from ETRAN’}
        */
    }

    @Override
    public void updateInvoice(InvoiceResponse invoiceFromEtran) {
        /*
        TODO: call http PATCH with url - https://url_to_ibpd_connector/json/ibpd-rest-api/etran_invoices_vw?invoice_id.eq.InvoiceID
        json - {‘inv_info’:’message from ETRAN’}
         */
    }

}
