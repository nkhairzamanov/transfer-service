package org.superbank;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.superbank.dataRepository.entity.Account;
import org.superbank.dataRepository.entity.Customer;
import org.superbank.endpoint.TransferOrder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransferServiceTest {
    private final static String BILL_ACC1_IBAN = "US78123987001";
    private final static String BILL_ACC2_IBAN = "US78123987002";
    private final static String STEVE_ACC2_IBAN = "US56123942003";

    private static Client client = ClientBuilder.newClient();
    private static WebTarget target;
    private static TransferService transferService;

    @BeforeClass
    public static void setUp() {
        transferService = new TransferService();
        target = client.target("http://localhost:" + transferService.getHttpPort());

        final long billId = createCustomer("Gates", "Bill");
        createAccount(billId, BILL_ACC1_IBAN, BigDecimal.valueOf(1_000));
        createAccount(billId, BILL_ACC2_IBAN, BigDecimal.valueOf(10_000_000));

        final long steveId = createCustomer("Ballmer", "Steve");
        createAccount(steveId, STEVE_ACC2_IBAN, BigDecimal.valueOf(3_000_000));
    }

    @AfterClass
    public static void cleanUp() {
        transferService.stop();
    }

    @Test
    public void testCustomerCreated() {
        Customer bill = getCustomer("Gates", "Bill");
        assertTrue(bill.getId() > 0);
        assertEquals("Bill", bill.getName());
        assertEquals("Gates", bill.getLastName());
        assertEquals(2, bill.getAccounts().size());
    }

    @Test(expected = AssertionError.class)
    public void testCustomerNamesUnique() {
        createCustomer("Ballmer", "Steve");
    }

    @Test(expected = AssertionError.class)
    public void testAccountIbanUnique() {
        createAccount(getCustomer("Ballmer", "Steve").getId(), STEVE_ACC2_IBAN, BigDecimal.valueOf(3_000_000));
    }

    @Test
    public void testAccountCreated() {
        final Account steveBalmer = getAccount(STEVE_ACC2_IBAN);
        assertTrue(steveBalmer.getId() > 0);
        assertEquals(STEVE_ACC2_IBAN, steveBalmer.getIban());
        assertEquals(0, BigDecimal.valueOf(3_000_000).compareTo(steveBalmer.getBalance()));
    }

    @Test
    public void testTransferToSameCustomer() {
        transfer(BILL_ACC1_IBAN, BILL_ACC2_IBAN, BigDecimal.valueOf(1_000), "consolidate own money");

        assertEquals(0, BigDecimal.ZERO.compareTo(getAccount(BILL_ACC1_IBAN).getBalance()));
        assertEquals(0, BigDecimal.valueOf(10_001_000).compareTo(getAccount(BILL_ACC2_IBAN).getBalance()));
    }

    @Test
    public void testTransferToSameAccount() {
        transfer(STEVE_ACC2_IBAN, STEVE_ACC2_IBAN, BigDecimal.valueOf(1_000), "try to brake the system");
        assertEquals(0, BigDecimal.valueOf(3_000_000).compareTo(getAccount(STEVE_ACC2_IBAN).getBalance()));
    }

    @Test
    public void testTransferToOtherCustomer() {
        final BigDecimal billBalanceBefore = getAccount(BILL_ACC2_IBAN).getBalance();
        transfer(STEVE_ACC2_IBAN, BILL_ACC2_IBAN, BigDecimal.valueOf(3_000_000), "make Bill happy");
        final BigDecimal billBalanceAfter = getAccount(BILL_ACC2_IBAN).getBalance();

        assertEquals(0, BigDecimal.ZERO.compareTo(getAccount(STEVE_ACC2_IBAN).getBalance()));
        assertEquals(0, BigDecimal.valueOf(3_000_000).compareTo(billBalanceAfter.subtract(billBalanceBefore)));
    }


    private static long createCustomer(String lastName, String name) {
        Response res = target.path("transfer-service/customer")
                .request().put(Entity.json(new Customer(lastName, name)));
        assertEquals(200, res.getStatus());
        return res.readEntity(Long.class);
    }

    private static void createAccount(long customerId, String iban, BigDecimal amount) {
        Response res = target.path("transfer-service/customer/" + customerId + "/account")
                .request().put(Entity.json(new Account(iban, amount)));
        assertEquals(200, res.getStatus());
    }

    private Customer getCustomer(String lastName, String name) {
        Response res = target.path("transfer-service/customer/" + lastName + "/" + name).request().get();
        assertEquals(200, res.getStatus());
        return res.readEntity(Customer.class);
    }

    private Account getAccount(String iban) {
        final Response res = target.path("transfer-service/account/" + iban).request().get();
        assertEquals(200, res.getStatus());

        return res.readEntity(Account.class);
    }

    private void transfer(String from, String to, BigDecimal amount, String msg) {
        final Response res = target.path("transfer-service/transfer")
                .request().put(
                        Entity.json(new TransferOrder(from, to, amount, msg)));
        assertEquals(204, res.getStatus());
    }
}
