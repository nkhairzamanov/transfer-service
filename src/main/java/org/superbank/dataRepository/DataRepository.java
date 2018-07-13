package org.superbank.dataRepository;

import org.superbank.dataRepository.entity.Account;
import org.superbank.dataRepository.entity.Customer;

import java.math.BigDecimal;
import java.util.Optional;

public interface DataRepository {

    long createCustomer(Customer customer);
    long createAccount(Account account, long customerId);

    Optional<Customer> findCustomer(String lastName, String name);
    Optional<Account> findAccount(String iban);

    void transfer(String fromIban, String toIban, BigDecimal amount, String message);
}
