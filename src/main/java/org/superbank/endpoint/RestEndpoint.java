package org.superbank.endpoint;


import org.superbank.TransferServiceRuntimeException;
import org.superbank.dataRepository.DataRepository;
import org.superbank.dataRepository.entity.Account;
import org.superbank.dataRepository.entity.Customer;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/transfer-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RestEndpoint {

    @Inject
    private DataRepository dataRepository;

    @PUT
    @Path("/customer")
    public long createCustomer(Customer customer) {
        return  dataRepository.createCustomer(customer);
    }

    @GET
    @Path("/customer/{lastName}/{name}")
    public Customer findCustomer(@PathParam("lastName") String lastName, @PathParam("name") String name) {
        return dataRepository.findCustomer(lastName, name)
                .orElseThrow(() -> new TransferServiceRuntimeException("customer not found"));
    }

    @PUT
    @Path("/customer/{customerId}/account")
    public long createAccount(Account account, @PathParam("customerId")long customerId) {
        return  dataRepository.createAccount(account, customerId);
    }

    @GET
    @Path("/account/{iban}")
    public Account findAccount(@PathParam("iban") String iban) {
        return dataRepository.findAccount(iban)
                .orElseThrow(() -> new WebApplicationException("account not found"));
    }

    @PUT
    @Path("/transfer")
    public void transfer(TransferOrder transferOrder) {
        dataRepository.transfer(transferOrder.getFromIban(), transferOrder.getToIban(),
                transferOrder.getAmount(), transferOrder.getMessage());
    }
}
