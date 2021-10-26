package ru.gpn.etranintegration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.gpn.etranintegration.service.cache.CacheService;
import ru.gpn.etranintegration.service.process.Process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EtranIntegrationApplicationTest {

    private static final String EXPECTED_URI_ESB_AUTH = "/uriEsbAuth";
    private static final String EXPECTED_TOKEN = "token";
    private static final String EXPECTED_URI_ETRAN_INVOICE_STATUS = "/invoiceStatus";
    private static final String EXPECTED_URI_ETRAN_INVOICE = "/invoice";
    private static final String EXPECTED_URI_IBPD_GET_LAST_OPER_DATE = "lastOperDate";
    private static final String EXPECTED_URI_IBPD_SET_NEW_INVOICE = "newInvoice";
    private static final String INVOICE_ID = "ID";
    private static final String EXPECTED_ETRAN_LOGIN = "login1";
    private static final String EXPECTED_ETRAN_PASS = "pass1";

    @Autowired
    private Process invoiceProcess;

    @Qualifier("restEsbAuth")
    @Autowired
    private RestTemplate restEsbAuth;

    @Qualifier("restEsbEtran")
    @Autowired
    private RestTemplate restEsbEtran;

    @Qualifier("restIbpd")
    @Autowired
    private RestTemplate restIbpd;

    @Autowired
    private CacheService cacheService;

    private MockRestServiceServer mockEsbEtranService;
    private MockRestServiceServer mockEsbAuthService;
    private MockRestServiceServer mockIbpdService;

    @BeforeAll
    void setUp() {
        mockEsbEtranService = MockRestServiceServer.createServer(restEsbEtran);
        mockEsbAuthService = MockRestServiceServer.createServer(restEsbAuth);
        mockIbpdService = MockRestServiceServer.createServer(restIbpd);
    }

    @Test
    void contextLoads() throws IOException {
//        EsbAuthRequest esbAuthRequest = new EsbAuthRequest();
//        esbAuthRequest.setGrantType("grantType");
//        esbAuthRequest.setUsername("username");
//        esbAuthRequest.setPassword("password");
//        esbAuthRequest.setClientId("clientId");
//        esbAuthRequest.setClientSecret("clientSecret");
//        mockEsbAuthService.expect(requestTo(EXPECTED_URI_ESB_AUTH))
//                .andExpect(method(HttpMethod.POST))
//                .andExpect(content().string(esbAuthRequest.toString()))
//                .andRespond(withSuccess(EXPECTED_TOKEN, MediaType.APPLICATION_FORM_URLENCODED));
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime dateFrom = now.withHour(0).withMinute(0).withSecond(0);
//        LocalDateTime dateTo = now.withHour(23).withMinute(59).withSecond(59);
//
//        String invoiceStatusRequestMessage = readTestFile("invoiceStatusRequest.xml");
//        invoiceStatusRequestMessage = String.format(invoiceStatusRequestMessage, EXPECTED_ETRAN_LOGIN,
//                EXPECTED_ETRAN_PASS, DateUtils.convertToString(dateFrom), DateUtils.convertToString(dateTo));
//
//        String invoiceStatusResponseMessage = readTestFile("invoiceStatusResponse.xml");
//        invoiceStatusResponseMessage = String.format(invoiceStatusResponseMessage, INVOICE_ID, "NUM",
//                DateUtils.convertToString(now));
//
//        mockEsbEtranService.expect(requestTo(EXPECTED_URI_ETRAN_INVOICE_STATUS))
//                .andExpect(content().string(invoiceStatusRequestMessage))
//                .andRespond(withSuccess(invoiceStatusResponseMessage, MediaType.APPLICATION_XML));
//
//        String invoiceRequestMessage = readTestFile("invoiceRequest.xml");
//        invoiceRequestMessage = String.format(invoiceRequestMessage, EXPECTED_ETRAN_LOGIN,
//                EXPECTED_ETRAN_PASS, INVOICE_ID, "NUM");
//
//        String invoiceResponseMessage = readTestFile("invoiceResponse.xml");
//        invoiceResponseMessage = String.format(invoiceResponseMessage, INVOICE_ID, DateUtils.convertToString(now));
//
//        mockEsbEtranService.expect(requestTo(EXPECTED_URI_ETRAN_INVOICE))
//                .andExpect(content().xml(invoiceRequestMessage))
//                .andRespond(withSuccess(invoiceResponseMessage, MediaType.APPLICATION_XML));
//
//        List<String> responseLastOperDate = new ArrayList<>();
//        responseLastOperDate.add(DateUtils.convertToString(now));
//
//        mockIbpdService.expect(requestTo(EXPECTED_URI_IBPD_GET_LAST_OPER_DATE))
//                .andExpect(method(HttpMethod.GET))
//                .andRespond(withSuccess(responseLastOperDate.toString(), MediaType.TEXT_PLAIN));
//
//        String messageToIbpd = String.format(readTestFile("invoiceToIbpd.json"), INVOICE_ID, DateUtils.convertToString(now));
//        mockIbpdService.expect(requestTo(EXPECTED_URI_IBPD_SET_NEW_INVOICE))
//                .andExpect(method(HttpMethod.POST))
//                .andExpect(content().json(messageToIbpd))
//                .andRespond(withSuccess());
//
//        invoiceProcess.processing();
//
//        String actualLastOperDate = cacheService.getLastOperDateByInvoiceId(INVOICE_ID);
//        assertEquals("10.10.2010 10:10:10", actualLastOperDate);
    }

    private String readTestFile(String fileName) throws IOException {
        File invoiceStatusRequest = new File("src/test/resources/" + fileName);
        return Files.readString(invoiceStatusRequest.toPath());
    }

}