package ru.gpn.etranintegration.service.etran;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.GetBlockResponse;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.converter.EtranConverter;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EtranServiceImplTest {

    private static final String TOKEN = "TOKEN";
    private static final String INVOICE_ID = "ID";
    private static final String INVOICE_NUM = "NUM";
    private static final String AUTH = "logpass";
    private static final String MESSAGE = "message";
    private static final String URL = "url";

    private EtranServiceImpl etranService;
    private RestTemplate restEsbEtran;
    private EtranConverter etranConverter;

    @BeforeAll
    void setUp() {
        restEsbEtran = Mockito.mock(RestTemplate.class);
        etranConverter = Mockito.mock(EtranConverter.class);
        etranService = new EtranServiceImpl(restEsbEtran, etranConverter);
        ReflectionTestUtils.setField(etranService, "requestCnt", 1);
        ReflectionTestUtils.setField(etranService, "invoiceStatusUrl", URL);
        ReflectionTestUtils.setField(etranService, "invoiceUrl", URL);
    }

    @Test
    void whenCallMethodGetInvoiceStatusThenCallEtranAuthServiceAndCallEtranClientWithToken(){
        LocalDateTime now = LocalDateTime.now();

        GetBlockRequest expectedGetBlockRequest = new GetBlockRequest();
        when(etranConverter.convertToInvoiceStatusRequest(now, AUTH, AUTH)).thenReturn(expectedGetBlockRequest);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(TOKEN);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        HttpEntity<GetBlockRequest> blockRequestHttpEntity = new HttpEntity<>(expectedGetBlockRequest, httpHeaders);

        GetBlockResponse expectedBlock = new GetBlockResponse();
        expectedBlock.setMessage(MESSAGE);
        ResponseEntity<GetBlockResponse> expectedResponseEntity = new ResponseEntity<>(expectedBlock, HttpStatus.OK);
        when(restEsbEtran.exchange(eq(URL), eq(HttpMethod.POST), eq(blockRequestHttpEntity), eq(GetBlockResponse.class)))
                .thenReturn(expectedResponseEntity);

        InvoiceStatusResponse expected = new InvoiceStatusResponse();
        when(etranConverter.convertToInvoiceStatusResponse(eq(expectedResponseEntity))).thenReturn(expected);

        InvoiceStatusResponse actual = etranService.getInvoiceStatus(now, AUTH, AUTH, TOKEN);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void whenCallMethodGetInvoiceThenCallEtranAuthServiceAndCallEtranClientWithToken(){
        GetBlockRequest expectedGetBlockRequest = new GetBlockRequest();
        when(etranConverter.convertToInvoiceRequest(INVOICE_ID, INVOICE_NUM, AUTH, AUTH)).thenReturn(expectedGetBlockRequest);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(TOKEN);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        HttpEntity<GetBlockRequest> blockRequestHttpEntity = new HttpEntity<>(expectedGetBlockRequest, httpHeaders);

        GetBlockResponse expectedBlock = new GetBlockResponse();
        expectedBlock.setMessage(MESSAGE);
        ResponseEntity<GetBlockResponse> expectedResponseEntity = new ResponseEntity<>(expectedBlock, HttpStatus.OK);
        when(restEsbEtran.exchange(eq(URL), eq(HttpMethod.POST), eq(blockRequestHttpEntity), eq(GetBlockResponse.class)))
                .thenReturn(expectedResponseEntity);

        InvoiceResponse expectedInvoiceResponse = new InvoiceResponse();

        when(etranConverter.convertToInvoiceResponse(eq(expectedResponseEntity))).thenReturn(expectedInvoiceResponse);

        InvoiceResponse actualInvoiceResponse = etranService.getInvoice(INVOICE_ID, INVOICE_NUM, AUTH, AUTH, TOKEN);

        Assertions.assertEquals(expectedInvoiceResponse, actualInvoiceResponse);
    }

}