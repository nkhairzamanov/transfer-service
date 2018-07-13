package org.superbank.dataRepository;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.superbank.TransferServiceRuntimeException;
import org.superbank.dataRepository.entity.Account;
import org.superbank.dataRepository.entity.Customer;
import org.superbank.dataRepository.entity.Transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class DataRepositoryImpl implements DataRepository {

    private final SessionFactory sessionFactory;

    public DataRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public long createCustomer(Customer customer) {
        requireNotBlank(customer.getName(), "customer's name cannot be empty");
        requireNotBlank(customer.getLastName(), "customer's last name cannot be empty");

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            customer.setId(0);
            session.persist(customer);
            tx.commit();
            return customer.getId();
        }
    }

    private void requireNotBlank(String val, String msg) {
        if (StringUtils.isBlank(val)) {
            throw new TransferServiceRuntimeException(msg);
        }
    }

    @Override
    public long createAccount(Account account, long customerId) {
        requireNotBlank(account.getIban(), "customer's account iban id cannot be empty");

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            account.setId(0);
            session.persist(account);

            Customer customer = session.get(Customer.class, customerId);
            customer.getAccounts().add(account);
            session.merge(customer);
            tx.commit();

            return account.getId();
        }
    }

    @Override
    public Optional<Customer> findCustomer(String lastName, String name) {
        try (Session session = sessionFactory.openSession()) {
            Query<Customer> query = session.createQuery("from Customer where name=:name and lastName=:lastName");
            query.setParameter("name", name);
            query.setParameter("lastName", lastName);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Optional<Account> findAccount(String iban) {
        try (Session session = sessionFactory.openSession()) {
            Query<Account> query = session.createQuery("from Account where iban=:iban");
            query.setParameter("iban", iban);
            Optional<Account> account = query.uniqueResultOptional();
            return account;
        }
    }

    private Transfer createTransfer(Account from, Account to, BigDecimal amount, String message) {
        Transfer transfer = new Transfer();
        transfer.setAmount(amount);
        transfer.setDateTime(LocalDateTime.now());
        transfer.setSourceAccount(from);
        transfer.setTargetAccount(to);
        transfer.setMessage(message);
        return transfer;
    }

    @Override
    public void transfer(String fromIban, String toIban, BigDecimal amount, String message) {
        requireNotBlank(fromIban, "The account iban number to withdraw from cannot be empty");
        requireNotBlank(toIban, "The account iban number to receive the money cannot be empty");
        requireNotBlank(message, "The transfer details message cannot be empty");
        if (amount == null) {
            throw new TransferServiceRuntimeException("The amount to transfer cannot be null");
        }

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Query<Account> query = session.createQuery("from Account where iban=:iban");
            query.setParameter("iban", fromIban);
            Account fromAccount = query.uniqueResultOptional()
                    .orElseThrow(() -> new TransferServiceRuntimeException("account " + fromIban + " not found"));
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            session.merge(fromAccount);

            query.setParameter("iban", toIban);
            Account toAccount = query.uniqueResultOptional()
                    .orElseThrow(() -> new TransferServiceRuntimeException("account " + toIban + "not found"));
            toAccount.setBalance(toAccount.getBalance().add(amount));
            session.merge(toAccount);

            session.persist(createTransfer(fromAccount, toAccount, amount, message));

            tx.commit();
        }
    }
}
