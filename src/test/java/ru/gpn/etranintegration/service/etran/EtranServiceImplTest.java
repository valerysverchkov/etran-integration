package ru.gpn.etranintegration.service.etran;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.InputSource;
import ru.gpn.etranintegration.model.etran.GetBlockRequest;
import ru.gpn.etranintegration.model.etran.GetBlockResponse;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusRequest;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusRequestWrapper;
import ru.gpn.etranintegration.model.etran.message.invoiceStatus.InvoiceStatusResponse;
import ru.gpn.etranintegration.service.util.DateUtils;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EtranServiceImplTest {

    private static final String TOKEN = "TOKEN";
    private static final String INVOICE_ID = "ID";
    private static final String AUTH = "logpass";
    private static final String MESSAGE = "message";
    private static final String URL = "url";

    private EtranServiceImpl etranService;
    private RestTemplate restEsbEtran;
    private XmlMapper xmlMapper;
    private XPath xPath;

    @Captor
    private ArgumentCaptor<HttpEntity> httpEntityCaptor;

    @BeforeAll
    void setUp() {
        restEsbEtran = Mockito.mock(RestTemplate.class);
        xmlMapper = Mockito.mock(XmlMapper.class);
        xPath = Mockito.mock(XPath.class);
        etranService = new EtranServiceImpl(restEsbEtran, xmlMapper, xPath);
        ReflectionTestUtils.setField(etranService, "requestCnt", 1);
        ReflectionTestUtils.setField(etranService, "invoiceStatusUrl", URL);
        ReflectionTestUtils.setField(etranService, "invoiceUrl", URL);
    }

    @Test
    void whenCallMethodGetInvoiceStatusThenCallEtranAuthServiceAndCallEtranClientWithToken() throws JsonProcessingException {
        LocalDateTime now = LocalDateTime.now();

        when(xmlMapper.writeValueAsString(any())).thenReturn(MESSAGE);

        GetBlockResponse expectedBlock = new GetBlockResponse();
        expectedBlock.setMessage(MESSAGE);
        ResponseEntity<GetBlockResponse> expectedResponseEntity = new ResponseEntity<>(expectedBlock, HttpStatus.OK);
        when(restEsbEtran.exchange(eq(URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(GetBlockResponse.class)))
                .thenReturn(expectedResponseEntity);

        InvoiceStatusResponse expected = new InvoiceStatusResponse();
        when(xmlMapper.readValue(MESSAGE, InvoiceStatusResponse.class)).thenReturn(expected);
        InvoiceStatusResponse actual = etranService.getInvoiceStatus(now, AUTH, AUTH, TOKEN);
        Assertions.assertEquals(expected, actual);

        verify(restEsbEtran, times(2))
                .exchange(eq(URL), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(GetBlockResponse.class));

        InvoiceStatusRequest invoiceStatusRequestExpected = new InvoiceStatusRequest();
        LocalDateTime fromDate = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime toDate = now.withHour(23).withMinute(59).withSecond(59);
        invoiceStatusRequestExpected.setFromDate(DateUtils.convertToValueAttribute(fromDate));
        invoiceStatusRequestExpected.setToDate(DateUtils.convertToValueAttribute(toDate));
        GetBlockRequest getBlockRequestActual = (GetBlockRequest) httpEntityCaptor.getValue().getBody();
        InvoiceStatusRequestWrapper invoiceStatusRequestWrapperActual = (InvoiceStatusRequestWrapper) getBlockRequestActual.getMessage();
        Assertions.assertEquals(invoiceStatusRequestExpected, invoiceStatusRequestWrapperActual.getInvoiceStatusRequest());
    }

    @Test
    void whenCallMethodGetInvoiceThenCallEtranAuthServiceAndCallEtranClientWithToken() throws JsonProcessingException, XPathExpressionException {
        when(xmlMapper.writeValueAsString(any())).thenReturn(MESSAGE);

        GetBlockResponse expectedBlock = new GetBlockResponse();
        expectedBlock.setMessage(MESSAGE);
        ResponseEntity<GetBlockResponse> expectedResponseEntity = new ResponseEntity<>(expectedBlock, HttpStatus.OK);
        when(restEsbEtran.exchange(eq(URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(GetBlockResponse.class)))
                .thenReturn(expectedResponseEntity);

        LocalDateTime lastOperDate = LocalDateTime.now().withNano(0);
        String lastOperDateExpected = DateUtils.convertToString(lastOperDate);
        when(xPath.evaluate(anyString(), any(InputSource.class), eq(XPathConstants.STRING))).thenReturn(lastOperDateExpected);

        InvoiceResponse actual = etranService.getInvoice(INVOICE_ID, AUTH, AUTH, TOKEN);
        Assertions.assertEquals(INVOICE_ID, actual.getInvoiceId());
        Assertions.assertEquals(lastOperDate, actual.getLastOperDate());
        Assertions.assertEquals(MESSAGE, actual.getMessage());
        Assertions.assertFalse(actual.isErrorAuth());
    }

}